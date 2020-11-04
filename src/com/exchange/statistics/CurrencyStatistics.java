package com.exchange.statistics;

public class CurrencyStatistics {
    private int cntCurrencyUp = 0, cntCurrencyDown = 0;
    private double meanCurrencyValue = 0;
    private String symbol = "";
//class made to store all the info for a currency
    public CurrencyStatistics(int cntCurrencyUp, int cntCurrencyDown, double meanCurrencyValue, String symbol){
        this.cntCurrencyUp = cntCurrencyUp;
        this.cntCurrencyDown = cntCurrencyDown;
        this.meanCurrencyValue = meanCurrencyValue;
        this.symbol = symbol;
    }

    public int getCntCurrencyUp(){
        return cntCurrencyUp;
    }
    public int getCntCurrencyDown(){
        return cntCurrencyDown;
    }
    public double getMeanCurrencyValue(){
        return meanCurrencyValue;
    }
    public String getSymbol(){
        return symbol;
    }
}
