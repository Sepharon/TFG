package sersilinc.needmorecookies;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Main tag for Logs
    private final String TAG = "Main Activity: ";

    // Service elements
    private Messenger mService = null;
    private boolean is_bound = false;
    private Update_Server server_service;
    private boolean is_bound_server = false;

    //Receiver
    private String GoogleAccount;
    private String request_type;
    private String list;
    private String main;
    public MyReceiver receiver;
    private IntentFilter filter;

    // UI elements
    private Button private_lists;
    private Button public_lists;
    private View separator1;
    private View separator2;
    private ProgressBar loading;

    //Lists
    private List<List<String>> private_l;
    private List<List<String>> public_l;
    private String [] last_list = new String[2];

    // ListView
    private ListView listview;
    // Private and public list names
    private List<String> public_list = new ArrayList<>();
    private List<String> private_list = new ArrayList<>();

    // Adapter
    private ArrayAdapter<String> adapter;

    //Google API client
    private GoogleApiClient mGoogleApiClient;

    //User info
    private User_Info usr_inf;
    private int list_type = -1;
    //Timer
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_main);

        /**[START Intent-filter for receiving Broadcast]**/
        filter = new IntentFilter("broadcast_service");
        receiver = new MyReceiver();
        this.registerReceiver(receiver, filter);
        /**[END Intent-filter for receiving Broadcast]**/

        /**[START Bind service Update List]**/
        Intent intent = new Intent(this, Update_List.class);
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
        loading = (ProgressBar) findViewById(R.id.progressBar);
        /**[END UI elements]**/

        /**[START List view]**/
         //Adapter
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,private_list);
        // Create List View
        listview.setAdapter(adapter);
        /**[END List view]**/

        /**[START Navigation]**/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setElevation(0);
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        /**[END Navigation]**/

        /**[START AddList call]**/
        //Add new list
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
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

        /**[START OnClickListeners]**/
        //Change to private or public view
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

        //Show the products of the selected shopping list in a new activity
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected = listview.getItemAtPosition(position).toString();
                if (is_bound) {
                    // Create and send a message to the service, using a supported 'what' value
                    Log.v(TAG, "Getting ready");
                    Message msg = Message.obtain(null, Update_List.MSG_GET_DATA);
                    Bundle bundle = new Bundle();
                    bundle.putString("request", "one_list");

                    //Get the private and public shopping lists of the user
                    //and check which one has been selected
                    private_l = usr_inf.getPrivate_lists();
                    public_l = usr_inf.getPublic_lists();
                    for (int i = 0; i < private_l.size(); i++) {
                        //Log.v(TAG, "LIST: " + private_l.get(i));
                        if (private_l.get(i).get(0).equals(selected)) {
                            list_type = 0;
                            bundle.putString("code_list", private_l.get(i).get(2));
                        }
                    }

                    for (int i = 0; i < public_l.size(); i++) {
                        //Log.v(TAG, "LIST: " + public_l.get(i));
                        if (public_l.get(i).get(0).equals(selected)) {
                            list_type = 1;
                            bundle.putString("code_list", public_l.get(i).get(2));
                        }
                    }

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
                //Log.v(TAG, "timer");
                getAll_ShoppingLists(usr_inf.getEmail());
                start();
            }
        }.start();
        /**[END Counter]**/

    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
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

        //Unregister receiver
        unregisterReceiver(receiver);
        timer.cancel();
    }

    // Binding Update List
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.v(TAG, "Binding service");
            mService = new Messenger(service);
            is_bound = true;

            //Get the shopping lists and displaying a loading progress circle
            new ProgressTask().execute();

            //Log.v(TAG, usr_inf.getPrivate_lists().size() + "");
            //Log.v(TAG, usr_inf.getPublic_lists().size() + "");
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
            GoogleAccount = intent.getStringExtra("GoogleAccount");

            //Check type of request
            switch(request_type){
                case "one_list":
                    list = intent.getStringExtra("One_list");
                    Log.v(TAG,list);
                    changeActivity(main, list);
                    break;
                case "all":
                    list = intent.getStringExtra("all");
                    update_Users_data(list);
                    break;
                case "shared_list":
                    list = intent.getStringExtra("shared_list");
                    changeActivity(main, list);
                    break;
                case "code":
                    list = "";
                    changeActivity(main, list);
                    break;
                case "new_list":
                    if (main.equals("False"))
                        Toast.makeText(MainActivity.this, R.string.add_list_error,Toast.LENGTH_SHORT)
                                .show();
                    Log.v(TAG, "Added new Shopping List correctly");
                    break;
                case "new_item":
                    if (main.equals("False"))
                        Toast.makeText(MainActivity.this,R.string.add_item_error,Toast.LENGTH_SHORT)
                                .show();
                    Log.v(TAG,"Added new item correctly");
                    break;
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        //Restart timer
        timer.start();

        //Register receiver
        registerReceiver(receiver, filter);

        //Get shopping lists
        new ProgressTask().execute();
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
            //TODO: Change share
            Intent intent = new Intent(MainActivity.this, Items.class);
            // Start next activity
            startActivity(intent);
            //share();

        } else if (id == R.id.nav_logout) {
            signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        // Check which request we're responding to
        //Log.v(TAG, "Received result");
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

                String list_name = data.getStringExtra("List_Name");
                String Type = data.getStringExtra("Type");
                //Check which type of list the user wants to add
                switch (Type){
                    case "true":
                        private_list.add(list_name);
                        reload_ui(true);
                        send_request_server(list_name, "1");
                        break;
                    case "false":
                        public_list.add(list_name);
                        reload_ui(false);
                        send_request_server(list_name, "0");
                        break;
                }
            }
        }
    }

    //Send request to Update Server service
    private void send_request_server(String list_name,String status){
        server_service.set_values(5, "_", list_name, "True", status);
        server_service.set_items("_", "_", "_", "_");
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                server_service.send_request();
                //noinspection StatementWithEmptyBody
                while (!server_service.return_response_status());
                String response = server_service.return_result();
                Log.v("Thread",response);
                Intent intent = new Intent();
                intent.setAction("broadcast_service");
                intent.putExtra("Main",response);
                intent.putExtra("Request", "new_list");
                sendBroadcast(intent);
            }
        });
        t.start();
    }

    //Change the UI either private or public shopping lists
    private void reload_ui(Boolean type){
        //Log.v(TAG, "Updating UI");
        if (type){
            adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,private_list);
            separator1.setVisibility(View.VISIBLE);
            separator2.setVisibility(View.INVISIBLE);
        }
        else {
            adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,public_list);
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
        intent.putExtra("Type", list_type +"");
        startActivity(intent);
    }

    private void getAll_ShoppingLists(String GoogleAccount){
        if (is_bound) {
            // Create and send a message to the service, using a supported 'what' value
            //Log.v(TAG, "Getting ready");
            Message msg = Message.obtain(null, Update_List.MSG_GET_DATA);
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
        //else Log.v(TAG,"NOT BOUND");
    }

    //Update the UI with all the shopping lists
    private void update_Users_data(String result){
        try {
            //Log.v(TAG, result);
            JSONObject json_obj = new JSONObject(result);
            //Log.v(TAG, "length: " + json_obj.length());
            Iterator<String> keys = json_obj.keys();
            while (keys.hasNext()) {
                //Get list name
                String list_name = String.valueOf(keys.next());
                JSONObject list1 = json_obj.getJSONObject(list_name);

                //Get the code
                String code = list1.getString("Code");

                //Variables to store the list name, type and code
                List<String> shopping_list_private = new ArrayList<>();
                List<String> shopping_list_public = new ArrayList<>();

                int type = list1.getInt("TypeList");
                //Check type of shopping list and store them in the User_Info class
                //The format is the following: [[List_name, Type, Code], [List_name2, Type, Code],...]
                switch (type) {
                    case 0:
                        shopping_list_public.add(list_name);
                        shopping_list_public.add("0");
                        shopping_list_public.add(code);
                        if (!public_list.contains(list_name)) {
                            public_list.add(list_name);

                        }
                        usr_inf.setPublic_lists(shopping_list_public);
                        reload_ui(Boolean.TRUE);
                        break;
                    case 1:
                        shopping_list_private.add(list_name);
                        shopping_list_private.add("1");
                        shopping_list_private.add(code);
                        if (!private_list.contains(list_name)) {
                            private_list.add(list_name);
                        }
                        usr_inf.setPrivate_lists(shopping_list_private);
                        reload_ui(Boolean.TRUE);
                        break;
                }
            }

        } catch (JSONException e){
            e.printStackTrace();
        }
    }


    //Create the loading and get all the shopping lists when finished
    class ProgressTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            loading.setVisibility(View.VISIBLE);
            listview.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            getAll_ShoppingLists(usr_inf.getEmail());
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
            listview.setVisibility(View.VISIBLE);
        }
    }
}
