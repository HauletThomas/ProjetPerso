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

    public String getMatchList(String puuid,Integer start, Integer count) {
        String urlBuilder = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("europe.api.riotgames.com")
                .path("/lol/match/v5/matches/by-puuid/{puuid}/ids")
                .queryParam("start", start)
                .queryParam("count", count)
                .queryParam("api_key", apiKey)
                .buildAndExpand(puuid)
                .toUriString();

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(urlBuilder, String.class);

        return response;
    }

    public MatchStatistics getMatchById(String puuid) {
        String urlBuilder = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("europe.api.riotgames.com")
                .path("/lol/match/v5/matches/EUW1_7243432438")
                .queryParam("api_key", apiKey)
                .buildAndExpand()
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
        System.out.println(response);


            // Récupération des IDs de match


            ObjectMapper objectMapper = new ObjectMapper();
            MatchStatistics stats = new MatchStatistics();

            // Map pour compter les performances des champions
            Map<String, Integer> allyWins = new HashMap<>();
            Map<String, Integer> allyLosses = new HashMap<>();
            Map<String, Integer> enemyWins = new HashMap<>();
            Map<String, Integer> enemyLosses = new HashMap<>();


                try {


                    JsonNode matchJson = objectMapper.readTree(String.valueOf(response.getBody()));

                    JsonNode info = matchJson.get("info");
                    JsonNode participants = info.get("participants");

                    boolean firstDragon = false;
                    String side = "Blue";
                    boolean win = false;
                    System.out.println("flag1");

                    for (JsonNode participant : participants) {
                        String summonerPuuid = participant.get("puuid").asText();
                        System.out.println(summonerPuuid);

                        String championName = participant.get("championName").asText();
                        System.out.println(championName);

                        boolean isWin = participant.get("win").asBoolean();
                        System.out.println(isWin);

                        int teamId = participant.get("teamId").asInt();
                        System.out.println(teamId);


                        if (summonerPuuid.equals(puuid)) {
                            System.out.println("flaf if puiid");

                            // Votre côté et votre statut de victoire
                            //win = isWin;
                            side = (teamId == 100) ? "Blue" : "Red";
                            System.out.println(win + "win");

                            firstDragon = true;
                            System.out.println(firstDragon);


                            if (isWin) {
                                System.out.println("flag win");

                                stats.incrementWins();
                                System.out.println("flag win");

                            } else {
                                stats.incrementLosses();
                                System.out.println("flag lose");

                            }
                        } else {
                            // Comptabiliser les champions alliés ou ennemis
                            if (teamId == 100 || teamId == 200) {
                                if (isWin) {
                                    if (teamId == 100) {
                                        allyWins.merge(championName, 1, Integer::sum);
                                    } else {
                                        enemyLosses.merge(championName, 1, Integer::sum);
                                    }
                                } else {
                                    if (teamId == 100) {
                                        allyLosses.merge(championName, 1, Integer::sum);
                                    } else {
                                        enemyWins.merge(championName, 1, Integer::sum);
                                    }
                                }
                            }
                        }
                    }
                    System.out.println("flag2");

                    // Statistiques sur le premier dragon
                    if (firstDragon) {
                        stats.incrementFirstDragonGames();
                        if (win) {
                            stats.incrementFirstDragonWins();
                        }
                    }

                    // Analyse des participants...
                } catch (HttpStatusCodeException e) {
                    System.err.println("Erreur HTTP pour le match ");
                } catch (Exception e) {
                    System.err.println("Erreur lors de la récupération du match " );
                }

            System.out.println("flage3");
            // Ajouter les tops des champions
            stats.setTopAlliesWin(getTopChampions(allyWins, 10));
            stats.setTopAlliesLoss(getTopChampions(allyLosses, 10));
            stats.setTopEnemiesWin(getTopChampions(enemyWins, 10));
            stats.setTopEnemiesLoss(getTopChampions(enemyLosses, 10));

            return stats;

    }


    private List<String> getTopChampions(Map<String, Integer> champions, int limit) {
        return champions.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }


}

