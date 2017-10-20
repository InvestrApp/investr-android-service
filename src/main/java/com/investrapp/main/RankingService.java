package com.investrapp.main;

import org.parse4j.ParseException;
import org.parse4j.ParseObject;
import org.parse4j.ParseQuery;

import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RankingService implements Runnable {

    private static String RANKING_CLASS = "Ranking";
    private static String TRANSACTION_CLASS = "Transaction";
    private static String COMPETITION_FIELD = "competition";
    private static String PLAYER_FIELD = "player";
    private static String PRICE_FIELD = "price";
    private static String UNITS_FIELD = "units";
    private static String ACTION_FIELD = "action";
    private static String ASSET_TYPE_FIELD = "asset_type";
    private static String TICKER_FIELD = "asset_ticker";
    private static String RANKING_FIELD = "ranking";
    private static String PORTFOLIO_VALUE_FIELD = "portfolio_value";
    private static String CASH_ASSET_TYPE = "cash";
    private static String CRYPTOCURRENCY_ASSET_TYPE = "cryptocurrency";
    private static String STOCK_ASSET_TYPE = "stock";
    private static String BUY_ACTION = "BUY";

    private List<ParseObject> mTransactions;
    private HashMap<String, HashMap<String, Double>> mCompetitionRankings;
    private HashMap<String, Double> mAssetPrices;
    private HashMap<String, ParseObject> mCompetitions;
    private HashMap<String, ParseObject> mPlayers;

    private final static Logger LOGGER = Logger.getLogger(RankingService.class.getName());

    public RankingService() {

    }

    @Override
    public void run() {
        LOGGER.log(Level.INFO,"Starting new run.");

        mCompetitionRankings = new HashMap<>();
        mAssetPrices = new HashMap<>();
        mCompetitions = new HashMap<>();
        mPlayers = new HashMap<>();

        try {
            getAllTransactions();
            getAllAssets();
            calculatePortfolioValues();
            calculateAndSaveRankings();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.log(Level.INFO,"Run completed.");
    }


    private void getAllTransactions() throws ParseException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(TRANSACTION_CLASS);
        mTransactions = query.find();
    }

    private void getAllAssets() {
        for (ParseObject transaction : mTransactions) {
            String ticker = transaction.getString(TICKER_FIELD);
            if (!mAssetPrices.containsKey(ticker)) {
                if (transaction.getString(ASSET_TYPE_FIELD).equals(CRYPTOCURRENCY_ASSET_TYPE)) {
                    Double price = AlphaVantageClient.getCurrentDigitalCurrencyPrice(ticker);
                    mAssetPrices.put(ticker, price);
                } else if (transaction.getString(ASSET_TYPE_FIELD).equals(STOCK_ASSET_TYPE)) {
                    Double price = AlphaVantageClient.getCurrentStockPrice(ticker);
                    mAssetPrices.put(ticker, price);
                }
            }
        }
    }

    private void calculatePortfolioValues() {
        for (ParseObject transaction : mTransactions) {
            ParseObject competition = (ParseObject) transaction.get(COMPETITION_FIELD);
            String competitionId = competition.getObjectId();
            mCompetitions.put(competitionId, competition);
            ParseObject player = (ParseObject) transaction.get(PLAYER_FIELD);
            String playerId = player.getObjectId();
            mPlayers.put(playerId, player);
            if (mCompetitionRankings.containsKey(competitionId)) {
                HashMap<String, Double> playersMap = mCompetitionRankings.get(competitionId);
                if (playersMap.containsKey(playerId)) {
                    Double value = playersMap.get(playerId);
                    value += getValue(transaction);
                    playersMap.put(playerId, value);
                } else {
                    Double value = getValue(transaction);
                    playersMap.put(playerId, value);
                }
                mCompetitionRankings.put(competitionId, playersMap);
            } else {
                HashMap<String, Double> playersMap = new HashMap<>();
                Double value = getValue(transaction);
                playersMap.put(playerId, value);
                mCompetitionRankings.put(competitionId, playersMap);
            }
        }
    }

    private Double getValue(ParseObject transaction) {
        Double value;
        if (transaction.getString(ASSET_TYPE_FIELD).equals(CASH_ASSET_TYPE)) {
            value = transaction.getDouble(PRICE_FIELD);
            LOGGER.log(Level.INFO,"Cash: " + value);
        } else if (transaction.getString(ACTION_FIELD).equals(BUY_ACTION)) {
            String ticker = transaction.getString(TICKER_FIELD);
            value = mAssetPrices.get(ticker) * transaction.getInt(UNITS_FIELD);
            LOGGER.log(Level.INFO,"Asset Buy: " + value + ", ticker: " + ticker + ", price: " +
                    mAssetPrices.get(ticker) + ", units: " + transaction.getInt(UNITS_FIELD));
        } else {
            String ticker = transaction.getString(TICKER_FIELD);
            value = transaction.getDouble(PRICE_FIELD) * transaction.getInt(UNITS_FIELD);
            LOGGER.log(Level.INFO,"Asset sell: " + value + ", ticker: " + ticker + ", price: " +
                    transaction.getDouble(PRICE_FIELD) + ", units: " + transaction.getInt(UNITS_FIELD));
        }
        return value;
    }

    private void calculateAndSaveRankings() throws ParseException {
        for (String competitionId : mCompetitionRankings.keySet()) {
            ParseObject competition = mCompetitions.get(competitionId);
            PriorityQueue<Portfolio> heap = new PriorityQueue();
            HashMap<String, Double> players = mCompetitionRankings.get(competitionId);
            for (String playerId : players.keySet()) {
                ParseObject player = mPlayers.get(playerId);
                Portfolio portfolio = new Portfolio(player, competition, players.get(playerId));
                heap.add(portfolio);
            }
            int rank = 1;
            while (heap.peek() != null) {
                Portfolio portfolio = heap.poll();
                portfolio.rank = rank;
                saveRanking(portfolio);
                rank += 1;
            }
        }
    }

    private void saveRanking(Portfolio portfolio) throws ParseException {
        if (!checkIfRankingExists(portfolio)) {
            ParseObject ranking = new ParseObject(RANKING_CLASS);
            ranking.put(RANKING_FIELD, portfolio.rank);
            ranking.put(PLAYER_FIELD, portfolio.player);
            ranking.put(COMPETITION_FIELD, portfolio.competition);
            ranking.put(PORTFOLIO_VALUE_FIELD, portfolio.value);
            ranking.save();
        }
    }

    private boolean checkIfRankingExists(Portfolio portfolio) throws ParseException {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(RANKING_CLASS);
        query.whereEqualTo(PLAYER_FIELD, portfolio.player);
        query.whereEqualTo(COMPETITION_FIELD, portfolio.competition);
        List<ParseObject> list = query.find();
        if (list.size() > 0) {
            ParseObject ranking = list.get(0);
            ranking.put(RANKING_FIELD, portfolio.rank);
            ranking.put(PORTFOLIO_VALUE_FIELD, portfolio.value);
            ranking.save();
            return true;
        }
        return false;
    }

}
