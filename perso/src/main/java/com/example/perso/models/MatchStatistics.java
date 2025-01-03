package com.example.perso.models;

import java.util.ArrayList;
import java.util.List;

public class MatchStatistics {
    private int wins = 0;
    private int losses = 0;
    private int firstDragonGames = 0;
    private int firstDragonWins = 0;
    private List<String> topAlliesWin = new ArrayList<>();
    private List<String> topAlliesLoss = new ArrayList<>();
    private List<String> topEnemiesWin = new ArrayList<>();
    private List<String> topEnemiesLoss = new ArrayList<>();

    public void incrementWins() {
        this.wins++;
    }

    public void incrementLosses() {
        this.losses++;
    }

    public void incrementFirstDragonGames() {
        this.firstDragonGames++;
    }

    public void incrementFirstDragonWins() {
        this.firstDragonWins++;
    }

    public double getFirstDragonWinRate() {
        return firstDragonGames > 0 ? (double) firstDragonWins / firstDragonGames * 100 : 0;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getFirstDragonGames() {
        return firstDragonGames;
    }

    public void setFirstDragonGames(int firstDragonGames) {
        this.firstDragonGames = firstDragonGames;
    }

    public int getFirstDragonWins() {
        return firstDragonWins;
    }

    public void setFirstDragonWins(int firstDragonWins) {
        this.firstDragonWins = firstDragonWins;
    }

    public List<String> getTopAlliesWin() {
        return topAlliesWin;
    }

    public void setTopAlliesWin(List<String> topAlliesWin) {
        this.topAlliesWin = topAlliesWin;
    }

    public List<String> getTopAlliesLoss() {
        return topAlliesLoss;
    }

    public void setTopAlliesLoss(List<String> topAlliesLoss) {
        this.topAlliesLoss = topAlliesLoss;
    }

    public List<String> getTopEnemiesWin() {
        return topEnemiesWin;
    }

    public void setTopEnemiesWin(List<String> topEnemiesWin) {
        this.topEnemiesWin = topEnemiesWin;
    }

    public List<String> getTopEnemiesLoss() {
        return topEnemiesLoss;
    }

    public void setTopEnemiesLoss(List<String> topEnemiesLoss) {
        this.topEnemiesLoss = topEnemiesLoss;
    }


}
