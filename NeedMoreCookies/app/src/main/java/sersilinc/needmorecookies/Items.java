package sersilinc.needmorecookies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class Items extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = "Items Activity: ";

    private GoogleApiClient mGoogleApiClient;

    Button all_items;
    Button meat_items;
    Button vegetables_items;
    Button cereals_items;
    Button dairy_items;
    Button sweet_items;
    Button others_items;

    View separator1;
    View separator2;
    View separator3;
    View separator4;
    View separator5;
    View separator6;
    View separator7;


    // ListView
    //Header
    ListView listview_header;
    ArrayList<HashMap<String, String>> l_header = new ArrayList<HashMap<String,String>>();
    //Content
    ListView listview_items;
    // Lists
    ArrayList<HashMap<String, String>> all_items_l = new ArrayList<HashMap<String,String>>();
    ArrayList<HashMap<String, String>> meat_items_l = new ArrayList<HashMap<String,String>>();
    ArrayList<HashMap<String, String>> vegetables_items_l = new ArrayList<HashMap<String,String>>();
    ArrayList<HashMap<String, String>> cereals_items_l = new ArrayList<HashMap<String,String>>();
    ArrayList<HashMap<String, String>> dairy_items_l = new ArrayList<HashMap<String,String>>();
    ArrayList<HashMap<String, String>> sweet_items_l = new ArrayList<HashMap<String,String>>();
    ArrayList<HashMap<String, String>> others_items_l = new ArrayList<HashMap<String,String>>();

    // Adapter
    ListViewAdapters adapter;
    ListViewAdapters adapter_header;
    //ArrayList<HashMap<String, String>> list;
    static final String FIRST_COLUMN="First";
    static final String SECOND_COLUMN="Second";
    static final String THIRD_COLUMN="Third";

    HashMap<String,String> temp;

    //Preferences
    SharedPreferences prefs;
    String currency;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

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

        temp=new HashMap<String, String>();
        temp.put(FIRST_COLUMN, "Product");
        temp.put(SECOND_COLUMN, "Quantity");
        temp.put(THIRD_COLUMN, "Price");
        l_header.add(temp);

        adapter_header=new ListViewAdapters(this, l_header, Boolean.FALSE);

        listview_header.setAdapter(adapter_header);

        adapter=new ListViewAdapters(this, all_items_l, Boolean.TRUE);

        listview_items.setAdapter(adapter);

        /*temp=new HashMap<String, String>();
        temp.put(FIRST_COLUMN, "Product");
        temp.put(SECOND_COLUMN, "Quantity");
        temp.put(THIRD_COLUMN, "Price");
        all_items_l.add(temp);
        meat_items_l.add(temp);
        vegetables_items_l.add(temp);
        cereals_items_l.add(temp);
        dairy_items_l.add(temp);
        others_items_l.add(temp);
        sweet_items_l.add(temp);
        */

        //Preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        currency = prefs.getString("currency_list", null);


        //Navigation + floating action button
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(0);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Items.this, AddItem.class);
                // Start next activity
                startActivity(intent);
                finish();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view2);
        navigationView.setNavigationItemSelectedListener(this);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();



        all_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do things here
                if (separator1.getVisibility() != View.VISIBLE) {
                    // Set Adapted for private lists
                    reload_ui(1);
                }
            }
        });
        meat_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do things here
                if (separator2.getVisibility() != View.VISIBLE) {
                    // Set Adapted for private lists
                    reload_ui(2);
                }
            }
        });
        vegetables_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do things here
                if (separator3.getVisibility() != View.VISIBLE) {
                    // Set Adapted for private lists
                    reload_ui(3);
                }
            }
        });
        cereals_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do things here
                if (separator4.getVisibility() != View.VISIBLE) {
                    // Set Adapted for private lists
                    reload_ui(4);
                }
            }
        });
        dairy_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do things here
                if (separator5.getVisibility() != View.VISIBLE) {
                    // Set Adapted for private lists
                    reload_ui(5);
                }
            }
        });
        sweet_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do things here
                if (separator6.getVisibility() != View.VISIBLE) {
                    // Set Adapted for private lists
                    reload_ui(6);
                }
            }
        });
        others_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do things here
                if (separator7.getVisibility() != View.VISIBLE) {
                    // Set Adapted for private lists
                    reload_ui(7);
                }
            }
        });

        reload_ui(1);


        Bundle extras = getIntent().getExtras();
        try {
            String main = extras.get("Main").toString();
            String list = extras.get("List").toString();
            Log.v(TAG, main+list+"");

            update_ShoppingList(list);
        } catch (NullPointerException e){
            e.printStackTrace();
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
    //comment

    //Navigation
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_locations) {
            Intent intent = new Intent(Items.this,MapsActivity.class);
            // Start next activity
            startActivity(intent);
        }
        else if (id == R.id.nav_home) {
            Intent intent = new Intent(Items.this,MainActivity.class);
            // Start next activity
            startActivity(intent);
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(Items.this,SettingsActivity.class);
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





    private void reload_ui(int type){
        Log.v(TAG, "Updating UI");
        if (type==1){
            adapter=new ListViewAdapters(this, all_items_l, Boolean.TRUE);
            //adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,all_items_l);
            separator1.setVisibility(View.VISIBLE);
            separator2.setVisibility(View.INVISIBLE);
            separator3.setVisibility(View.INVISIBLE);
            separator4.setVisibility(View.INVISIBLE);
            separator5.setVisibility(View.INVISIBLE);
            separator6.setVisibility(View.INVISIBLE);
            separator7.setVisibility(View.INVISIBLE);
        }
        else if (type==2){
            adapter=new ListViewAdapters(this, meat_items_l, Boolean.TRUE);
            //adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,meat_items_l);
            separator1.setVisibility(View.INVISIBLE);
            separator2.setVisibility(View.VISIBLE);
            separator3.setVisibility(View.INVISIBLE);
            separator4.setVisibility(View.INVISIBLE);
            separator5.setVisibility(View.INVISIBLE);
            separator6.setVisibility(View.INVISIBLE);
            separator7.setVisibility(View.INVISIBLE);
        }
        else if (type==3){
            adapter=new ListViewAdapters(this, vegetables_items_l, Boolean.TRUE);
            //adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,vegetables_items_l);
            separator1.setVisibility(View.INVISIBLE);
            separator2.setVisibility(View.INVISIBLE);
            separator3.setVisibility(View.VISIBLE);
            separator4.setVisibility(View.INVISIBLE);
            separator5.setVisibility(View.INVISIBLE);
            separator6.setVisibility(View.INVISIBLE);
            separator7.setVisibility(View.INVISIBLE);
        }
        else if (type==4){
            adapter=new ListViewAdapters(this, cereals_items_l, Boolean.TRUE);
            //adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,cereals_items_l);
            separator1.setVisibility(View.INVISIBLE);
            separator2.setVisibility(View.INVISIBLE);
            separator3.setVisibility(View.INVISIBLE);
            separator4.setVisibility(View.VISIBLE);
            separator5.setVisibility(View.INVISIBLE);
            separator6.setVisibility(View.INVISIBLE);
            separator7.setVisibility(View.INVISIBLE);
        }
        else if (type==5){
            adapter=new ListViewAdapters(this, dairy_items_l, Boolean.TRUE);
            //adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,dairy_items_l);
            separator1.setVisibility(View.INVISIBLE);
            separator2.setVisibility(View.INVISIBLE);
            separator3.setVisibility(View.INVISIBLE);
            separator4.setVisibility(View.INVISIBLE);
            separator5.setVisibility(View.VISIBLE);
            separator6.setVisibility(View.INVISIBLE);
            separator7.setVisibility(View.INVISIBLE);
        }
        else if (type==6){
            adapter=new ListViewAdapters(this, sweet_items_l, Boolean.TRUE);
            //adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,sweet_items_l);
            separator1.setVisibility(View.INVISIBLE);
            separator2.setVisibility(View.INVISIBLE);
            separator3.setVisibility(View.INVISIBLE);
            separator4.setVisibility(View.INVISIBLE);
            separator5.setVisibility(View.INVISIBLE);
            separator6.setVisibility(View.VISIBLE);
            separator7.setVisibility(View.INVISIBLE);
        }
        else if (type==7){
            adapter=new ListViewAdapters(this, others_items_l, Boolean.TRUE);
            //adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,others_items_l);
            separator1.setVisibility(View.INVISIBLE);
            separator2.setVisibility(View.INVISIBLE);
            separator3.setVisibility(View.INVISIBLE);
            separator4.setVisibility(View.INVISIBLE);
            separator5.setVisibility(View.INVISIBLE);
            separator6.setVisibility(View.INVISIBLE);
            separator7.setVisibility(View.VISIBLE);
        }
        // Create listview
        listview_items.setAdapter(adapter);
    }


    public void update_ShoppingList(String list){
        try{
            int i = 0;
            JSONObject json_obj = new JSONObject(list);
            Iterator<String> keys = json_obj.keys();
            while (keys.hasNext()) {
                String type = String.valueOf(keys.next());
                switch (type){
                    case "Everything":
                        JSONArray products = json_obj.getJSONArray(type);
                        while (i<products.length()){
                            JSONArray rec = products.getJSONArray(i);
                            temp=new HashMap<String, String>();
                            if (rec.getString(2).equals("null")){
                                temp.put(THIRD_COLUMN, "-"+currency);
                            } else{
                                temp.put(THIRD_COLUMN, rec.getString(2)+currency);
                            }
                            temp.put(FIRST_COLUMN, rec.getString(0));
                            temp.put(SECOND_COLUMN, rec.getString(1));

                            all_items_l.add(temp);
                            i = i+1;
                            Log.v(TAG, "" + rec.toString());
                        }
                        i=0;
                        break;
                    case "Meat and Fish":
                        JSONArray products2 = json_obj.getJSONArray(type);
                        while (i<products2.length()){
                            JSONArray rec = products2.getJSONArray(i);
                            temp=new HashMap<String, String>();
                            if (rec.getString(2).equals("null")){
                                temp.put(THIRD_COLUMN, "-"+currency);
                            } else{
                                temp.put(THIRD_COLUMN, rec.getString(2)+currency);
                            }
                            temp.put(FIRST_COLUMN, rec.getString(0));
                            temp.put(SECOND_COLUMN, rec.getString(1));
                            meat_items_l.add(temp);
                            i = i+1;
                            Log.v(TAG, "" + rec.toString());
                        }
                        i=0;
                        //Log.v(TAG, "" + products.toString());
                        break;
                    case "Vegetables":
                        JSONArray products3 = json_obj.getJSONArray(type);
                        while (i<products3.length()){
                            JSONArray rec = products3.getJSONArray(i);
                            temp=new HashMap<String, String>();
                            if (rec.getString(2).equals("null")){
                                temp.put(THIRD_COLUMN, "-"+currency);
                            } else{
                                temp.put(THIRD_COLUMN, rec.getString(2)+currency);
                            }
                            temp.put(FIRST_COLUMN, rec.getString(0));
                            temp.put(SECOND_COLUMN, rec.getString(1));
                            vegetables_items_l.add(temp);
                            i = i+1;
                            Log.v(TAG, "" + rec.toString());
                        }
                        i=0;
                        //Log.v(TAG, "" + products.toString());
                        break;
                    case "Cereal":
                        JSONArray products4 = json_obj.getJSONArray(type);
                        while (i<products4.length()){
                            JSONArray rec = products4.getJSONArray(i);
                            temp=new HashMap<String, String>();
                            if (rec.getString(2).equals("null")){
                                temp.put(THIRD_COLUMN, "-"+currency);
                            } else{
                                temp.put(THIRD_COLUMN, rec.getString(2)+currency);
                            }
                            temp.put(FIRST_COLUMN, rec.getString(0));
                            temp.put(SECOND_COLUMN, rec.getString(1));
                            cereals_items_l.add(temp);
                            i = i+1;
                            Log.v(TAG, "" + rec.toString());
                        }
                        i=0;
                        //Log.v(TAG, "" + products.toString());
                        break;
                    case "Dairy":
                        JSONArray products5 = json_obj.getJSONArray(type);
                        while (i<products5.length()){
                            JSONArray rec = products5.getJSONArray(i);
                            temp=new HashMap<String, String>();
                            if (rec.getString(2).equals("null")){
                                temp.put(THIRD_COLUMN, "-"+currency);
                            } else{
                                temp.put(THIRD_COLUMN, rec.getString(2)+currency);
                            }
                            temp.put(FIRST_COLUMN, rec.getString(0));
                            temp.put(SECOND_COLUMN, rec.getString(1));
                            dairy_items_l.add(temp);
                            i = i+1;
                            Log.v(TAG, "" + rec.toString());
                        }
                        i=0;
                        //Log.v(TAG, "" + products.toString());
                        break;
                    case "Sweet":
                        JSONArray products6 = json_obj.getJSONArray(type);
                        while (i<products6.length()){
                            JSONArray rec = products6.getJSONArray(i);
                            temp=new HashMap<String, String>();
                            if (rec.getString(2).equals("null")){
                                temp.put(THIRD_COLUMN, "-"+currency);
                            } else{
                                temp.put(THIRD_COLUMN, rec.getString(2)+currency);
                            }
                            temp.put(FIRST_COLUMN, rec.getString(0));
                            temp.put(SECOND_COLUMN, rec.getString(1));
                            sweet_items_l.add(temp);
                            i = i+1;
                            Log.v(TAG, "" + rec.toString());
                        }
                        i=0;
                        //Log.v(TAG, "" + products.toString());
                        break;
                    case "Others":
                        JSONArray products7 = json_obj.getJSONArray(type);
                        while (i<products7.length()){
                            JSONArray rec = products7.getJSONArray(i);
                            temp=new HashMap<String, String>();
                            if (rec.getString(2).equals("null")){
                                temp.put(THIRD_COLUMN, "-"+currency);
                            } else{
                                temp.put(THIRD_COLUMN, rec.getString(2)+currency);
                            }
                            temp.put(FIRST_COLUMN, rec.getString(0));
                            temp.put(SECOND_COLUMN, rec.getString(1));
                            others_items_l.add(temp);
                            i = i+1;
                            Log.v(TAG, "" + rec.toString());
                        }
                        i=0;
                        //Log.v(TAG, "" + products.toString());
                        break;
                }
            }


        } catch (JSONException e){
            Log.v(TAG, "Error JSON");
            e.printStackTrace();
        }
    }

}
