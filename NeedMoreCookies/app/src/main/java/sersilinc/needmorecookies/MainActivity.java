package sersilinc.needmorecookies;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Main tag for Logs
    private final String TAG = "Shopping_Lists: ";

    // Service elements
    private Messenger mService = null;
    private boolean is_bound = false;
    private Update_Server server_service;
    private boolean is_bound_server = false;

    //Receiver
    private String request_type;
    private String list;
    private String main;
    private String update_product;
    public MyReceiver receiver;
    private IntentFilter filter;

    // UI elements
    private Button private_lists;
    private Button public_lists;
    private View separator1;
    private View separator2;
    private TextView welcome;
    private ProgressBar loading;
    private View first_layout;
    private View third_layout;
    private AdView mAdView;

    //Lists
    private String old_codes;

    // ListView
    private ListView listview;
    // Private and public list names
    private ArrayList<HashMap<String, String>> private_list = new ArrayList<>();
    private ArrayList<HashMap<String, String>> public_list = new ArrayList<>();
    HashMap<String, String> temp;
    //Columns
    private static final String FIRST_COLUMN = "First";
    private static final String SECOND_COLUMN = "Second";

    // Adapter
    private ListViewAdapters adapter;

    //Google API client
    private GoogleApiClient mGoogleApiClient;

    //User info
    private User_Info usr_inf;
    private String list_type = "";
    //Timer
    private CountDownTimer timer,timer2;
    // Selected private or public tab
    private boolean is_private_selected = true;
    //Selected shopping list
    private int currentSelection;

    //Database
    DB_Helper db;

    // Ads
    private InterstitialAd mInterstitialAd;

    //GCM
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_main);
        /**[START Intent-filter for receiving Broadcast]**/
        filter = new IntentFilter("broadcast_service");
        receiver = new MyReceiver();
        this.registerReceiver(receiver, filter);
        /**[END Intent-filter for receiving Broadcast]**/

        /**[START Bind service Update List]**/
        Intent intent = new Intent(this, Update_Android.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        /**[END Bind service Update List]**/

        /**[START Bind service Update Server]**/
        Intent in = new Intent(this, Update_Server.class);
        bindService(in, mConnection2, Context.BIND_AUTO_CREATE);
        /**[END Bind service Update Server]**/

        /**[START UI elements]**/
        private_lists = (Button) findViewById(R.id.private_lists);
        public_lists = (Button) findViewById(R.id.public_lists);
        separator1 = findViewById(R.id.separator);
        separator2 = findViewById(R.id.separator2);
        listview = (ListView) findViewById(R.id.list);
        welcome = (TextView) findViewById(R.id.welcome_text);
        loading = (ProgressBar) findViewById(R.id.progressBar);
        first_layout = findViewById(R.id.firstLayout);
        third_layout = findViewById(R.id.thirdLayout);
        /**[END UI elements]**/

        /**[START DataBase]**/
        db = new DB_Helper(getApplicationContext());
        /**[END DataBase]**/

        /**[START List view]**/
        adapter = new ListViewAdapters(this, private_list, "MainActivity", list_type);

        listview.setAdapter(adapter);
        /**[END List view]**/

        /**[START Navigation]**/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            assert toolbar != null;
            toolbar.setElevation(25);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);
        /**[END Navigation]**/

        /**[START AddList call]**/
        //Add new list
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddList.class);
                // Start next activity
                startActivityForResult(intent, 1);
            }
        });
        /**[END AddList call]**/

        /**[START GoogleApiClient]**/
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        /**[END GoogleApiClient]**/

        /** [START Advertisements] **/
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        /** [END Advertisements] **/

        /**[START OnClickListeners]**/
        //Change to private or public view
        private_lists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (separator1.getVisibility() != View.VISIBLE) reload_ui(true);

            }
        });
        public_lists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (separator2.getVisibility() != View.VISIBLE) reload_ui(false);
            }
        });

        //Show the products of the selected shopping list in a new activity
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object shop_list = adapter.getItem(position);
                String selected = ((HashMap) shop_list).get(FIRST_COLUMN).toString();
                if (is_bound) {
                    if (!usr_inf.getOffline_mode()) {
                        // Create and send a message to the service, using a supported 'what' value
                        Log.v(TAG, "Getting ready");
                        Message msg = Message.obtain(null, Update_Android.MSG_GET_DATA);
                        Bundle bundle = new Bundle();
                        bundle.putString("request", "one_list");
                        bundle.putString("Activity", "MainActivity");

                        // Get the code of the list from the DB
                        Log.v(TAG, "Code: " + db.read_code(selected));
                        bundle.putString("code_list", db.read_code(selected));
                        bundle.putString("GoogleAccount", usr_inf.getEmail());
                        msg.setData(bundle);
                        //Send message
                        try {
                            mService.send(msg);
                            //Log.v(TAG, "Message sent");
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        Intent in = new Intent(MainActivity.this, Items.class);
                        in.putExtra("Code", db.read_code(selected));
                        in.putExtra("Type", list_type);
                        startActivity(in);
                    }
                }
            }
        });

        //Show option to edit or delete if long press
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("Long click");
                currentSelection = position;
                startActionMode(modeCallBack);
                view.setSelected(true);
                return true;
            }
        });
        /**[END OnClickListeners]**/

        /**[START User_Info]**/
        //Get User info
        usr_inf = User_Info.getInstance();
        /**[END User_Info]**/

        /**[START Counter]**/
        //Counter to reload the MainActivity every 2 minutes
        timer = new CountDownTimer(120000, 1000) { //2min
            public void onTick(long millisUntilFinished) {}
            public void onFinish() {
                if (!usr_inf.getOffline_mode())
                    getAll_ShoppingLists(usr_inf.getEmail());
                start();
            }
        }.start();
        if (usr_inf.getOffline_mode()){
            // every minute
            Log.v(TAG,"Starting offline counter");
            timer2 = new CountDownTimer(30000,1000) {
                @Override
                public void onTick(long millisUntilFinished) {}
                @Override
                public void onFinish() {
                    if (is_network_available()) {
                        Log.d(TAG,"Internet back");
                        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                        alert.setTitle(R.string.go_online_alert);
                        alert.setMessage(R.string.go_online_question);
                        alert.setPositiveButton(android.R.string.yes,new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog,int which){
                                Toast.makeText(MainActivity.this,R.string.shutting_down,Toast.LENGTH_SHORT).show();
                                Intent i = getBaseContext().getPackageManager()
                                        .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                finish();
                                startActivity(i);
                                System.exit(0);
                            }
                        });
                        alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which){
                                timer2.cancel();
                            }
                        });
                        alert.show();
                    }
                    else {
                        Log.d(TAG, "Internet back");
                        usr_inf.setOffline_mode(true);
                        // Start timer again
                        start();
                    }
                }
            }.start();
        }
        /**[END Counter]**/

        //GCM
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                sharedPreferences.getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
            }
        };
        // Registering BroadcastReceiver
        registerReceiver();
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent2 = new Intent(this, RegistrationIntentService.class);
            startService(intent2);
        }
        if (!isXLargeTablet(getApplicationContext())){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        reload_ui(is_private_selected);
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
        unregisterReceiver(receiver);
        if (timer2 != null) timer2.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Restart timer
        timer.start();
        if (timer2 != null && usr_inf.getOffline_mode()) timer2.start();
        //Register receiver
        registerReceiver(receiver, filter);
        registerReceiver();
        db = new DB_Helper(getApplicationContext());
        //Get shopping lists
        if (!usr_inf.getOffline_mode())
            getAll_ShoppingLists(usr_inf.getEmail());
        reload_ui(is_private_selected);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
    }

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unbind from the services
        if (is_bound) {
            unbindService(mConnection);
            is_bound = false;
        }
        if (is_bound_server) {
            unbindService(mConnection2);
            is_bound_server = false;
        }

        db.destroy_class();
        timer.cancel();
        if (timer2 != null) timer2.cancel();
    }

    // Binding Update Android
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.v(TAG, "Binding service");
            mService = new Messenger(service);
            is_bound = true;
            //Get the shopping lists and displaying a loading progress circle
            new ProgressTask().execute();
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.v(TAG,"Update List disconnected");
            mService = null;
            is_bound = false;
        }
    };

    // Binding Update Server
    private ServiceConnection mConnection2 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.v(TAG, "Binding service");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            Update_Server.LocalBinder binder = (Update_Server.LocalBinder) service;
            server_service = binder.getService();
            is_bound_server = true;
            if (!usr_inf.getOffline_mode())
                send_unsynced_entries();
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            is_bound_server = false;
        }
    };

    //Receiver from Services
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            request_type = intent.getStringExtra("Request");
            main = intent.getStringExtra("Main");
            Log.v(TAG,"Request: "+ request_type);
            //Check type of request
            switch(request_type){
                case "one_list":
                    update_product = intent.getStringExtra("Update_Products");
                    if (!update_product.equals("True")) {
                        list = intent.getStringExtra("One_list");
                        Log.v(TAG, list);
                        changeActivity(main, list);
                    }
                    break;
                case "all":
                    Log.v(TAG,"Getting lists");
                    list = intent.getStringExtra("all");
                    public_list.clear();
                    private_list.clear();
                    adapter.notifyDataSetChanged();
                    update_Users_data(list);
                    break;
                case "new_list":
                    if (main.equals("False"))
                        Toast.makeText(MainActivity.this, R.string.add_list_error,Toast.LENGTH_SHORT)
                                .show();
                    else {
                        Log.v(TAG,"adding new code: " + main);
                        Log.v(TAG,"old_code: " + old_codes);
                        db.update_list_code(main,old_codes);
                        try {
                            db.delete_list(old_codes);
                        } catch (android.database.CursorIndexOutOfBoundsException e){
                            e.printStackTrace();
                        }
                        db.set_list_flag(main,0);
                        print_db();
                        new ProgressTask_Back().execute();
                        reload_ui(is_private_selected);
                        Log.v(TAG, "Added new Shopping List correctly");
                    }
                    break;
                case "change_list_name":
                    if (main.equals("False"))
                        Toast.makeText(MainActivity.this, R.string.change_list_name_error,Toast.LENGTH_SHORT)
                                .show();
                    else {
                        Toast.makeText(MainActivity.this, "Shopping List name changed", Toast.LENGTH_SHORT).show();
                        new ProgressTask_Back().execute();
                        Log.v(TAG, "Name changed correctly");
                    }
                    break;
                case "add_usr_to_list":
                    if (main.equals("False")){
                        Toast.makeText(MainActivity.this, "The email you have entered does not belong to an existing user", Toast.LENGTH_SHORT).show();
                    } else{
                        Log.v(TAG,"Shared list");
                        Toast.makeText(MainActivity.this, "Shopping list shared", Toast.LENGTH_SHORT).show();
                        new ProgressTask_Back().execute();
                    }
                    break;
                case "delete_list":
                    if (main.equals("False"))
                        Toast.makeText(MainActivity.this,R.string.delete_list_error,Toast.LENGTH_SHORT)
                                .show();
                    else {
                        Toast.makeText(MainActivity.this, "Shopping list deleted", Toast.LENGTH_SHORT).show();
                        new ProgressTask_Back().execute();
                    }
                    break;
                case "Token":
                    String token = intent.getStringExtra("Token");
                    send_request_server("_", "_", "add_token", "_", token);
                    Log.v(TAG, "TOKEN: "+token);
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finish();
            super.onBackPressed();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        public_list.clear();
        private_list.clear();
        adapter.notifyDataSetChanged();
        switch (item.getItemId()) {
            case R.id.action_update:
                if (!usr_inf.getOffline_mode()) {
                    Log.v(TAG,"update online");
                    Log.v(TAG,"" + usr_inf.getOffline_mode());
                    usr_inf.setOffline_mode(false);
                    getAll_ShoppingLists(usr_inf.getEmail());
                    Toast.makeText(MainActivity.this,R.string.update,Toast.LENGTH_SHORT).show();
                } else {
                    Log.v(TAG,"update offline");
                    Toast.makeText(getBaseContext(), R.string.offline_update, Toast.LENGTH_SHORT).show();
                    Toast.makeText(getBaseContext(),R.string.offline_update_warning,Toast.LENGTH_SHORT).show();
                    read_from_internal_DB();
                    reload_ui(is_private_selected);
                }
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    //Navigation
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_locations) {
            Intent intent = new Intent(MainActivity.this,MapsActivity.class);
            // Start next activity
            startActivity(intent);
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            // Start next activity
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            Log.d(TAG,"Sharing shopping");
            Intent mail_intent = new Intent(Intent.ACTION_SEND);
            mail_intent.setType("message/rfc822");
            // Body of mail
            mail_intent.putExtra(Intent.EXTRA_SUBJECT,"Try Need More Cookies!");
            mail_intent.putExtra(Intent.EXTRA_TEXT,"I invite you to try this awesome app! You will be able to write and share shopping lists " +
                    "with your friends! \nDownload it here: test.com \nYour friend: " + usr_inf.getName());
            Intent final_intent = Intent.createChooser(mail_intent,"Choose mail client");
            final_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // Start Mail chooser
            startActivity(final_intent);

        } else if (id == R.id.nav_logout) {
            signOut();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Sign Out from Google Account
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                Intent intent = new Intent(MainActivity.this, Login.class);
                // Start next activity
                startActivity(intent);
                finish();
            }
        });
    }

    //Get result from AddList activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                String list_name = data.getStringExtra("List_Name");
                String Type = data.getStringExtra("Type");
                //Check which type of list the user wants to add
                switch (Type){
                    // private
                    case "true":
                        if (usr_inf.getOffline_mode())
                            old_codes =db.add_new_list(list_name,1);
                        Log.v(TAG,"name: " + list_name);
                        if (!usr_inf.getOffline_mode())
                            send_request_server(list_name, "1", "new_list", "", "");
                        reload_ui(true);
                        break;
                    // public
                    case "false":
                        if (usr_inf.getOffline_mode())
                            old_codes = db.add_new_list(list_name,0);
                        reload_ui(false);
                        if (!usr_inf.getOffline_mode())
                            send_request_server(list_name, "0", "new_list","", "");
                        break;
                }
                if (usr_inf.getOffline_mode()){
                    public_list.clear();
                    private_list.clear();
                    adapter.notifyDataSetChanged();
                    read_from_internal_DB();
                }
            }
        }
    }

    //Send request to Update Server service
    private void send_request_server(String list_name,String is_private, final String Objective, String code, String set_value){
        Log.v(TAG,"objective: " + server_service.get_objective(Objective));
        server_service.set_values(server_service.get_objective(Objective), code, list_name, "True", is_private);
        server_service.set_items("_", "_", set_value, "_", "_");
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                server_service.send_request();
                //noinspection StatementWithEmptyBody
                while (!server_service.return_response_status());
                String response = server_service.return_result();
                Log.v("Thread", response);
                Intent intent = new Intent();
                intent.setAction("broadcast_service");
                intent.putExtra("Main", response);
                intent.putExtra("Request", Objective);
                sendBroadcast(intent);
            }
        });
        t.start();
    }

    //Change the UI either private or public shopping lists
    private void reload_ui(Boolean type){
        if (type){
            is_private_selected = true;
            list_type="1";
            adapter = new ListViewAdapters(this, private_list, "MainActivity", list_type);
            separator1.setVisibility(View.VISIBLE);
            separator2.setVisibility(View.INVISIBLE);
        }
        else {
            is_private_selected = false;
            list_type="0";
            adapter = new ListViewAdapters(this, public_list, "MainActivity", list_type);
            separator2.setVisibility(View.VISIBLE);
            separator1.setVisibility(View.INVISIBLE);
        }
        // Create listview
        listview.setAdapter(adapter);
    }

    //When a shopping lists is pressed, change to Items activity and send the items
    private void changeActivity(String main, String list){
        Log.v(TAG,main);
        Intent intent = new Intent(this, Items.class);
        intent.putExtra("Main", main);
        intent.putExtra("List", list);
        intent.putExtra("Type", list_type);
        startActivity(intent);
    }

    private void getAll_ShoppingLists(String GoogleAccount){
        if (is_bound) {
            // Create and send a message to the service, using a supported 'what' value
            Message msg = Message.obtain(null, Update_Android.MSG_GET_DATA);
            Bundle bundle = new Bundle();
            bundle.putString("request", "all");
            bundle.putString("GoogleAccount", GoogleAccount);
            msg.setData(bundle);
            //Send message
            try {
                mService.send(msg);
                Log.v(TAG, "Message sent");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    //Update the UI with all the shopping lists
    private void update_Users_data(String result){
        try {
            boolean name_change_flag = false;

            public_list.clear();
            private_list.clear();
            adapter.notifyDataSetChanged();
            Log.v(TAG,"UPDATE_USR");
            JSONObject json_obj = new JSONObject(result);
            Iterator<String> keys = json_obj.keys();
            List<String> c = new ArrayList<>();
            while (keys.hasNext()) {
                //Get list name
                String list_name = String.valueOf(keys.next());
                JSONObject list1 = json_obj.getJSONObject(list_name);
                //Get the code
                String code = list1.getString("Code");
                //Get timestamp
                String timestamp = list1.getString("Timestamp");
                int type = list1.getInt("TypeList");
                //Check type of shopping list and store them in the User_Info class
                //The format is the following: [[List_name, Type, Code, Timestamp], [List_name2, Type, Code, Timestamp],...]
                c.add(code);
                Log.v(TAG,"List name: " + list_name);
                switch (type) {
                    case 0:
                        print_db();
                       // If server timestamp is newer than ours update name
                        if (db.read_shopping_list(0,code).equals("Error")) {
                            Log.v(TAG,"Item not found. Adding to DB");
                            Log.v(TAG,"List name: " + list_name);
                            String old_code = db.add_new_list(list_name, 0);
                            db.set_list_flag(code,0);
                            db.update_list_code(code,old_code);
                        }
                        print_db();
                        break;
                    case 1:
                        // If list not in DB insert it (might be added by someone else)
                        if (db.read_shopping_list(0,code).equals("Error")) {
                            Log.v(TAG,"Item not found. Adding to DB");
                            Log.v(TAG,"List name: " + list_name);
                            String old_code = db.add_new_list(list_name, 1);
                            db.set_list_flag(code,0);
                            db.update_list_code(code,old_code);
                        }
                        break;
                }
                if (!db.read_shopping_list(1,code).equals(list_name)) {
                    db.update_list_name(list_name, code);
                    name_change_flag = true;
                }
                db.update_timestamp_server(code, timestamp);
                db.set_list_flag(code,0);
            }
            if (name_change_flag)
                Toast.makeText(MainActivity.this,R.string.external_name_change,Toast.LENGTH_SHORT).show();
            remove_deleted_SL(c);

        } catch (JSONException e){
            e.printStackTrace();
        }
        read_from_internal_DB();
        reload_ui(is_private_selected);
    }

    private void read_from_internal_DB(){
        Log.d(TAG,"Reading from internal DB");
        List<String> shopping_list_private = new ArrayList<>();
        List<String> shopping_list_public = new ArrayList<>();
        List<String[]> a = db.read_all_lists();
        if (a!=null) {
            for (int i = 0; i < a.size(); i++) {
                String[] b = a.get(i);
                if (a.get(i)[3].equals("1")) {
                    shopping_list_private.add(b[0]); // listname
                    shopping_list_private.add(b[3]); // Private
                    shopping_list_private.add(b[2]); // code
                    shopping_list_private.add(b[1]); // timestamp
                    if (!private_list.contains(b[0])) {
                        temp = new HashMap<>();
                        temp.put(FIRST_COLUMN, b[0]);
                        temp.put(SECOND_COLUMN,b[1]);
                        private_list.add(temp);
                    }
                    Log.v(TAG,"Lists: " + String.valueOf(private_list));
                } else {
                    shopping_list_public.add(b[0]); // listname
                    shopping_list_public.add(b[3]); // Private
                    shopping_list_public.add(b[2]); // code
                    shopping_list_private.add(b[1]); // timestamp
                    if (!public_list.contains(b[0])) {
                        temp = new HashMap<>();
                        temp.put(FIRST_COLUMN, b[0]);
                        temp.put(SECOND_COLUMN, b[1]);
                        public_list.add(temp);
                    }
                }
            }
        }
    }

    // Removes unused lists.
    private void remove_deleted_SL(List<String> c){
        int i;
        boolean deleted_flag = false;
        Log.v(TAG,"Removing lists");
        List<String[]> internal_db_lists;
        List<String> internal_codes = new ArrayList<>();
        // Grab all entries from DB
        print_db();
        internal_db_lists = db.read_all_lists();
        // Add all codes from entries into a list
        for (i = 0;i < internal_db_lists.size();i++)
            internal_codes.add(internal_db_lists.get(i)[2]);
        // If one of the received codes is inside the DB remove it form the list
        for (i = 0;i < c.size(); i++){
            Log.d(TAG,"received lists: " + c.get(i));
            if (internal_codes.contains(c.get(i)))
                internal_codes.remove(c.get(i));
        }
        // In case there are still codes in the DB that are not on the server remove them from the DB
        if (internal_codes.size() > 0){
            for (i = 0;i < internal_codes.size(); i++){
                deleted_flag = true;
                Log.d(TAG,"Removing lists: " + internal_codes.get(i));
                db.delete_list(internal_codes.get(i));
            }
        }
        if (deleted_flag)
            Toast.makeText(MainActivity.this,R.string.external_delete,Toast.LENGTH_SHORT).show();
        print_db();
    }

    class ProgressTask_Back extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            loading.setVisibility(View.VISIBLE);
            listview.setVisibility(View.GONE);
            public_list.clear();
            private_list.clear();
            adapter.notifyDataSetChanged();

        }
        @Override
        protected Void doInBackground(Void... arg0) {
            if (!usr_inf.getOffline_mode()) {
                //noinspection StatementWithEmptyBody
                while (!server_service.return_response_status());
                getAll_ShoppingLists(usr_inf.getEmail());
            }
            else
                read_from_internal_DB();
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            loading.setVisibility(View.GONE);
            final Animation fadein = new AlphaAnimation(0, 1);
            fadein.setDuration(1000);
            listview.setVisibility(View.VISIBLE);
            listview.setAnimation(fadein);
        }
    }

    // Create the loading and get all the shopping lists when finished
    class ProgressTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            Log.v(TAG,"STARTING EXECUTION OF APP");
            public_list.clear();
            private_list.clear();
            adapter.notifyDataSetChanged();
            loading.setVisibility(View.VISIBLE);
            listview.setVisibility(View.GONE);
            mAdView.setVisibility(View.GONE);

        }
        @Override
        protected Void doInBackground(Void... arg0) {
            if (!usr_inf.getOffline_mode())
                getAll_ShoppingLists(usr_inf.getEmail());
            else
                read_from_internal_DB();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            loading.setVisibility(View.GONE);
            final Animation fadein = new AlphaAnimation(0,1);
            fadein.setDuration(1000);
            Animation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setStartOffset(2000);
            fadeOut.setDuration(1000);
            if (usr_inf.getOffline_mode()) welcome.setText(R.string.offline_explanation);
            else welcome.setText(String.format("\n\nWelcome back\n %s !", usr_inf.getName()));
            welcome.setVisibility(View.VISIBLE);
            welcome.setAnimation(fadein);
            welcome.setAnimation(fadeOut);
            reload_ui(is_private_selected);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    welcome.setVisibility(View.GONE);
                    listview.setVisibility(View.VISIBLE);
                    listview.setAnimation(fadein);
                    first_layout.setVisibility(View.VISIBLE);
                    third_layout.setVisibility(View.VISIBLE);
                    first_layout.setAnimation(fadein);
                    third_layout.setAnimation(fadein);
                    mAdView.setVisibility(View.VISIBLE);
                    mAdView.setAnimation(fadein);
                    if (!usr_inf.getOffline_mode()) send_request_server("_", "_", "add_user", "_", usr_inf.getName());
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
    }

    //On long pressed in a shopping list, display options
    private ActionMode.Callback modeCallBack = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_list, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            switch (id) {
                case R.id.delete: {
                    delete_shoppingList();
                    mode.finish();
                    break;
                }
                case R.id.edit: {
                    edit_shoppingList();
                    mode.finish();
                    break;
                }
                case R.id.share:{
                    if (!usr_inf.getOffline_mode())
                        share_shoppingList();
                    else
                        Toast.makeText(MainActivity.this,R.string.offline_share, Toast.LENGTH_LONG).show();
                    mode.finish();
                    break;
                }
                default:
                    return false;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }
    };

    private void share_shoppingList(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.share_list_msg);
        alert.setMessage(R.string.write_email_msg);

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Boolean valid = validateEmail(input.getText().toString());
                if (!valid) {
                    Context context = getApplicationContext();
                    CharSequence error = String.valueOf(R.string.email_not_valid_msg);
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, error, duration);
                    toast.show();
                    share_shoppingList();
                } else {
                    if (is_bound_server) {
                        Object shop_list = adapter.getItem(currentSelection);
                        String list_name = ((HashMap) shop_list).get(FIRST_COLUMN).toString();

                        String tmp_code;
                        tmp_code = db.read_code(list_name);
                        db.update_list_public(0,tmp_code);
                        send_request_server(list_name, list_type,
                                "add_usr_to_list", tmp_code, input.getText().toString());
                    }
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    private boolean validateEmail(String email) {

        Pattern pattern;
        Matcher matcher;
        String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();

    }


    private void delete_shoppingList(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.delete_list_alert);
        alert.setMessage(R.string.delete_list_msg);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String code_list;
                String status="";
                Object shop_list = adapter.getItem(currentSelection);
                String list_name = ((HashMap) shop_list).get(FIRST_COLUMN).toString();
                code_list = db.read_code(list_name);
                // fake delete until synchronize
                if (!usr_inf.getOffline_mode()) {
                    send_request_server(list_name, status, "delete_list", code_list, "_");
                    Log.v(TAG,"code_list: " + code_list);
                    print_db();
                    db.delete_list(code_list);
                }
                else {
                    db.update_list_change("delete_list",code_list);
                    public_list.clear();
                    private_list.clear();
                    adapter.notifyDataSetChanged();
                    read_from_internal_DB();
                    reload_ui(is_private_selected);
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    private void edit_shoppingList(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.change_name_list);
        alert.setMessage(R.string.set_new_name_msg);
        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (is_bound) {
                    String code="";
                    Object shop_list = adapter.getItem(currentSelection);
                    String list_name = ((HashMap) shop_list).get(FIRST_COLUMN).toString();
                    print_db();
                    code = db.read_code(list_name);
                    db.update_list_name(input.getText().toString(),code);
                    print_db();
                    if (!usr_inf.getOffline_mode()) {
                        db.set_list_flag(code, 0);
                        Log.v(TAG,"Changing list name");
                        send_request_server(list_name, list_type, "change_list_name", code, input.getText().toString());
                        new ProgressTask_Back().execute();
                    }
                    else {
                        db.set_list_flag(code, 1);
                        public_list.clear();
                        private_list.clear();
                        adapter.notifyDataSetChanged();
                        read_from_internal_DB();
                        reload_ui(is_private_selected);
                    }
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    private boolean send_unsynced_entries(){
        Log.d(TAG,"We are online");
        Log.d(TAG,"Synchronizing entries from DB");
        // Get all items with sync flag set
        List<String[]> entries = db.read_all_with_flag_set_list();
        print_db();
        if (entries == null) return true;
        String entry[];
        Log.v(TAG,"Size: "+entries.size());
        for (int i = 0; i< entries.size(); i++){
            entry = entries.get(i);
            Log.d(TAG,"entry: " + Arrays.toString(entry));
            db.set_list_flag(entry[1],0);
            if (entry[3].equals("new_list")) old_codes = entry[1];
            if (entry[3].equals("change_list_name"))
                send_request_server("_",entry[2],entry[3],entry[1],entry[0]);
            else
                send_request_server(entry[0],entry[2],entry[3],entry[1],"_");
            // delete list really.
            if (entry[2].equals("delete_list")) db.delete_list(entry[1]);
            // wait
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        print_db();
        return true;
    }

    private void print_db(){
        List<String[]> entries = db.read_all_lists();
        Log.v(TAG,"STARTING THE PRINT DB");
        for (int i=0; i<entries.size();i++){
            Log.v(TAG,"Entries: " + entries.get(i)[0] +" " + entries.get(i)[2] +" " + entries.get(i)[4]);
        }
        Log.v(TAG,"END");
    }

    private boolean is_network_available(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Override onConfigurationChanged method to configure the orientation of the screen
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!isXLargeTablet(getApplicationContext())){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }
}
