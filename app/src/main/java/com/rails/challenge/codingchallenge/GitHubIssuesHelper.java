package com.rails.challenge.codingchallenge;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by adattatr on 4/24/2016.
 * Input :
 *        mApiString : the URL to fetch from
 *        mParams    : A mapping for the parameters to pass to
 *                    the URL.
 */
public class GitHubIssuesHelper {

    private String mApiString;
    private HashMap<String,String> mParams;

    GitHubIssuesHelper(String mApiString, HashMap<String,String> mParams){
        this.mApiString = mApiString;
        this.mParams = mParams;
    }

    private void createHttpUri() {
        if (mParams != null) {
            for (String key : mParams.keySet()) {
                mApiString += "&" + key + "=" + mParams.get(key);
            }
        }
    }

    public String downloadIssues()
    {
        String result = null;
        this.createHttpUri();
        try {
            URL urlToRead = new URL(mApiString);
                HttpURLConnection urlConnection = (HttpURLConnection) urlToRead.openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                bufferedReader.close();
                result = sb.toString();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
