package com.exchange.statistics;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpClient {
    private static URL url;
    private static HttpURLConnection connection;
    public static String getHtmlResponse(String urlToRead){
        StringBuffer result = new StringBuffer();
        try{
            url = new URL(urlToRead);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getResponseCode() / 100 == 2 ? connection.getInputStream() : connection.getErrorStream())); //verify the code from html headers
            String line;
            while ((line = bufferedReader.readLine()) != null){
                result.append(line);
            }
            bufferedReader.close();
        }catch (Exception e){
            JOptionPane.showMessageDialog(null,e.getMessage());
        }
        return result.toString();
    }
}
