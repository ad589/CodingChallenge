package com.rails.challenge.codingchallenge;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class CCActivity extends ListActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient mclient;

    private static final String TAG_COMMENTSURL = "comments_url";
    private static final String TAG_ISSUEBODY = "body";
    private static final String TAG_ISSUETITLE = "title";
    private static final String TAG_USER = "user";
    private static final String TAG_LOGIN = "login";
    private static final Integer MAX_DESC_LEN = 140;

    /*
     * When the actvity is created(onCreate) run an async task to get the open issues, in the
     * order they were updated.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mclient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        super.onCreate(savedInstanceState);

        ListView activeListView = getListView();
        new GetOpenIssues(this).execute();
        activeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position,
                                    long rowId) {
                String message;
                String commentsUrl;
                String[] asyncParam = new String[1];

                HashMap<String,String> listMap = (HashMap<String,String>)adapter.getItemAtPosition(position);
                if(listMap.get(TAG_COMMENTSURL) != null){
                    commentsUrl  = listMap.get(TAG_COMMENTSURL);
                }
                else {
                    Log.d("ONCLICK", "no key of type \"comments_url\" in the map\n");
                    return;
                }
                commentsUrl += "?";
                Log.d("ONCLICK", commentsUrl);
                asyncParam[0] = commentsUrl;
                /* Create an AsyncTask to fetch the comments from the
                 * comments_url, the download is performed on the aynctask
                 * thread and not the UI thread thus in case of slow networks
                 * the app will not go unresponsive. */
                new GetComments().execute(asyncParam);
            }
        });
    }

    /*
     * Called by onPostExecute of GetComments, to display the obtained comments
     * in a pop-up dialog.
     */
    private void dialogPopUp(String message) {
        final Dialog dialog = new Dialog(this);
        TextView comments_txt;

        dialog.setContentView(R.layout.dialog_comments);
        comments_txt = (TextView) dialog.findViewById(R.id.comments);

        if(message.length() == 0) {
            message = "No Comments\n";
        }
        Log.d("COMMENT", message);
        comments_txt.setText(message);
        comments_txt.setMovementMethod(new ScrollingMovementMethod());
        dialog.show();
    }
    /*
     * Called by onPostExecute of GetComments, parses the json response and
     * returns a string containing
     * commenter name1 : Comment1 <EOM>
     * commenter name2 : Comment2 <EOM> , where EOM is a delimiter signifying end of message.
     */
    private String parseJsonComments(String message) {
        JSONArray jsonArray;
        JSONObject jsonObject, user;
        String textMessage = "", body, username;

        try {
            //TODO : Check if the returned data is of type JSONArray or JsonObject
            //       Don;t assume.
            jsonArray = new JSONArray(message);
            for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    body = jsonObject.getString(TAG_ISSUEBODY);
                    user = jsonObject.getJSONObject(TAG_USER);
                    username = user.getString(TAG_LOGIN);

                    textMessage += username + ":" + body + "<EOM>\n";
            }
        }
        catch (JSONException jsonExc) {
            throw new RuntimeException(jsonExc);
        }
        return textMessage;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mclient.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "CC Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.rails.challenge.codingchallenge/http/host/path")
        );
        AppIndex.AppIndexApi.start(mclient, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "CC Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.rails.challenge.codingchallenge/http/host/path")
        );
        AppIndex.AppIndexApi.end(mclient, viewAction);
        mclient.disconnect();
    }


    /* GetOpenIssues runs in the background to retrieve
     * all open issues in the order they were updated.
     */

    private class GetOpenIssues extends AsyncTask<Void, Void, String> {

        private static final String GITHUB_ISSUES_API = "https://api.github.com/repos/rails/rails/issues?";
        private CCActivity mActivity;
        private ProgressDialog mProgressDiag;

        public GetOpenIssues(CCActivity activity) {
            super();
            mActivity = activity;
        }

        /* AsyncTask takes as input < Params, Progress, Return-Type>
         * onPreExecute is excuted on the UI thread and take no input
         * parameter
         */

        @Override
        protected void onPreExecute() {
            mProgressDiag = new ProgressDialog(this.mActivity);
            mProgressDiag.setMessage("Please wait while we find the open issues in rails/rails...");
            mProgressDiag.setCancelable(false);
            mProgressDiag.show();
        }

        /* doInBackground, which runs on a different thread and performs the over
         * the network operation getting the Http response.
         */
        @Override
        protected String doInBackground(Void... voids) {
            String result;
            /* parameters is a Hashmap containing the mapping from
             *  the API parameter and associated value.
             *  TODO We can use a Decorator design pattern to add the different parameters
             *  i.e decorate the parameters passed ( simple query + status + sort + ...
             */
            HashMap<String,String> parameters = new HashMap<String, String>();
            parameters.put("status", "open");
            parameters.put("sort", "updated");
            GitHubIssuesHelper downloadHelper = new GitHubIssuesHelper(GITHUB_ISSUES_API, parameters);
            try {
                /*
                 * Check for an internet connection.
                 */
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    result = downloadHelper.downloadIssues();
                    Log.d("RESULT", result);
                }
                else {
                    result = null;
                }
            } catch (Exception e) {
                return null;
            }
            return result;
        }

        private ArrayList<HashMap<String,String>> parseIssueJson(String result){
            String comments, title, body;
            JSONArray jsonArray;
            JSONObject jsonObject;
            ArrayList<HashMap<String,String>> issuesList = new ArrayList<HashMap<String, String>>();

            if (result != null) {
                if (result.length() == 0) {
                    return issuesList;
                }
            }
            else {
                HashMap<String,String> map = new HashMap<String, String>();
                map.put(TAG_ISSUETITLE,"Could not retrieve issue");
                map.put(TAG_ISSUEBODY, "Check internet connection or API url ");
                issuesList.add(map);
                return issuesList;
            }
            try {
                jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        // TODO handle the absence of these keys in jsonobject
                        title = jsonObject.getString(TAG_ISSUETITLE);
                        body = jsonObject.getString(TAG_ISSUEBODY);
                        comments = jsonObject.getString(TAG_COMMENTSURL);

                        HashMap<String,String> map = new HashMap<String, String>();
                        map.put(TAG_ISSUETITLE, title);
                        if(body.length() > MAX_DESC_LEN) {
                            body = body.substring(0, MAX_DESC_LEN - 1);
                        }
                        map.put(TAG_ISSUEBODY, body);
                        map.put(TAG_COMMENTSURL, comments);

                        issuesList.add(map);
                }
            }
            catch (JSONException jsonExc) {
                throw new RuntimeException(jsonExc);
            }
            return issuesList;
        }

        /*
         * onPostExecute is run after the background operation and it parses the json response data
         * and uses a simplelistadapter to bind the data to a listview
         * Since we dont need to update the listview after the app execution, go with the
         * SimpleAdapter solution. ArrayAdapter can be used to add issues to the list at runtime.
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ArrayList<HashMap<String,String>> issueList = parseIssueJson(result);

            String[] from = new String[] {TAG_ISSUETITLE, TAG_ISSUEBODY};
            int[] to = new int[] { R.id.issue_title, R.id.issue_description };
            ListAdapter adapter = new SimpleAdapter(CCActivity.this, issueList,R.layout.list_item, from, to );

            setListAdapter(adapter);
            if (mProgressDiag.isShowing())
                mProgressDiag.dismiss();
        }
    }


    /* GetComments runs in the background to retrieve
     * all comments for the ListItem which was clicked on.
     */
    private class GetComments extends AsyncTask<String, Void, String> {
        // Retrieve json data from comments url
        @Override
        protected String doInBackground(String... strings) {
            String commentsUrl = strings[0];
            String message;
            GitHubIssuesHelper commentsHelper = new GitHubIssuesHelper(commentsUrl, null);
            message = commentsHelper.downloadIssues();
            return message;
        }

        @Override
        protected void onPostExecute(String result){
            String textMessage;
            textMessage = parseJsonComments(result);
            dialogPopUp(textMessage);
        }
    }
}
