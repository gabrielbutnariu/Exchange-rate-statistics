package com.exchange.statistics;

import org.jfree.data.time.Day;
import org.json.JSONObject;

import javax.swing.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class JsonParser {
    private String data;
    private JSONObject jsonObject;
    private Map<String,String> jsonRatesToMap;
    private Day[] timeStamp;
    private double[] currencyValues;
    private int currencyUp = 0, currencDown = 0;
    private double meanValueCurrency = 0;
    private List<CurrencyStatistics> currencyStatistics = new ArrayList<>();

    public JsonParser(String data){
        this.data = data;
        this.jsonObject = new JSONObject(data);
    }
    public void extractRatesFromJsonObject(){
      try{
          JSONObject ratesJson = new JSONObject(jsonObject.getJSONObject("rates").toString());
          jsonRatesToMap =  new TreeMap<>(); //to sort all data by date(key)
          Iterator<String> keyItr = ratesJson.keys();
          while (keyItr.hasNext()){
              String key = keyItr.next();
              Object value = ratesJson.get(key);
              jsonRatesToMap.put(key,value.toString());
          }
      }catch (Exception e){
          JOptionPane.showMessageDialog(null, jsonObject.getString("error"));
      }

    }
    public void getCurrencyDate() throws ParseException {
        Set<String> keys = new TreeMap<>(jsonRatesToMap).keySet();
        Day[] keysFromMap = new Day[keys.size()];
        int i = 0;
        for(String key: keys){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = simpleDateFormat.parse(key);
            keysFromMap[i++] = new Day(date);
        }
        timeStamp = keysFromMap;
    }
    public List<double[]> getCurrencyValues(String symbols){
        Map<String,String> map = new TreeMap<>(jsonRatesToMap);
        Set<String> keys = new TreeMap<>(jsonRatesToMap).keySet();

        List<double[]> valuesFromRateMap =  new ArrayList<>(); //here will be saved all the double[] for every currency
        for (String currency : splitString(symbols,",")){ //for every currency selected
            double[] valuesForCurrency = new double[keys.size()]; //here will be saved all the data for a specific currency
            int i = 0;
            currencyUp = currencDown = 0;
            meanValueCurrency = 0;
            double aux = 0;
            for (String year : keys){ //every currency its linked with the same date, so i'm adding them by key
                JSONObject currencyValues = new JSONObject(map.get(year));
                if (aux > (valuesForCurrency[i++] = Double.parseDouble(currencyValues.get(currency).toString()))) {
                    currencyUp++;
                }else{
                    currencDown++;
                }
                aux = valuesForCurrency[i-1];
                meanValueCurrency += aux;

            }
            meanValueCurrency = meanValueCurrency/i;
            valuesFromRateMap.add(valuesForCurrency);
            currencyStatistics.add(new CurrencyStatistics(currencyUp,currencDown,meanValueCurrency,currency));
        }
        return valuesFromRateMap;
    }


    public List<Map<Day[],double[]>> getMap(String symbols) throws ParseException {
        List<Map<Day[],double[]>> seriesList = new ArrayList<>();
        getCurrencyDate();
        System.out.println();
        List<double[]> currencyValues = getCurrencyValues(symbols);
        for (int i = 0; i < currencyValues.size(); i++){
            Map<Day[], double[]> dataSet = new HashMap<>();
            dataSet.put(timeStamp,currencyValues.get(i));
            seriesList.add(dataSet);
        }
        return seriesList;
    }

    public String[] splitString(String key,String regex){
        String[] splitString = key.split(regex);
        return splitString;
        //System.out.println(splitString);
    }

    public List<CurrencyStatistics> getCurrencyStatistics(){
        return currencyStatistics;
    }
}
