package com.example.perso.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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

        System.out.println("URL construite : " + urlBuilder);


        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(urlBuilder, String.class);

        return response;
    }
}

