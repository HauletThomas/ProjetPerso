package com.example.perso.controllers;

import com.example.perso.models.MatchStatistics;
import com.example.perso.services.RiotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RiotController {

    @Autowired
    private RiotService riotService;

    @GetMapping("/getAccountByGameName")
    public String getAccountByGameName(@RequestParam String gameName, @RequestParam String tagLine) {
        return riotService.getAccountByGameName(gameName, tagLine);
    }

    @GetMapping("/getMatchList")
    public String getMatchList(@RequestParam String puuid,
                              @RequestParam Integer start,
                              @RequestParam Integer count) {
        return riotService.getMatchList(puuid, start, count);
    }
    @GetMapping("/getMatchById")
    public MatchStatistics getMatchById(@RequestParam String puuid){
        return riotService.getMatchById(puuid);
    }

//    @GetMapping("/getMatchStatistics")
//    public MatchStatistics getMatchStatistics(@RequestParam String puuid, @RequestParam Integer count){
//        return riotService.getMatchStatistics(puuid, count);
//    }

}
