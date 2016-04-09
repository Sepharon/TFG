package sersilinc.needmorecookies;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.Messenger;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import android.os.Handler;
import android.os.Message;

import javax.net.ssl.HttpsURLConnection;


public class Update_List extends Service {

    //URL
    private static final String url = "https://www.tfg.centrethailam.com";

    //JSON objects and arrays
    private JSONObject mainData = null;


    //Handle incoming messages
    static final int MSG_GET_DATA = 1;
    private static HttpsURLConnection urlConnection;

    //TAG for Logs
    private final String TAG = "Update List Service: ";

    //Bind service
    private final IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        Update_List getService() {
            // Return this instance of LocalService so clients can call public methods
            return Update_List.this;
        }
    }

    //Incoming messages handler
    private Messenger msg = new Messenger(new IncomingHandler());


    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "Binding");
        return msg.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Log.v(TAG, "Created");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        Log.v(TAG, "StartCommand");
        return START_STICKY;
    }

    //It gets the JSON data from the API
    private void get_data_json(JSONObject json_obj, String request, String source) throws JSONException{
        JSONObject mainJSON = json_obj.getJSONObject("main");
        Intent broadcast = new Intent();
        broadcast.setAction("broadcast_service");

        broadcast.putExtra("Main", mainJSON.toString());

        //Process type of request and broadcast it
        switch (request){
            case "all":
                JSONObject listsJSON = json_obj.getJSONObject("lists");
                broadcast.putExtra("all", listsJSON.toString());
                break;
            case "one_list":
                JSONObject listJSON = json_obj.getJSONObject("list");
                switch(source){
                    case "Items":
                        broadcast.putExtra("Update_Products", "True");
                        break;
                    case "MainActivity":
                        broadcast.putExtra("Update_Products", "False");
                        break;
                }

                broadcast.putExtra("One_list", listJSON.toString());
                break;
            case "shared_list":
                JSONObject shared_list = json_obj.getJSONObject("list");
                broadcast.putExtra("shared_list", shared_list.toString());
                break;
            case "share":
                String result = mainJSON.getString("Result");
                broadcast.putExtra("result", result);
                break;
        }
        broadcast.putExtra("Request", request);
        sendBroadcast(broadcast);
    }


    private void send_post_request(final String request, final String GoogleAccount, final String code_name, final String Friend, final String source) {
        //Log.v(TAG, "full url = " + url);

        // Create new thread so not to block URL
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result;
                    JSONObject json_obj = new JSONObject();

                    //Process type of request. Depending on the value, we create a different JSON object.
                    switch (request) {
                        case "all":
                            json_obj.put("Request", "Update Android");
                            json_obj.put("shared_list", "False");
                            json_obj.put("request_code", "False");
                            json_obj.put("all", "True");
                            json_obj.put("GoogleAccount", GoogleAccount);
                            break;
                        case "shared_list":
                            json_obj.put("Request", "Update Android");
                            json_obj.put("shared_list", "True");
                            json_obj.put("request_code", "False");
                            json_obj.put("all", "False");
                            json_obj.put("Code", code_name);
                            json_obj.put("GoogleAccount", GoogleAccount);
                            break;
                        case "code":
                            json_obj.put("Request", "Update Android");
                            json_obj.put("shared_list", "True");
                            json_obj.put("request_code", "True");
                            json_obj.put("all", "False");
                            json_obj.put("list_name", code_name);
                            json_obj.put("GoogleAccount", GoogleAccount);
                            break;
                        case "one_list":
                            json_obj.put("Request", "Update Android");
                            json_obj.put("shared_list", "False");
                            json_obj.put("request_code", "False");
                            json_obj.put("all", "False");
                            json_obj.put("Code", code_name);
                            json_obj.put("GoogleAccount", GoogleAccount);
                            break;
                        case "share":
                            json_obj.put("Request", "Share List");
                            json_obj.put("Code", code_name);
                            json_obj.put("GoogleAccount", GoogleAccount);
                            json_obj.put("Friend", Friend);
                            break;
                    }

                    JSONObject mainObj = new JSONObject();
                    mainObj.put("main", json_obj);

                    //Log.v(TAG, mainObj.toString());

                    URL link_url = new URL(url);
                    urlConnection = (HttpsURLConnection) link_url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    //urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setReadTimeout(5000);
                    urlConnection.connect();
                    Log.v(TAG, "Connected");

                    //Write
                    OutputStream outputStream = urlConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    writer.write(mainObj.toString());
                    writer.flush();
                    writer.close();
                    outputStream.close();
                    Log.v(TAG, "Sent");

                    //Read
                    InputStream stream = urlConnection.getInputStream();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

                    String line;
                    StringBuilder sb = new StringBuilder();

                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }

                    bufferedReader.close();
                    result = sb.toString();
                    Log.v(TAG, result + "");

                    try {
                        mainData = new JSONObject(result);
                        get_data_json(mainData, request, source);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                } catch (MalformedURLException e) {
                    Log.v(TAG, "Malformed");
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.v(TAG, "IOException");
                    e.printStackTrace();
                } catch (JSONException e) {
                    Log.v(TAG, "JSONException");
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                        Log.v(TAG, "Disconnected");
                    }
                }
            }
        });
        t.start();
    }

    //Check if network available
    private boolean is_network_available(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //It handles incoming messages
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (!is_network_available()) {
                Toast.makeText(getBaseContext(), "No network available", Toast.LENGTH_LONG).show();
            } else {
                switch (msg.what) {
                    case MSG_GET_DATA:
                        Log.v("Service:", "Got data");
                        String request = msg.getData().getString("request");
                        String GoogleAccount = msg.getData().getString("GoogleAccount");
                        Log.v("Service:", request);
                        try {
                            //Process type of request and send POST request
                            switch (request) {
                                case "one_list":
                                    String source = msg.getData().getString("Activity");
                                    String code_list = msg.getData().getString("code_list");
                                    send_post_request(request, GoogleAccount, code_list, "", source);
                                    break;
                                case "shared_list":
                                    String code_list2 = msg.getData().getString("code_list");
                                    send_post_request(request, GoogleAccount, code_list2, "", null);
                                    break;
                                case "code":
                                    String list_name = msg.getData().getString("list_name");
                                    send_post_request(request, GoogleAccount, list_name, "", null);
                                    break;
                                case "all":
                                    send_post_request(request, GoogleAccount, null, "", null);
                                    break;
                                case "share":
                                    String code_list3 = msg.getData().getString("code_list");
                                    String friend = msg.getData().getString("Friend");
                                    send_post_request(request, GoogleAccount, code_list3, friend, null);
                                    break;
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        }
    }
}