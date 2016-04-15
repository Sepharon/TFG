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
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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
import java.util.List;
import java.util.Map;
import java.util.Set;


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


    /**
     * [START ListView]
     **/
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
    private List<List<String>> items_l;

    // Adapter
    private ListViewAdapters adapter;
    private ListViewAdapters adapter_header;

    //Columns
    private static final String FIRST_COLUMN = "First";
    private static final String SECOND_COLUMN = "Second";
    private static final String THIRD_COLUMN = "Third";
    private static final String FOURTH_COLUMN="Fourth";

    //Temporal HashMap to write to the columns
    private HashMap<String, String> temp;
    /**
     * [END ListView]
     **/
    //Preferences
    private SharedPreferences prefs;
    private String currency;

    // Service
    private Update_Server server_service;
    private boolean is_bound_server = false;
    private boolean is_bound = false;
    private Messenger mService = null;

    //Receiver
    private String GoogleAccount;
    private String request_type;
    private String list_items;
    private String main_receiver;
    private String update_product;
    public MyReceiver receiver;
    private IntentFilter filter;

    // Info
    String main = null;
    String list_type;
    int current_tab = 1;
    private final String[] objectives = {"new_name","new_price","new_quantity","new_item","delete_item","new_list","delete_list","change_list_name","set_public","add_usr_to_list","add_user","add_token"};

    // Update items flags
    private boolean product_to_update = false;
    private boolean quantity_to_update = false;
    private boolean price_to_update = false;

    //Selected Item
    private int currentSelection;

    //User info instance
    private User_Info usr_inf;


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

        /*/**[START Intent-filter for receiving Broadcast]
        filter = new IntentFilter("broadcast_service");
        MainActivity i = new MainActivity();
        this.registerReceiver(i.receiver, filter);/*

        /**[START Intent-filter for receiving Broadcast]**/
        filter = new IntentFilter("broadcast_service");
        receiver = new MyReceiver();
        this.registerReceiver(receiver, filter);
        /**[END Intent-filter for receiving Broadcast]**/

        /**[START List View]**/

        //Temporal hash to write to columns
        temp = new HashMap<String, String>();
        temp.put(FIRST_COLUMN, "Product");
        temp.put(SECOND_COLUMN, "Quantity");
        temp.put(THIRD_COLUMN, "Price");
        l_header.add(temp);

        //Custom adapter
        adapter_header = new ListViewAdapters(this, l_header, "Header", "1");

        listview_header.setAdapter(adapter_header);

        adapter = new ListViewAdapters(this, all_items_l, "Content", list_type);

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
                intent.putExtra("Edit", "False");
                // Start next activity
                startActivityForResult(intent, 1);
            }
        });
        /**[END AddItem activity]**/

        /**[START Service binding]**/
        Intent in = new Intent(this, Update_Server.class);
        bindService(in, mConnection2, Context.BIND_AUTO_CREATE);
        Intent intent = new Intent(this, Update_List.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
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

        //Show option to edit or delete if long press
        listview_items.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //Log.v(TAG, "OnItemLongClickListener");
                //System.out.println("Long click");
                currentSelection = position;
                startActionMode(modeCallBack);
                view.setSelected(true);
                return true;
            }
        });
        /**[END onClickListeners]**/

        /**[START User_Info]**/
        //Get User info
        usr_inf = User_Info.getInstance();
        /**[END User_Info]**/

        /**[START Get intent extras]**/
        Bundle extras = getIntent().getExtras();
        //Get JSON Strings from the MainActivity
        try {
            main = extras.getString("Main");
            String list = extras.getString("List");
            list_type = extras.getString("Type");
            Log.v(TAG, main + list + "Type: "+list_type);
            Log.v(TAG, main);

            update_ShoppingList(list);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        /**[END Get intent extras]**/

        //Reload UI
        reload_ui(1);
    }


    // Binding Update List
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.v(TAG, "Binding service");
            mService = new Messenger(service);
            is_bound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.v(TAG,"Update List disconnected");
            mService = null;
            is_bound = false;
        }
    };

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Register receiver
        registerReceiver(receiver, filter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_update_items:
                getAll_products();
                Toast.makeText(Items.this, R.string.update_products, Toast.LENGTH_SHORT).show();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
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
            adapter = new ListViewAdapters(this, all_items_l, "Content", list_type);
            separator1.setVisibility(View.VISIBLE);
            separator2.setVisibility(View.INVISIBLE);
            separator3.setVisibility(View.INVISIBLE);
            separator4.setVisibility(View.INVISIBLE);
            separator5.setVisibility(View.INVISIBLE);
            separator6.setVisibility(View.INVISIBLE);
            separator7.setVisibility(View.INVISIBLE);
        } else if (type == 2) {
            adapter = new ListViewAdapters(this, meat_items_l, "Content", list_type);
            separator1.setVisibility(View.INVISIBLE);
            separator2.setVisibility(View.VISIBLE);
            separator3.setVisibility(View.INVISIBLE);
            separator4.setVisibility(View.INVISIBLE);
            separator5.setVisibility(View.INVISIBLE);
            separator6.setVisibility(View.INVISIBLE);
            separator7.setVisibility(View.INVISIBLE);
        } else if (type == 3) {
            adapter = new ListViewAdapters(this, vegetables_items_l, "Content", list_type);
            separator1.setVisibility(View.INVISIBLE);
            separator2.setVisibility(View.INVISIBLE);
            separator3.setVisibility(View.VISIBLE);
            separator4.setVisibility(View.INVISIBLE);
            separator5.setVisibility(View.INVISIBLE);
            separator6.setVisibility(View.INVISIBLE);
            separator7.setVisibility(View.INVISIBLE);
        } else if (type == 4) {
            adapter = new ListViewAdapters(this, cereals_items_l, "Content", list_type);
            separator1.setVisibility(View.INVISIBLE);
            separator2.setVisibility(View.INVISIBLE);
            separator3.setVisibility(View.INVISIBLE);
            separator4.setVisibility(View.VISIBLE);
            separator5.setVisibility(View.INVISIBLE);
            separator6.setVisibility(View.INVISIBLE);
            separator7.setVisibility(View.INVISIBLE);
        } else if (type == 5) {
            adapter = new ListViewAdapters(this, dairy_items_l, "Content", list_type);
            separator1.setVisibility(View.INVISIBLE);
            separator2.setVisibility(View.INVISIBLE);
            separator3.setVisibility(View.INVISIBLE);
            separator4.setVisibility(View.INVISIBLE);
            separator5.setVisibility(View.VISIBLE);
            separator6.setVisibility(View.INVISIBLE);
            separator7.setVisibility(View.INVISIBLE);
        } else if (type == 6) {
            adapter = new ListViewAdapters(this, sweet_items_l, "Content", list_type);
            separator1.setVisibility(View.INVISIBLE);
            separator2.setVisibility(View.INVISIBLE);
            separator3.setVisibility(View.INVISIBLE);
            separator4.setVisibility(View.INVISIBLE);
            separator5.setVisibility(View.INVISIBLE);
            separator6.setVisibility(View.VISIBLE);
            separator7.setVisibility(View.INVISIBLE);
        } else if (type == 7) {
            adapter = new ListViewAdapters(this, others_items_l, "Content", list_type);
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
            all_items_l.clear();
            meat_items_l.clear();
            vegetables_items_l.clear();
            cereals_items_l.clear();
            dairy_items_l.clear();
            sweet_items_l.clear();
            others_items_l.clear();
            adapter.notifyDataSetChanged();

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

                            if (list_type.equals("0")){
                                temp.put(FOURTH_COLUMN, rec.getString(4));
                            }

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

                            if (list_type.equals("0")){
                                temp.put(FOURTH_COLUMN, rec.getString(4));
                            }

                            meat_items_l.add(temp);

                            //Variables to store the product name, quantity, price, type, code and last_user
                            List<String> item_list_temp = new ArrayList<>();
                            item_list_temp.add(rec.getString(0));
                            item_list_temp.add(rec.getString(1));
                            item_list_temp.add(rec.getString(2));
                            item_list_temp.add(type);
                            item_list_temp.add(rec.getString(3));
                            item_list_temp.add(rec.getString(4));

                            usr_inf.setItems_lists(item_list_temp);


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

                            if (list_type.equals("0")){
                                temp.put(FOURTH_COLUMN, rec.getString(4));
                            }

                            vegetables_items_l.add(temp);

                            //Variables to store the product name, quantity, price, type, code and last_user
                            List<String> item_list_temp = new ArrayList<>();
                            item_list_temp.add(rec.getString(0));
                            item_list_temp.add(rec.getString(1));
                            item_list_temp.add(rec.getString(2));
                            item_list_temp.add(type);
                            item_list_temp.add(rec.getString(3));
                            item_list_temp.add(rec.getString(4));

                            usr_inf.setItems_lists(item_list_temp);


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

                            if (list_type.equals("0")){
                                temp.put(FOURTH_COLUMN, rec.getString(4));
                            }

                            cereals_items_l.add(temp);

                            //Variables to store the product name, quantity, price, type, code and last_user
                            List<String> item_list_temp = new ArrayList<>();
                            item_list_temp.add(rec.getString(0));
                            item_list_temp.add(rec.getString(1));
                            item_list_temp.add(rec.getString(2));
                            item_list_temp.add(type);
                            item_list_temp.add(rec.getString(3));
                            item_list_temp.add(rec.getString(4));

                            usr_inf.setItems_lists(item_list_temp);


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

                            if (list_type.equals("0")){
                                temp.put(FOURTH_COLUMN, rec.getString(4));
                            }

                            dairy_items_l.add(temp);

                            //Variables to store the product name, quantity, price, type, code and last_user
                            List<String> item_list_temp = new ArrayList<>();
                            item_list_temp.add(rec.getString(0));
                            item_list_temp.add(rec.getString(1));
                            item_list_temp.add(rec.getString(2));
                            item_list_temp.add(type);
                            item_list_temp.add(rec.getString(3));
                            item_list_temp.add(rec.getString(4));

                            usr_inf.setItems_lists(item_list_temp);


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

                            if (list_type.equals("0")){
                                temp.put(FOURTH_COLUMN, rec.getString(4));
                            }

                            sweet_items_l.add(temp);

                            //Variables to store the product name, quantity, price, type, code and last_user
                            List<String> item_list_temp = new ArrayList<>();
                            item_list_temp.add(rec.getString(0));
                            item_list_temp.add(rec.getString(1));
                            item_list_temp.add(rec.getString(2));
                            item_list_temp.add(type);
                            item_list_temp.add(rec.getString(3));
                            item_list_temp.add(rec.getString(4));

                            usr_inf.setItems_lists(item_list_temp);


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

                            if (list_type.equals("0")){
                                temp.put(FOURTH_COLUMN, rec.getString(4));
                            }

                            others_items_l.add(temp);

                            //Variables to store the product name, quantity, price, type, code and last_user
                            List<String> item_list_temp = new ArrayList<>();
                            item_list_temp.add(rec.getString(0));
                            item_list_temp.add(rec.getString(1));
                            item_list_temp.add(rec.getString(2));
                            item_list_temp.add(type);
                            item_list_temp.add(rec.getString(3));
                            item_list_temp.add(rec.getString(4));

                            usr_inf.setItems_lists(item_list_temp);


                            i = i + 1;
                            //Log.v(TAG, "" + rec.toString());
                        }
                        i = 0;
                        //Log.v(TAG, "" + products.toString());
                        break;
                }
                //Log.v(TAG, usr_inf.getItems_lists().toString());
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
        // Add product
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // New Item
                Log.v(TAG, "Result OK");
                String product = data.getStringExtra("product");
                String quantity = data.getStringExtra("quantity");
                String price = data.getStringExtra("price");
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
                send_request_server("new_item", list_type, code, type, product, price, quantity, "");
            }
        }
        // Edit products
        else if (requestCode==2) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Log.v(TAG, "Result OK");
                String product = data.getStringExtra("product");
                String quantity = data.getStringExtra("quantity");
                String price = data.getStringExtra("price");
                if (price.equals("")) price = " ";

                Boolean product_changed = data.getBooleanExtra("product_changed", false);
                Boolean quantity_changed = data.getBooleanExtra("quantity_changed", false);
                Boolean price_changed = data.getBooleanExtra("price_changed", false);

                String type = data.getStringExtra("type");
                Log.v(TAG, product + quantity + price + type);
                String code = null;
                try {
                    JSONObject rsp = new JSONObject(main);
                    code = rsp.getString("Code");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Get unique Code
                String code_item = "";
                Object item = adapter.getItem(currentSelection);
                String Product = ((HashMap) item).get(FIRST_COLUMN).toString();
                String Quantity = ((HashMap) item).get(SECOND_COLUMN).toString();
                String Price_currency = ((HashMap) item).get(THIRD_COLUMN).toString();
                String Price = Price_currency.split(currency)[0];
                if (Price.equals("-") | Price.equals("")) {
                    Price = "null";
                }
                String type_prod = get_Product_Type(adapter.getItem(currentSelection).toString());

                items_l = usr_inf.getItems_lists();
                for (int i = 0; i < items_l.size(); i++) {
                    if (items_l.get(i).get(0).equals(Product) & items_l.get(i).get(1).equals(Quantity) & items_l.get(i).get(2).equals(Price) & items_l.get(i).get(3).equals(type_prod)) {
                        code_item = items_l.get(i).get(4);
                    }
                }
                final String finalCode = code;
                final String finalPrice = price;
                final String finalCode_item = code_item;
                final String finalType = type;
                final String finalProduct = product;
                final String finalQuantity = quantity;

                if (product_changed){
                    product_to_update = true;
                    Log.v(TAG,"new name");
                    Log.v("Thread2","product changing");
                    send_request_server("new_name", list_type,finalCode, finalType, finalProduct, finalPrice, finalQuantity, finalCode_item);
                }
                if (quantity_changed) {
                    quantity_to_update = true;
                    Log.v(TAG,"new quantity");
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (product_to_update);
                            Log.v("Thread2","quantity changing");
                            send_request_server("new_quantity", list_type, finalCode, finalType, finalProduct, finalPrice, finalQuantity, finalCode_item);
                        }
                    });
                    t.start();
                }
                if (price_changed) {
                    price_to_update = true;
                    Log.v(TAG,"new price");
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (quantity_to_update || product_to_update);
                            Log.v("Thread2","price changing");
                            send_request_server("new_price", list_type, finalCode, finalType, finalProduct, finalPrice, finalQuantity, finalCode_item);
                        }
                    });
                    t.start();
                }
            }
        }
    }

    //Send request to Update Server service
    private void send_request_server(final String Objective, String status, String code, String type, String product, String price, String quantity, String code_item) {

        server_service.set_values(server_service.get_objective(Objective), code, "_", "True", status);
        server_service.set_items(type, product, price, quantity, code_item);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                server_service.send_request();
                //noinspection StatementWithEmptyBody
                while (!server_service.return_response_status()) ;
                String response = server_service.return_result();
                Intent intent = new Intent();
                intent.setAction("broadcast_service");
                intent.putExtra("Main", response);
                intent.putExtra("Request", Objective);
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


    //On long pressed in a shopping list, display options
    private ActionMode.Callback modeCallBack = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            //mode.setTitle("Options");
            mode.getMenuInflater().inflate(R.menu.menu_item, menu);
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
                case R.id.delete_item: {
                    delete_item();
                    mode.finish();
                    break;
                }
                case R.id.edit_item: {
                    edit_shoppingList();
                    mode.finish();
                    //System.out.println(" edit ");
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

    //Delete selected item
    private void delete_item() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Delete the product from the shopping list?");
        alert.setMessage("Do you really want to delete the product?");


        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Object item = adapter.getItem(currentSelection);
                String Product = ((HashMap) item).get(FIRST_COLUMN).toString();
                String Quantity = ((HashMap) item).get(SECOND_COLUMN).toString();
                String Price_currency = ((HashMap) item).get(THIRD_COLUMN).toString();
                String Price = Price_currency.split(currency)[0];
                if (Price.equals("-")) {
                    Price = "null";
                }
                String type = get_Product_Type(adapter.getItem(currentSelection).toString());

                //Log.v(TAG, "DELETE: "+Product+Quantity+Price+type);
                //Get list code
                String code = null;
                try {
                    JSONObject rsp = new JSONObject(main);
                    code = rsp.getString("Code");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String code_item="";
                items_l = usr_inf.getItems_lists();
                //Log.v(TAG, "LIST: "+items_l);
                for (int i = 0; i < items_l.size(); i++) {
                    if (items_l.get(i).get(0).equals(Product) & items_l.get(i).get(1).equals(Quantity) & items_l.get(i).get(2).equals(Price) & items_l.get(i).get(3).equals(type)) {
                        code_item = items_l.get(i).get(4);
                    }
                }
                send_request_server("delete_item", list_type, code, type, Product, Price, Quantity, code_item);
            }
        });


        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();

    }

    private String get_Product_Type(String selection) {
        String type = "";
        for (int i = 0; i < meat_items_l.size(); i++) {
            if (meat_items_l.get(i).toString().equals(selection)) {
                type = "Meat and Fish";
            }
        }
        for (int i = 0; i < vegetables_items_l.size(); i++) {
            if (vegetables_items_l.get(i).toString().equals(selection)) {
                type = "Vegetables";
            }
        }
        for (int i = 0; i < cereals_items_l.size(); i++) {
            if (cereals_items_l.get(i).toString().equals(selection)) {
                type = "Cereal";
            }
        }
        for (int i = 0; i < dairy_items_l.size(); i++) {
            if (dairy_items_l.get(i).toString().equals(selection)) {
                type = "Dairy";
            }
        }
        for (int i = 0; i < sweet_items_l.size(); i++) {
            if (sweet_items_l.get(i).toString().equals(selection)) {
                type = "Sweet";
            }
        }
        for (int i = 0; i < others_items_l.size(); i++) {
            if (others_items_l.get(i).toString().equals(selection)) {
                type = "Others";
            }
        }
        return type;
    }


    private void getAll_products() {
        if (is_bound) {
            String code="";
            String user="";
            // Create and send a message to the service, using a supported 'what' value
            //Log.v(TAG, "Getting ready");
            try {
                //Log.v(TAG, "MAAAIN"+main);
                JSONObject rsp = new JSONObject(main);
                code = rsp.getString("Code");
                user = rsp.getString("GoogleAccount");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Message msg = Message.obtain(null, Update_List.MSG_GET_DATA);
            Bundle bundle = new Bundle();
            bundle.putString("request", "one_list");
            bundle.putString("GoogleAccount", user);
            bundle.putString("code_list", code);
            bundle.putString("Activity", "Items");
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



    //Receiver from Services
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            request_type = intent.getStringExtra("Request");
            main_receiver = intent.getStringExtra("Main");
            GoogleAccount = intent.getStringExtra("GoogleAccount");

            //Check type of request
            switch(request_type){
                case "one_list":
                    update_product = intent.getStringExtra("Update_Products");
                    if (update_product.equals("True")) {
                        list_items = intent.getStringExtra("One_list");
                        update_ShoppingList(list_items);
                    }
                    break;
                case "new_item":
                    if (main_receiver.equals("False"))
                        Toast.makeText(Items.this,R.string.add_item_error,Toast.LENGTH_SHORT)
                                .show();
                    else{
                        all_items_l.clear();
                        meat_items_l.clear();
                        vegetables_items_l.clear();
                        cereals_items_l.clear();
                        dairy_items_l.clear();
                        sweet_items_l.clear();
                        others_items_l.clear();
                        adapter.notifyDataSetChanged();
                        getAll_products();
                        Log.v(TAG, "Added new product correctly");
                    }

                    break;
                case "delete_item":
                    if (main_receiver.equals("False"))
                        Toast.makeText(Items.this,R.string.delete_item_error,Toast.LENGTH_SHORT)
                                .show();
                    else{
                        all_items_l.clear();
                        meat_items_l.clear();
                        vegetables_items_l.clear();
                        cereals_items_l.clear();
                        dairy_items_l.clear();
                        sweet_items_l.clear();
                        others_items_l.clear();
                        adapter.notifyDataSetChanged();
                        getAll_products();
                        Log.v(TAG, "Deleted product correctly");
                    }

                    break;
                case "new_name":
                    if (main_receiver.equals("False"))
                        Toast.makeText(Items.this, R.string.new_name_item_error, Toast.LENGTH_SHORT)
                                .show();
                    else{
                        all_items_l.clear();
                        meat_items_l.clear();
                        vegetables_items_l.clear();
                        cereals_items_l.clear();
                        dairy_items_l.clear();
                        sweet_items_l.clear();
                        others_items_l.clear();
                        adapter.notifyDataSetChanged();
                        getAll_products();
                        product_to_update = false;
                        Log.v(TAG, "Name of the product changed correctly");
                    }

                    break;
                case "new_quantity":
                    if (main_receiver.equals("False"))
                        Toast.makeText(Items.this, R.string.new_quantity_item_error, Toast.LENGTH_SHORT)
                                .show();
                    else{
                        all_items_l.clear();
                        meat_items_l.clear();
                        vegetables_items_l.clear();
                        cereals_items_l.clear();
                        dairy_items_l.clear();
                        sweet_items_l.clear();
                        others_items_l.clear();
                        adapter.notifyDataSetChanged();
                        getAll_products();
                        quantity_to_update = false;
                        Log.v(TAG, "Quantity of the product changed correctly");
                    }

                    break;
                case "new_price":
                    if (main_receiver.equals("False"))
                        Toast.makeText(Items.this,R.string.new_price_item_error,Toast.LENGTH_SHORT)
                                .show();
                    else{
                        all_items_l.clear();
                        meat_items_l.clear();
                        vegetables_items_l.clear();
                        cereals_items_l.clear();
                        dairy_items_l.clear();
                        sweet_items_l.clear();
                        others_items_l.clear();
                        adapter.notifyDataSetChanged();
                        getAll_products();
                        price_to_update = false;
                        Log.v(TAG, "Price of the product changed correctly");
                    }

                    break;
            }
        }
    }


    private void edit_shoppingList(){
        Object item = adapter.getItem(currentSelection);
        String Product = ((HashMap) item).get(FIRST_COLUMN).toString();
        String Quantity = ((HashMap) item).get(SECOND_COLUMN).toString();
        String Price_currency = ((HashMap) item).get(THIRD_COLUMN).toString();
        String Price = Price_currency.split(currency)[0];
        if (Price.equals("-")) {
            Price = null;
        }
        String type = get_Product_Type(adapter.getItem(currentSelection).toString());
        Intent intent = new Intent(Items.this, AddItem.class);
        intent.putExtra("Edit", "True");
        intent.putExtra("Product", Product);
        intent.putExtra("Price", Price);
        intent.putExtra("Quantity", Quantity);
        intent.putExtra("Type", type);

        // Start next activity
        startActivityForResult(intent, 2);
    }
}
