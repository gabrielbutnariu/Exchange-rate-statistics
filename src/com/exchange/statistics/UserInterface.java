package com.exchange.statistics;

import javax.swing.*;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.*;

public class UserInterface extends JFrame{
    private JPanel containerPanel;
    private JPanel currencyPanel;
    private JPanel graphPanel;
    private JComboBox yearStartComboBox;
    private JComboBox yearStopComboBox;
    private JLabel yearStartLabel;
    private JLabel yearStopLabel;
    private JComboBox baseComboBox;
    private JLabel Base;
    private JPanel rawDataPanel;
    private JButton convertCSVButton;
    private JButton convertJSONButton;
    private JTextArea convertedArea;
    private JTextArea rawDataArea;
    private JRadioButton showFromSelectedYearRadioButton;
    private JList symbolsList;
    private XYPlot xyPlot;
    private JFreeChart chart;
    private ChartPanel chartPanel;
    private Plotter plotter = new Plotter();

    public UserInterface(){
        super();
        init();
    }
    private void init(){
        this.setContentPane(containerPanel);
        this.setTitle("Exchange Rate Statistics");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        rawDataArea.setEditable(false);
        convertedArea.setEditable(false);
        populateCurrencyPanel();

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        NumberAxis numberAxis = new NumberAxis("Currency");
        numberAxis.setAutoRange(true);
        numberAxis.setAutoRangeIncludesZero(false);
        xyPlot = new XYPlot(plotter.getDataSet(),new DateAxis("Time"),numberAxis,renderer);
        chart = new JFreeChart("Exchange rate statistics",xyPlot);
        chartPanel = new ChartPanel(chart);
        graphPanel.add(chartPanel);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
    private void populateCurrencyPanel(){
        final String[] resultFromHttp = new String[1];

        rawDataArea.setLineWrap(true);
        convertedArea.setLineWrap(true);
        showFromSelectedYearRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                yearStopLabel.setVisible(!showFromSelectedYearRadioButton.isSelected());
                yearStopComboBox.setVisible(!showFromSelectedYearRadioButton.isSelected());
            }
        });

        for(int i = 1999; i <= Year.now().getValue(); i++){
            yearStartComboBox.addItem(i);
            yearStopComboBox.addItem(i);
        }

        currencyPanel.add(new JButton(new AbstractAction("Search") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(Integer.parseInt(yearStartComboBox.getSelectedItem().toString()) >= Integer.parseInt(yearStopComboBox.getSelectedItem().toString())){
                    JOptionPane.showMessageDialog(null,"Please select a proper combination for both years!");
                }else{
                    String symbols = "";
                    for (Object s : symbolsList.getSelectedValuesList()){
                        symbols += s.toString()+",";
                    }
                    System.out.println();
                    if(showFromSelectedYearRadioButton.isSelected()){
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        resultFromHttp[0] = HttpClient.getHtmlResponse(
                                "https://api.exchangeratesapi.io/history?start_at="+ yearStartComboBox.getSelectedItem()+"-01-01&end_at="+simpleDateFormat.format(new Date())+"&symbols="+symbols.substring(0,symbols.length() - 1)+"&base="+baseComboBox.getSelectedItem()+""
                        );
                    }else{
                        resultFromHttp[0] = HttpClient.getHtmlResponse(
                                "https://api.exchangeratesapi.io/history?start_at="+ yearStartComboBox.getSelectedItem()+"-01-01&end_at="+yearStopComboBox.getSelectedItem()+"-01-01&symbols="+symbols.substring(0,symbols.length() - 1)+"&base="+baseComboBox.getSelectedItem()+""
                        );
                    }
                    JsonParser jsonParser =  new JsonParser(resultFromHttp[0]); //create JSON object from GET request, here is all the data from api
                    try{
                        jsonParser.extractRatesFromJsonObject(); // extrat just the rated from json object
                    }catch (Exception ex){
                        JOptionPane.showMessageDialog(null,ex.getMessage());
                    }

                    try {
                        plotter.createDataset(jsonParser.getMap(symbols),symbolsList.getSelectedValuesList(),baseComboBox.getSelectedItem().toString());
                        //in getMap is created the entire dataset
                    } catch (ParseException parseException) {
                        parseException.printStackTrace();
                    }
                    xyPlot.setDataset(plotter.getDataSet());
                    rawDataArea.append(Arrays.toString(resultFromHttp));
                }
            }
        }));
    }
}
