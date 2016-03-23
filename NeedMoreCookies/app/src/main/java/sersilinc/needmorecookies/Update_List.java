package sersilinc.needmorecookies;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Messenger;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.Handler;
import android.os.Message;


public class Update_List extends Service {

    //URL
    static final String url = "https://www.tfg.centrethailam.com";

    //JSON objects and arrays
    JSONObject mainData = null;
    JSONObject JSONData = null;
    JSONArray data_array = null;

    //Handle incoming messages
    static final int MSG_GET_DATA = 1;
    static int responseCode;
    static HttpURLConnection urlConnection;
    static String result = null;

    //TAG for Log
    private final String TAG = "Update List Service: ";


    //Bind service
    private final IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        Update_List getService() {
            // Return this instance of LocalService so clients can call public methods
            return Update_List.this;
        }
    }

    //Incoming handler
    private Messenger msg = new Messenger(new IncomingHandler());


    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "Binding");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "Created");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        send_post_request();
        Log.v(TAG, "Started countdown");
        new CountDownTimer(18000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                //Write function to be called
                send_post_request();
                start();
            }
        }.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        Log.v(TAG,"StartCommand");
        return START_STICKY;
    }

    //It gets the JSON data from the API
    public void get_data_json(JSONObject json_obj) throws JSONException{
        mainData = json_obj.getJSONObject("main");
        Log.v(TAG, mainData.getString("id"));
    }


    private void send_post_request() {
        final String request = "{'main': {'id': 'quercusroses@gmail.com', 'Update': 'False', 'shared_list': 'False', 'all': 'True'}}";
        Log.v(TAG, "full url = " + url);

        // Create new thread so not to block URL
        Log.v(TAG, "Creating thread to send data");
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject json_obj = new JSONObject();
                    json_obj.put("main", "id");
                    // Opening connection
                    //Connect
                    urlConnection = (HttpURLConnection) ((new URL(url).openConnection()));
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setRequestMethod("POST");
                    urlConnection.connect();
                    //Write
                    OutputStream outputStream = urlConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    writer.write(json_obj.toString());
                    writer.close();
                    outputStream.close();
/*
                    //Read
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));

                    String line = null;
                    StringBuilder sb = new StringBuilder();

                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }

                    bufferedReader.close();
                    result = sb.toString();
                    Log.v(TAG, result+"");
*/
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }


    //It handles incoming messages
    static class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // Here write code for messages from activity (eg: city to get data from,
            // units desired celius or farenheid)
            switch (msg.what) {
                case MSG_GET_DATA:
                    Log.v("Service:", "Got data");
                    String msg_data = msg.getData().getString("city");
                    Log.v("Service:", msg_data);
                    //Toast.makeText(getApplicationContext(), "Requesting main data", Toast.LENGTH_SHORT).show();
                    //send_post_request();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
