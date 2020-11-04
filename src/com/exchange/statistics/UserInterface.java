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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.*;
import java.util.List;

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
    private JTextArea convertedArea;
    private JTextArea rawDataArea;
    private JRadioButton showFromSelectedYearRadioButton;
    private JList symbolsList;
    private JPanel statisticsContainer;
    private JTextArea statisticsArea;
    private XYPlot xyPlot;
    private JFreeChart chart;
    private ChartPanel chartPanel;
    private Plotter plotter = new Plotter();
    private JPanel currencyStatisticsPanel;
    private JScrollPane scrollPane;

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
                if((Integer.parseInt(yearStartComboBox.getSelectedItem().toString()) >= Integer.parseInt(yearStopComboBox.getSelectedItem().toString())) && !showFromSelectedYearRadioButton.isSelected()){
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
                    List<CurrencyStatistics> currencyStatistics = jsonParser.getCurrencyStatistics();
                    currencyStatisticsPanel = new JPanel(new GridLayout(currencyStatistics.size(), 0, 5, 5));
                    if(scrollPane != null){
                        statisticsContainer.remove(scrollPane);
                    }
                    scrollPane=new JScrollPane(
                            currencyStatisticsPanel,
                            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
                    );
                    for(CurrencyStatistics l : currencyStatistics){
                        JTextArea statisticsArea = new JTextArea(l.getSymbol()+"   ↑  "+ l.getCntCurrencyUp()+"    ↓  " + l.getCntCurrencyDown() + "   Mean:  "+ l.getMeanCurrencyValue());
                        currencyStatisticsPanel.add(statisticsArea);
                    }
                    scrollPane.setViewportView (currencyStatisticsPanel);
                    statisticsContainer.add(scrollPane);
                    containerPanel.revalidate();
                    SwingUtilities.updateComponentTreeUI(containerPanel);
                }
            }
        }));
    }
}
