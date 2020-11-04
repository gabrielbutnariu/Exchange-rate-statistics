package com.exchange.statistics;

import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Plotter {
    private XYDataset dataSet;

    public void createDataset(Day[] x, double[] y, String title) {
        //this can be modified to get a list of series, so i can add as many series as i want
        //this can be used to add just one plot
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        TimeSeries series1 = new TimeSeries(title);
        for(int i = 0;i<y.length;i++){
            series1.add(x[i],y[i]);
        }
        dataset.addSeries(series1);
        this.dataSet = dataset;
    }

    public void createDataset(List<Map<Day[],double[]>> seriesList, List symbols, String base){
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        int j = 0;
        for (Map<Day[],double[]> map : seriesList){ //for every element in that list i create a series that can be added to graph
            TimeSeries series = new TimeSeries(symbols.get(j++).toString() + "/" + base);
            for (Map.Entry<Day[],double[]> entry : map.entrySet()) {
                for (int i = 0; i < entry.getValue().length; i++){
                    series.add(entry.getKey()[i],entry.getValue()[i]);
                }
            }
            dataset.addSeries(series); //adding all the series to the dataset
        }
        this.dataSet = dataset;
    }
    public XYDataset getDataSet(){
        return dataSet;
    }
}
