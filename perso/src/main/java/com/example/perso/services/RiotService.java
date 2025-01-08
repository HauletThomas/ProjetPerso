package com.example.perso.services;

import com.example.perso.models.MatchStatistics;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class RiotService {

    @Value("${riot.api.key}") // Assurez-vous de configurer votre clé API dans application.properties
    private String apiKey;

    private static final String API_URL = "https://europe.api.riotgames.com";

    public String getAccountByGameName(String gameName, String tagLine) {
        String url = UriComponentsBuilder.fromHttpUrl(API_URL + "/riot/account/v1/accounts/by-riot-id/")
                .pathSegment(gameName)
                .pathSegment(tagLine)
                .queryParam("api_key", apiKey)
                .toUriString();
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);

        // Parse the response to get the puuid
        // You might need to deserialize the JSON response to extract the puuid
        // assuming response contains the PUUID
        return response; // Extract and return the PUUID from the response
    }


    public MatchStatistics getMatchById(String puuid) {
        String urlBuilder0 = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("europe.api.riotgames.com")
                .path("/lol/match/v5/matches/by-puuid/{puuid}/ids")
                .queryParam("queue", 420)
                .queryParam("start", 0)
                .queryParam("count", 20)
                .queryParam("api_key", apiKey)
                .buildAndExpand(puuid)
                .toUriString();

        RestTemplate restTemplateMatchId = new RestTemplate();
        String[] matchIds = restTemplateMatchId.getForObject(urlBuilder0, String[].class);

        ObjectMapper objectMapper = new ObjectMapper();
        MatchStatistics stats = new MatchStatistics();

        // Map pour compter les performances des champions
        Map<String, Integer> allyWins = new HashMap<>();
        Map<String, Integer> allyLosses = new HashMap<>();
        Map<String, Integer> enemyWins = new HashMap<>();
        Map<String, Integer> enemyLosses = new HashMap<>();

        assert matchIds != null;
        for (String matchId: matchIds){
            String urlBuilder = UriComponentsBuilder.newInstance()
                    .scheme("https")
                    .host("europe.api.riotgames.com")
                    .path("/lol/match/v5/matches/{matchId}")
                    .queryParam("api_key", apiKey)
                    .buildAndExpand(matchId)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Accept", "*/*");
            headers.add("Connection", "keep-alive");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.exchange(
                    urlBuilder,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            // Récupération des IDs de match

            try {

                JsonNode matchJson = objectMapper.readTree(String.valueOf(response.getBody()));

                JsonNode info = matchJson.get("info");
                JsonNode participants = info.get("participants");

                //boolean firstDragon = false;
                String side = "Blue";
                boolean win = false;

                for (JsonNode participant : participants) {
                    String summonerPuuid = participant.get("puuid").asText();
                    if (summonerPuuid.equals(puuid)) {
                        // Identifier votre teamId et votre statut de victoire
                        win = participant.get("win").asBoolean();
                        side = (participant.get("teamId").asInt() == 100) ? "Blue" : "Red";

                        if (win) {
                            stats.incrementWins();
                        } else {
                            stats.incrementLosses();
                        }
                        break; //joueur trouve
                    }
                }

                for (JsonNode participant : participants) {

                    String summonerPuuid = participant.get("puuid").asText();

                    // Ignorer le joueur principal
                    if (summonerPuuid.equals(puuid)) {
                        continue;
                    }
                    String championName = participant.get("championName").asText();
                    boolean isWin = participant.get("win").asBoolean();
                    int teamId = participant.get("teamId").asInt();

                    if (teamId == (side.equals("Blue") ? 100 : 200)) {
                        // Alliés (même teamId que le joueur principal)
                        if (isWin) {
                            allyWins.merge(championName, 1, Integer::sum);
                        } else {
                            allyLosses.merge(championName, 1, Integer::sum);
                        }
                    } else {
                        // Ennemis (autre teamId)
                        if (isWin) {
                            enemyWins.merge(championName, 1, Integer::sum);
                        } else {
                            enemyLosses.merge(championName, 1, Integer::sum);
                        }
                    }
                }


                // Récupérer les équipes
                JsonNode teams = info.get("teams");

// Vérifier si le premier dragon a été pris par l'équipe du joueur principal
                for (JsonNode team : teams) {
                    int teamId = team.get("teamId").asInt();
                    boolean firstDragon = team.get("objectives").get("dragon").get("first").asBoolean();
                    boolean firstHorde = team.get("objectives").get("horde").get("first").asBoolean(); //voidgrubs
                    boolean sixHorde = team.get("objectives").get("horde").get("kills").asInt() == 6; //voidgrubs
                    int testsixHorde = team.get("objectives").get("horde").get("kills").asInt(); //voidgrubs


                    if (teamId == (side.equals("Blue") ? 100 : 200)) {
                        // Si le joueur principal est dans cette équipe
                        if(win){
                            System.out.println("win");

                        }
                        else System.out.println("lose");
                        System.out.println(testsixHorde);


                        if (firstDragon) {
                            stats.incrementFirstDragonGames();
                            if (win) {
                                stats.incrementFirstDragonWins();
                            }
                        }
                        if (firstHorde) {
                            stats.incrementFirstHordeGames();
                            if (win) {
                                stats.incrementFirstHordeWins();
                            }
                        }
                        if (sixHorde) {
                            stats.incrementSixHordeGames();
                            if (win) {
                                stats.incrementSixHordeWins();
                            }
                        }
                        break; // Pas besoin de vérifier l'autre équipe
                    }
                }

                // Analyse des participants...
            } catch (HttpStatusCodeException e) {
                System.err.println("Erreur HTTP pour le match ");
            } catch (Exception e) {
                System.err.println("Erreur lors de la récupération du match " );
            }

            // Ajouter les tops des champions
            stats.setTopAlliesWin(getTopChampions(allyWins, 10));
            stats.setTopAlliesLoss(getTopChampions(allyLosses, 10));
            stats.setTopEnemiesWin(getTopChampions(enemyWins, 10));
            stats.setTopEnemiesLoss(getTopChampions(enemyLosses, 10));
        }

                return stats;

    }

    private List<String> getTopChampions(Map<String, Integer> champions, int limit) {
        return champions.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue())) // Trier par nombre décroissant
                .limit(limit)
                .map(entry -> entry.getKey() + " (" + entry.getValue() + ")") // Inclure le nombre dans le résultat
                .collect(Collectors.toList());
    }


}

