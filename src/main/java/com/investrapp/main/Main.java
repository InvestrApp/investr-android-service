package com.investrapp.main;

import org.parse4j.Parse;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        String APP_ID = "investrAndroid";
        String APP_REST_API_ID = "investrParse2017";
        String CUSTOM_SERVER_PATH = "https://investr-android.herokuapp.com/parse";
        Parse.initialize(APP_ID, APP_REST_API_ID, CUSTOM_SERVER_PATH);

        RankingService rankingService = new RankingService();

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(rankingService, 0, 15, TimeUnit.MINUTES);
    }

}
