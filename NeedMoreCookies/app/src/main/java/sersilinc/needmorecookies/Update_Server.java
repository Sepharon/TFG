package sersilinc.needmorecookies;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


public class Update_Server extends Service {
    JSONObject DATA = null;
    JSONArray data_array = null;

    static final int MSG_GET_DATA = 1;
    static final String url = "https://www.tfg.centrethailam.com";

    private final IBinder mBinder = new LocalBinder();

    private String TAG = "Service: ";

    public class LocalBinder extends Binder {
        public Update_Server getService() {
            // Return this instance of LocalService so clients can call public methods
            return Update_Server.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.v("Service:", "Binding");
        return mBinder;
    }

    @Override
    public void onCreate (){
        super.onCreate();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        // f(total_countdown_time,tick_time)
        // 5 minutes, ticks every second
        if (!is_network_available()) {
            Toast.makeText(getBaseContext(), "No network available", Toast.LENGTH_LONG).show();
        }
        else {
            Log.v("Service: ", "Started countdown");
            new CountDownTimer(18000, 1000) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                   //Write function to be called
                    get_data();
                    start();
                }
                }.start();
            }
        }

    public void get_data(){
        int responseCode;

        String full_url = url;
        Log.v("Service:", "full url = " + full_url);

        try{
            Log.v("Service:" , "Creating connection");
            URL link_url = new URL(full_url);
            // Opening connection
            URLConnection connection = link_url.openConnection();
            Log.v("Service:" , "Trying connection");
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            // Checking if connection exists
            responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK){
                httpConnection.connect();
                Log.v("Service:", "Connection ok, getting input");
                InputStream stream = httpConnection.getInputStream();
                Log.v("Service:" , "Reading stream");
                // Read data
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

                String json = reader.readLine();
                Log.v("Service: ", json);
                DATA = new JSONObject(json);
                // Put the data in a JSONObject
                data_array = DATA.getJSONArray("first_name");
                Log.v("Service: ",data_array.toString());
                Log.v("Service: ", data_array.getString(0));

                /*Intent broadcast = new Intent();
                broadcast.setAction("miss_temps");
                broadcast.putExtra("data1", mainData.getString("temp"));
                broadcast.putExtra("data2", mainData.getString("temp_min"));

                sendBroadcast(broadcast);
                */
            }
            else {
                Log.v("Service: ", "city does not exist");
                Toast.makeText(getBaseContext(),"The city does not exist",Toast.LENGTH_LONG).show();
            }
        } catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public boolean is_network_available(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // Here write code for messages from activity (eg: city to get data from,
            // units desired celius or farenheid)
            switch (msg.what) {
                case MSG_GET_DATA:

                    Log.v("Service:", "Got data");
                    //result_city = msg.getData().getString("city");
                    //result_country = msg.getData().getString("country_code");
                    //temp_units = msg.getData().getString("unit");
                    Toast.makeText(getApplicationContext(), "Requesting weather's data", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
