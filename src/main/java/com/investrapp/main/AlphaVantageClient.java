package com.investrapp.main;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AlphaVantageClient {

    //Alpha Avantage API Parameters
    public static final String FUNCTION = "function";
    public static final String SYMBOL = "symbol";
    public static final String INTERVAL = "interval";
    public static final String API_KEY = "apikey";
    public static final String MARKET = "market";

    //Alpha Avantage API Parameter Values
    public static final String USD = "USD";
    public static final String TIME_SERIES_INTRADAY = "TIME_SERIES_INTRADAY";
    public static final String DIGITAL_CURRENCY_INTRADAY = "DIGITAL_CURRENCY_INTRADAY";
    public static final String PRICE_INTERVAL_ONE_MIN = "1min";
    public static final String ALPHA_VANTAGE_URL = "https://www.alphavantage.co/query";
    public static String ALPHA_VANTAGE_API_KEY = "9D8E0KIYHAUBX0KV";

    private final static Logger LOGGER = Logger.getLogger(RankingService.class.getName());

    public AlphaVantageClient() {

    }

    public static Double getCurrentStockPrice(String symbol) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(ALPHA_VANTAGE_URL).newBuilder();
        urlBuilder.addQueryParameter(API_KEY, ALPHA_VANTAGE_API_KEY);
        urlBuilder.addQueryParameter(FUNCTION, TIME_SERIES_INTRADAY);
        urlBuilder.addQueryParameter(INTERVAL, PRICE_INTERVAL_ONE_MIN);
        urlBuilder.addQueryParameter(SYMBOL, symbol);

        String url = urlBuilder.build().toString();
        LOGGER.log(Level.INFO, "Trying: " + url);
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String responseData = response.body().string();
            JSONObject jsonObject = new JSONObject(responseData);
            JSONObject timeSeries = jsonObject.getJSONObject("Time Series (1min)");
            JSONObject timeData = timeSeries.getJSONObject((String) timeSeries.keys().next());
            Double price = timeData.getDouble("1a. price (USD)");
            return price;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public static Double getCurrentDigitalCurrencyPrice(String ticker) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(ALPHA_VANTAGE_URL).newBuilder();
        urlBuilder.addQueryParameter(API_KEY, ALPHA_VANTAGE_API_KEY);
        urlBuilder.addQueryParameter(FUNCTION, DIGITAL_CURRENCY_INTRADAY);
        urlBuilder.addQueryParameter(MARKET, USD);
        urlBuilder.addQueryParameter(INTERVAL, PRICE_INTERVAL_ONE_MIN);
        urlBuilder.addQueryParameter(SYMBOL, ticker);

        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(url)
                .build();
        LOGGER.log(Level.INFO, "Trying: " + url);
        try {
            Response response = client.newCall(request).execute();
            String responseData = response.body().string();
            JSONObject jsonObject = new JSONObject(responseData);
            JSONObject timeSeries = jsonObject.getJSONObject("Time Series (Digital Currency Intraday)");
            JSONObject timeData = timeSeries.getJSONObject((String) timeSeries.keys().next());
            Double price = timeData.getDouble("1a. price (USD)");
            return price;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

}
