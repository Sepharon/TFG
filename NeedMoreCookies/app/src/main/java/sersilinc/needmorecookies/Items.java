package sersilinc.needmorecookies;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;


public class Items extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //TAG for Logs
    private final String TAG = "Items Activity: ";

    //GoogleApiClient
    private GoogleApiClient mGoogleApiClient;

    //UI elements
    private Button all_items;
    private Button meat_items;
    private Button vegetables_items;
    private Button cereals_items;
    private Button dairy_items;
    private Button sweet_items;
    private Button others_items;

    private View separator1;
    private View separator2;
    private View separator3;
    private View separator4;
    private View separator5;
    private View separator6;
    private View separator7;


    /**[START ListView]**/
    //Header
    private ListView listview_header;
    private ArrayList<HashMap<String, String>> l_header = new ArrayList<HashMap<String, String>>();
    //Content
    private ListView listview_items;
    // Lists
    private ArrayList<HashMap<String, String>> all_items_l = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> meat_items_l = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> vegetables_items_l = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> cereals_items_l = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> dairy_items_l = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> sweet_items_l = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> others_items_l = new ArrayList<HashMap<String, String>>();

    // Adapter
    private ListViewAdapters adapter;
    private ListViewAdapters adapter_header;

    //Columns
    private static final String FIRST_COLUMN = "First";
    private static final String SECOND_COLUMN = "Second";
    private static final String THIRD_COLUMN = "Third";

    //Temporal HashMap to write to the columns
    private HashMap<String, String> temp;
    /**[END ListView]**/
    // RECEIVER
    private IntentFilter filter;
    //Preferences
    private SharedPreferences prefs;
    private String currency;

    // Service
    private Update_Server server_service;
    private boolean is_bound_server = false;

    // Info
    String main = null,list_type = null;
    int current_tab = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        /**[START UI elements]**/
        all_items = (Button) findViewById(R.id.all);
        meat_items = (Button) findViewById(R.id.meat);
        vegetables_items = (Button) findViewById(R.id.vegetables);
        cereals_items = (Button) findViewById(R.id.cereals);
        dairy_items = (Button) findViewById(R.id.dairy);
        sweet_items = (Button) findViewById(R.id.sweet);
        others_items = (Button) findViewById(R.id.others);


        separator1 = findViewById(R.id.separator_items);
        separator2 = findViewById(R.id.separator2_items);
        separator3 = findViewById(R.id.separator3_items);
        separator4 = findViewById(R.id.separator4_items);
        separator5 = findViewById(R.id.separator5_items);
        separator6 = findViewById(R.id.separator6_items);
        separator7 = findViewById(R.id.separator7_items);

        listview_items = (ListView) findViewById(R.id.list_item);

        listview_header = (ListView) findViewById(R.id.list_header);
        /**[END UI elements]**/

        /**[START Intent-filter for receiving Broadcast]**/
        filter = new IntentFilter("broadcast_service");
        MainActivity i = new MainActivity();
        this.registerReceiver(i.receiver, filter);
        /**[END Intent-filter for receiving Broadcast]**/

        /**[START List View]**/

        //Temporal hash to write to columns
        temp = new HashMap<String, String>();
        temp.put(FIRST_COLUMN, "Product");
        temp.put(SECOND_COLUMN, "Quantity");
        temp.put(THIRD_COLUMN, "Price");
        l_header.add(temp);

        //Custom adapter
        adapter_header = new ListViewAdapters(this, l_header, Boolean.FALSE);

        listview_header.setAdapter(adapter_header);

        adapter = new ListViewAdapters(this, all_items_l, Boolean.TRUE);

        listview_items.setAdapter(adapter);
        /**[END List View]**/

        /**[START Preferences]**/
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //Get currency User's preference
        currency = prefs.getString("currency_list", "€");
        /**[END Preferences]**/

        /**[START Navigation]**/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(0);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view2);
        navigationView.setNavigationItemSelectedListener(this);
        /**[END Navigation]**/


        /**[START AddItem activity]**/
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Items.this, AddItem.class);
                // Start next activity
                startActivityForResult(intent, 1);
            }
        });
        /**[END AddItem activity]**/

        /**[START Service binding]**/
        Intent in = new Intent(this, Update_Server.class);
        bindService(in, mConnection2, Context.BIND_AUTO_CREATE);
        /**[END Service binding]**/

        /**[START GoogleApiClient]**/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        /**[END GoogleApiClient]**/


        /**[START onClickListeners]**/
        all_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (separator1.getVisibility() != View.VISIBLE) {
                    current_tab = 1;
                    reload_ui(1);
                }
            }
        });
        meat_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (separator2.getVisibility() != View.VISIBLE) {
                    current_tab = 2;
                    reload_ui(2);
                }
            }
        });
        vegetables_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (separator3.getVisibility() != View.VISIBLE) {
                    current_tab = 3;
                    reload_ui(3);
                }
            }
        });
        cereals_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (separator4.getVisibility() != View.VISIBLE) {
                    current_tab = 4;
                    reload_ui(4);
                }
            }
        });
        dairy_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (separator5.getVisibility() != View.VISIBLE) {
                    current_tab = 5;
                    reload_ui(5);
                }
            }
        });
        sweet_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (separator6.getVisibility() != View.VISIBLE) {
                    current_tab = 6;
                    reload_ui(6);
                }
            }
        });
        others_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (separator7.getVisibility() != View.VISIBLE) {
                    current_tab = 7;
                    reload_ui(7);
                }
            }
        });
        /**[END onClickListeners]**/

        /**[START Get intent extras]**/
        Bundle extras = getIntent().getExtras();
        //Get JSON Strings from the MainActivity
        try {
            main = extras.get("Main").toString();
            String list = extras.get("List").toString();
            list_type = extras.getString("Type");
            Log.v(TAG, main + list + "");
            Log.v(TAG,main);

            update_ShoppingList(list);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        /**[END Get intent extras]**/

        //Reload UI
        reload_ui(1);
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
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
            Intent intent = new Intent(Items.this, MapsActivity.class);
            // Start next activity
            startActivity(intent);
        } else if (id == R.id.nav_home) {
            Intent intent = new Intent(Items.this, MainActivity.class);
            // Start next activity
            startActivity(intent);
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(Items.this, SettingsActivity.class);
            // Start next activity
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            //share();

        } else if (id == R.id.nav_logout) {
            signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //Sign Out from Google Account
    public void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                Intent intent = new Intent(Items.this, Login.class);
                // Start next activity
                startActivity(intent);
                finish();
            }
        });
    }


    private void reload_ui(int type) {
        //Log.v(TAG, "Updating UI");
        if (type == 1) {
            adapter = new ListViewAdapters(this, all_items_l, Boolean.TRUE);
            separator1.setVisibility(View.VISIBLE);
            separator2.setVisibility(View.INVISIBLE);
            separator3.setVisibility(View.INVISIBLE);
            separator4.setVisibility(View.INVISIBLE);
            separator5.setVisibility(View.INVISIBLE);
            separator6.setVisibility(View.INVISIBLE);
            separator7.setVisibility(View.INVISIBLE);
        } else if (type == 2) {
            adapter = new ListViewAdapters(this, meat_items_l, Boolean.TRUE);
            separator1.setVisibility(View.INVISIBLE);
            separator2.setVisibility(View.VISIBLE);
            separator3.setVisibility(View.INVISIBLE);
            separator4.setVisibility(View.INVISIBLE);
            separator5.setVisibility(View.INVISIBLE);
            separator6.setVisibility(View.INVISIBLE);
            separator7.setVisibility(View.INVISIBLE);
        } else if (type == 3) {
            adapter = new ListViewAdapters(this, vegetables_items_l, Boolean.TRUE);
            separator1.setVisibility(View.INVISIBLE);
            separator2.setVisibility(View.INVISIBLE);
            separator3.setVisibility(View.VISIBLE);
            separator4.setVisibility(View.INVISIBLE);
            separator5.setVisibility(View.INVISIBLE);
            separator6.setVisibility(View.INVISIBLE);
            separator7.setVisibility(View.INVISIBLE);
        } else if (type == 4) {
            adapter = new ListViewAdapters(this, cereals_items_l, Boolean.TRUE);
            separator1.setVisibility(View.INVISIBLE);
            separator2.setVisibility(View.INVISIBLE);
            separator3.setVisibility(View.INVISIBLE);
            separator4.setVisibility(View.VISIBLE);
            separator5.setVisibility(View.INVISIBLE);
            separator6.setVisibility(View.INVISIBLE);
            separator7.setVisibility(View.INVISIBLE);
        } else if (type == 5) {
            adapter = new ListViewAdapters(this, dairy_items_l, Boolean.TRUE);
            separator1.setVisibility(View.INVISIBLE);
            separator2.setVisibility(View.INVISIBLE);
            separator3.setVisibility(View.INVISIBLE);
            separator4.setVisibility(View.INVISIBLE);
            separator5.setVisibility(View.VISIBLE);
            separator6.setVisibility(View.INVISIBLE);
            separator7.setVisibility(View.INVISIBLE);
        } else if (type == 6) {
            adapter = new ListViewAdapters(this, sweet_items_l, Boolean.TRUE);
            separator1.setVisibility(View.INVISIBLE);
            separator2.setVisibility(View.INVISIBLE);
            separator3.setVisibility(View.INVISIBLE);
            separator4.setVisibility(View.INVISIBLE);
            separator5.setVisibility(View.INVISIBLE);
            separator6.setVisibility(View.VISIBLE);
            separator7.setVisibility(View.INVISIBLE);
        } else if (type == 7) {
            adapter = new ListViewAdapters(this, others_items_l, Boolean.TRUE);
            separator1.setVisibility(View.INVISIBLE);
            separator2.setVisibility(View.INVISIBLE);
            separator3.setVisibility(View.INVISIBLE);
            separator4.setVisibility(View.INVISIBLE);
            separator5.setVisibility(View.INVISIBLE);
            separator6.setVisibility(View.INVISIBLE);
            separator7.setVisibility(View.VISIBLE);
        }
        //Set adapter
        listview_items.setAdapter(adapter);
    }


    //Put the items in the correct section
    private void update_ShoppingList(String list) {
        try {
            int i = 0;
            JSONObject json_obj = new JSONObject(list);
            Iterator<String> keys = json_obj.keys();
            while (keys.hasNext()) {
                String type = String.valueOf(keys.next());
                switch (type) {
                    case "Everything":
                        JSONArray products = json_obj.getJSONArray(type);
                        while (i < products.length()) {
                            JSONArray rec = products.getJSONArray(i);
                            temp = new HashMap<String, String>();
                            if (rec.getString(2).equals("null")) {
                                temp.put(THIRD_COLUMN, "-" + currency);
                            } else {
                                temp.put(THIRD_COLUMN, rec.getString(2) + currency);
                            }
                            temp.put(FIRST_COLUMN, rec.getString(0));
                            temp.put(SECOND_COLUMN, rec.getString(1));

                            all_items_l.add(temp);
                            i = i + 1;
                            //Log.v(TAG, "" + rec.toString());
                        }
                        i = 0;
                        break;
                    case "Meat and Fish":
                        JSONArray products2 = json_obj.getJSONArray(type);
                        while (i < products2.length()) {
                            JSONArray rec = products2.getJSONArray(i);
                            temp = new HashMap<String, String>();
                            if (rec.getString(2).equals("null")) {
                                temp.put(THIRD_COLUMN, "-" + currency);
                            } else {
                                temp.put(THIRD_COLUMN, rec.getString(2) + currency);
                            }
                            temp.put(FIRST_COLUMN, rec.getString(0));
                            temp.put(SECOND_COLUMN, rec.getString(1));
                            meat_items_l.add(temp);
                            i = i + 1;
                            //Log.v(TAG, "" + rec.toString());
                        }
                        i = 0;
                        //Log.v(TAG, "" + products.toString());
                        break;
                    case "Vegetables":
                        JSONArray products3 = json_obj.getJSONArray(type);
                        while (i < products3.length()) {
                            JSONArray rec = products3.getJSONArray(i);
                            temp = new HashMap<String, String>();
                            if (rec.getString(2).equals("null")) {
                                temp.put(THIRD_COLUMN, "-" + currency);
                            } else {
                                temp.put(THIRD_COLUMN, rec.getString(2) + currency);
                            }
                            temp.put(FIRST_COLUMN, rec.getString(0));
                            temp.put(SECOND_COLUMN, rec.getString(1));
                            vegetables_items_l.add(temp);
                            i = i + 1;
                            //Log.v(TAG, "" + rec.toString());
                        }
                        i = 0;
                        //Log.v(TAG, "" + products.toString());
                        break;
                    case "Cereal":
                        JSONArray products4 = json_obj.getJSONArray(type);
                        while (i < products4.length()) {
                            JSONArray rec = products4.getJSONArray(i);
                            temp = new HashMap<String, String>();
                            if (rec.getString(2).equals("null")) {
                                temp.put(THIRD_COLUMN, "-" + currency);
                            } else {
                                temp.put(THIRD_COLUMN, rec.getString(2) + currency);
                            }
                            temp.put(FIRST_COLUMN, rec.getString(0));
                            temp.put(SECOND_COLUMN, rec.getString(1));
                            cereals_items_l.add(temp);
                            i = i + 1;
                            //Log.v(TAG, "" + rec.toString());
                        }
                        i = 0;
                        //Log.v(TAG, "" + products.toString());
                        break;
                    case "Dairy":
                        JSONArray products5 = json_obj.getJSONArray(type);
                        while (i < products5.length()) {
                            JSONArray rec = products5.getJSONArray(i);
                            temp = new HashMap<String, String>();
                            if (rec.getString(2).equals("null")) {
                                temp.put(THIRD_COLUMN, "-" + currency);
                            } else {
                                temp.put(THIRD_COLUMN, rec.getString(2) + currency);
                            }
                            temp.put(FIRST_COLUMN, rec.getString(0));
                            temp.put(SECOND_COLUMN, rec.getString(1));
                            dairy_items_l.add(temp);
                            i = i + 1;
                            //Log.v(TAG, "" + rec.toString());
                        }
                        i = 0;
                        //Log.v(TAG, "" + products.toString());
                        break;
                    case "Sweet":
                        JSONArray products6 = json_obj.getJSONArray(type);
                        while (i < products6.length()) {
                            JSONArray rec = products6.getJSONArray(i);
                            temp = new HashMap<String, String>();
                            if (rec.getString(2).equals("null")) {
                                temp.put(THIRD_COLUMN, "-" + currency);
                            } else {
                                temp.put(THIRD_COLUMN, rec.getString(2) + currency);
                            }
                            temp.put(FIRST_COLUMN, rec.getString(0));
                            temp.put(SECOND_COLUMN, rec.getString(1));
                            sweet_items_l.add(temp);
                            i = i + 1;
                            //Log.v(TAG, "" + rec.toString());
                        }
                        i = 0;
                        //Log.v(TAG, "" + products.toString());
                        break;
                    case "Others":
                        JSONArray products7 = json_obj.getJSONArray(type);
                        while (i < products7.length()) {
                            JSONArray rec = products7.getJSONArray(i);
                            temp = new HashMap<String, String>();
                            if (rec.getString(2).equals("null")) {
                                temp.put(THIRD_COLUMN, "-" + currency);
                            } else {
                                temp.put(THIRD_COLUMN, rec.getString(2) + currency);
                            }
                            temp.put(FIRST_COLUMN, rec.getString(0));
                            temp.put(SECOND_COLUMN, rec.getString(1));
                            others_items_l.add(temp);
                            i = i + 1;
                            //Log.v(TAG, "" + rec.toString());
                        }
                        i = 0;
                        //Log.v(TAG, "" + products.toString());
                        break;
                }
            }


        } catch (JSONException e) {
            Log.v(TAG, "Error JSON");
            e.printStackTrace();
        }
    }


    //Get results from the AddItem activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // TODO : Diferents if's per cada "status"
                // New Item
                Log.v(TAG, "Result OK");
                String product = data.getStringExtra("product");
                String quantity = data.getStringExtra("quantity");
                String price  = data.getStringExtra("price");
                if (price.equals("")) price = " ";
                String type = data.getStringExtra("type");
                Log.v(TAG, product + quantity + price + type);
                String code = null;
                try {
                    JSONObject rsp = new JSONObject(main);
                    code = rsp.getString("Code");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                send_request_server(list_type,code,type,product,price,quantity);
                temp = new HashMap<String, String>();
                if (price.equals("null")) {
                    temp.put(THIRD_COLUMN, "-" + currency);
                } else {
                    temp.put(THIRD_COLUMN, price + currency);
                }
                temp.put(FIRST_COLUMN, product);
                temp.put(SECOND_COLUMN, quantity);
                all_items_l.add(temp);
                switch (type){
                    case "Cereal":
                        cereals_items_l.add(temp);
                        break;
                    case "Dairy":
                        dairy_items_l.add(temp);
                        break;
                    case "Meat and Fish":
                        meat_items_l.add(temp);
                        break;
                    case "Others":
                        others_items_l.add(temp);
                        break;
                    case "Sweet":
                        sweet_items_l.add(temp);
                        break;
                    case "Vegetables":
                        vegetables_items_l.add(temp);
                        break;
                }
                reload_ui(current_tab);
            }
        }
    }

    //Send request to Update Server service
    private void send_request_server(String status,String code,String type, String product,String price,String quantity){
        server_service.set_values(3, code, "_", "True", status);
        server_service.set_items(type, product , price, quantity);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                server_service.send_request();
                //noinspection StatementWithEmptyBody
                while (!server_service.return_response_status());
                String response = server_service.return_result();
                Intent intent = new Intent();
                intent.setAction("broadcast_service");
                intent.putExtra("Main",response);
                intent.putExtra("Request", "new_item");
                sendBroadcast(intent);
            }
        });
        t.start();
    }
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
}
