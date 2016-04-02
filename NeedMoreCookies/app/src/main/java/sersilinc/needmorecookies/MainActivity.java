package sersilinc.needmorecookies;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// TODO : CREATE BIND SERVICE IMPLEMENTATION
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    // call functions from service usuing data.function_name()
    // Main tag for Logs
    private final String TAG = "Main Activity: ";
    // Service elements
    Messenger mService = null;
    boolean is_bound = false;
    boolean is_bound_server = false;

    //Receiver
    String GoogleAccount;
    String request_type;
    String list;
    String main;
    MyReceiver receiver;

    // UI elements
    Button private_lists;
    Button public_lists;
    private View separator1;
    private View separator2;

    TextView Data1;
    TextView Data2;
    String data1="";
    String data2="";

    // ListView
    private ListView listview;
    // Private and public list names
    List<String> public_list = new ArrayList<>();
    List<String> private_list = new ArrayList<>();
    // Adapter
    ArrayAdapter<String> adapter;
    //Google API client
    private GoogleApiClient mGoogleApiClient;
    // Current state of UI true = private ; false = public
    boolean private_or_public = true;

    //User info
    User_Info usr_inf;

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_main);

        //Intent-filter for receiving Broadcast
        IntentFilter filter = new IntentFilter("broadcast_service");
        receiver = new MyReceiver();
        this.registerReceiver(receiver, filter);

        //Bind service Update List
        Intent intent = new Intent(this, Update_List.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        //Bind service Update Server
        //Log.v(TAG, "Starting Update Server");
        Intent in = new Intent(this, Update_Server.class);
        bindService(in, mConnection2, Context.BIND_AUTO_CREATE);


        private_lists = (Button) findViewById(R.id.private_lists);
        public_lists = (Button) findViewById(R.id.public_lists);
        separator1 = findViewById(R.id.separator);
        separator2 = findViewById(R.id.separator2);
        listview = (ListView) findViewById(R.id.list);
        // Create array with all the pacients
        //private_list.add("Test");
        //Log.v(TAG, private_list.size() + "");

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,private_list);
        // Create listview
        listview.setAdapter(adapter);

        //Navigation + floating action button
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(0);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddList.class);
                // Start next activity
                startActivityForResult(intent, 1);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        private_lists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do things here
                if (separator1.getVisibility() != View.VISIBLE) {
                    // Set Adapted for private lists
                    reload_ui(true);
                }
            }
        });
        public_lists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do things here
                if (separator2.getVisibility() != View.VISIBLE) {
                    // Set Adapted for private lists
                    reload_ui(false);
                }
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected = listview.getItemAtPosition(position).toString();
                if (is_bound) {
                    Log.v(TAG, selected);
                    //if (!is_bound) return;
                    // Create and send a message to the service, using a supported 'what' value
                    Log.v(TAG, "Getting ready");
                    Message msg = Message.obtain(null, Update_List.MSG_GET_DATA);
                    Bundle bundle = new Bundle();
                    bundle.putString("request", "one_list");
                    bundle.putString("code_list", "private_silvia");
                    bundle.putString("hash_list", "xyz");
                    bundle.putString("GoogleAccount", usr_inf.getEmail());
                    msg.setData(bundle);
                    try {
                        mService.send(msg);
                        Log.v(TAG, "Message sent");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    // Done sending data
                }
            }
        });

        //Get User info
        usr_inf = User_Info.getInstance();
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (is_bound) {
            unbindService(mConnection);
            is_bound = false;
        }
        if (is_bound_server) {
            unbindService(mConnection2);
            is_bound_server = false;
        }
    }

    // Binding Update List
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.v(TAG, "Binding service");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            //Update_List.LocalBinder binder = (Update_List.LocalBinder) service;
            //binder.getService();
            mService = new Messenger(service);
            is_bound = true;
            getAll_ShoppingLists(usr_inf.getEmail());
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
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
            //Update_List.LocalBinder binder = (Update_List.LocalBinder) service;
            //binder.getService();
            //mService = new Messenger(service);
            is_bound_server = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            //mService = null;
            is_bound_server = false;
        }
    };



    //Receiver from Service
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //text.setText("");
            request_type = intent.getStringExtra("Request");
            main = intent.getStringExtra("Main");
            GoogleAccount = intent.getStringExtra("GoogleAccount");
            switch(request_type){
                case "one_list":
                    list = intent.getStringExtra("One_list");
                    changeActivity(main, list);
                    break;
                case "all":
                    list = intent.getStringExtra("all");
                    updateUI(list);
                    break;
                case "shared_list":
                    list = intent.getStringExtra("shared_list");
                    changeActivity(main, list);
                    break;
                case "code":
                    list = "";
                    changeActivity(main, list);
                    break;
            }
            Log.v(TAG, "Main: " + main + "\r\n"+"Shopping_list: "+list);
            //text.setText("Main: "+main+"\r\n"+"Shopping_list: "+list);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //Navigation
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_locations) {
            Intent intent = new Intent(MainActivity.this,MapsActivity.class);
            // Start next activity
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            // Start next activity
            startActivity(intent);
            finish();
        }
        else if (id == R.id.nav_home) {
            Intent intent = new Intent(MainActivity.this,MainActivity.class);
            // Start next activity
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_share) {
            Intent intent = new Intent(MainActivity.this,Items.class);
            // Start next activity
            startActivity(intent);
            finish();
            //share();

        } else if (id == R.id.nav_logout) {
            signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //Sign Out from Google Account
    public void signOut() {
        User_Info usr_inf;
        usr_inf = User_Info.getInstance();
        //Log.v("GOAPICLIENT2", "" + usr_inf.getmAPIClient());
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        Log.v(TAG, "Received result");
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Log.v(TAG,"Result OK");
                Log.v(TAG,"" + data.getStringExtra("Type"));
                switch (data.getStringExtra("Type")){
                    case "true":
                        private_list.add(data.getStringExtra("List_Name"));
                        reload_ui(true);
                        break;
                    case "false":
                        public_list.add(data.getStringExtra("List_Name"));
                        reload_ui(false);
                        break;
                }
            }
        }
    }
    private void reload_ui(Boolean type){
        Log.v(TAG, "Updating UI");
        if (type){
            adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,private_list);
            private_or_public = true;
            separator1.setVisibility(View.VISIBLE);
            separator2.setVisibility(View.INVISIBLE);
        }
        else {
            adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,public_list);
            private_or_public = false;
            separator2.setVisibility(View.VISIBLE);
            separator1.setVisibility(View.INVISIBLE);
        }
        // Create listview
        listview.setAdapter(adapter);
    }




    public void changeActivity(String main, String list){
        Intent intent = new Intent(this, Items.class);
        intent.putExtra("Main", main);
        intent.putExtra("List", list);
        startActivity(intent);
    }

    public void getAll_ShoppingLists(String GoogleAccount){
        if (is_bound) {
            //if (!is_bound) return;
            // Create and send a message to the service, using a supported 'what' value
            Log.v(TAG, "Getting ready");
            Message msg = Message.obtain(null, Update_List.MSG_GET_DATA);
            Bundle bundle = new Bundle();
            bundle.putString("request", "all");
            bundle.putString("GoogleAccount", GoogleAccount);
            msg.setData(bundle);
            try {
                mService.send(msg);
                Log.v(TAG, "Message sent");
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            // Done sending data
        }
    }

    public void updateUI(String result){
        try {
            JSONObject json_obj = new JSONObject(result);
            Log.v(TAG, "length: " + json_obj.length());
            Iterator<String> keys = json_obj.keys();
            while (keys.hasNext()){
                //JSONObject list1 = json_obj.getJSONObject(String.valueOf(keys.next()));
                String list_name = String.valueOf(keys.next());
                JSONObject list1 = json_obj.getJSONObject(list_name);
                switch (list1.getInt("TypeList")){
                    case 0:
                        public_list.add(list_name);
                        reload_ui(Boolean.TRUE);
                        break;
                    case 1:
                        private_list.add(list_name);
                        reload_ui(Boolean.TRUE);
                        break;
                }
                //Log.v(TAG, "OBJECT: "+list1+ "LIST: "+list_name+" TYPE: "+list1.getInt("TypeList"));
                //Log.v(TAG, "keys: "+String.valueOf(keys.next()));
            }
        } catch (JSONException e){
            Log.v(TAG, "Error_JSON");
            e.printStackTrace();
        }


    }
}
