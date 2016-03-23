package sersilinc.needmorecookies;

import android.content.Intent;
import android.os.Bundle;
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
import android.widget.HorizontalScrollView;
import android.widget.ListView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.util.ArrayList;
import java.util.List;

//For now it is called when you press Share on the MainActivity

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
    private ListView listview_items;
    // Lists
    List<String> all_items_l = new ArrayList<>();
    List<String> meat_items_l = new ArrayList<>();
    List<String> vegetables_items_l = new ArrayList<>();
    List<String> cereals_items_l = new ArrayList<>();
    List<String> dairy_items_l = new ArrayList<>();
    List<String> sweet_items_l = new ArrayList<>();
    List<String> others_items_l = new ArrayList<>();
    // Adapter
    ArrayAdapter<String> adapter;
    //Google API client

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

        meat_items_l.add("HOLA");

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,all_items_l);
        // Create listview
        listview_items.setAdapter(adapter);



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
            finish();
        }
        else if (id == R.id.nav_home) {
            Intent intent = new Intent(Items.this,MainActivity.class);
            // Start next activity
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(Items.this,SettingsActivity.class);
            // Start next activity
            startActivity(intent);
            finish();
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
        User_Info usr_inf;
        usr_inf = User_Info.getInstance();
        //Log.v("GOAPICLIENT2", "" + usr_inf.getmAPIClient());
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
            adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,all_items_l);
            separator1.setVisibility(View.VISIBLE);
            separator2.setVisibility(View.INVISIBLE);
            separator3.setVisibility(View.INVISIBLE);
            separator4.setVisibility(View.INVISIBLE);
            separator5.setVisibility(View.INVISIBLE);
            separator6.setVisibility(View.INVISIBLE);
            separator7.setVisibility(View.INVISIBLE);
        }
        else if (type==2){
            adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,meat_items_l);
            separator1.setVisibility(View.INVISIBLE);
            separator2.setVisibility(View.VISIBLE);
            separator3.setVisibility(View.INVISIBLE);
            separator4.setVisibility(View.INVISIBLE);
            separator5.setVisibility(View.INVISIBLE);
            separator6.setVisibility(View.INVISIBLE);
            separator7.setVisibility(View.INVISIBLE);
        }
        else if (type==3){
            adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,vegetables_items_l);
            separator1.setVisibility(View.INVISIBLE);
            separator2.setVisibility(View.INVISIBLE);
            separator3.setVisibility(View.VISIBLE);
            separator4.setVisibility(View.INVISIBLE);
            separator5.setVisibility(View.INVISIBLE);
            separator6.setVisibility(View.INVISIBLE);
            separator7.setVisibility(View.INVISIBLE);
        }
        else if (type==4){
            adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,cereals_items_l);
            separator1.setVisibility(View.INVISIBLE);
            separator2.setVisibility(View.INVISIBLE);
            separator3.setVisibility(View.INVISIBLE);
            separator4.setVisibility(View.VISIBLE);
            separator5.setVisibility(View.INVISIBLE);
            separator6.setVisibility(View.INVISIBLE);
            separator7.setVisibility(View.INVISIBLE);
        }
        else if (type==5){
            adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,dairy_items_l);
            separator1.setVisibility(View.INVISIBLE);
            separator2.setVisibility(View.INVISIBLE);
            separator3.setVisibility(View.INVISIBLE);
            separator4.setVisibility(View.INVISIBLE);
            separator5.setVisibility(View.VISIBLE);
            separator6.setVisibility(View.INVISIBLE);
            separator7.setVisibility(View.INVISIBLE);
        }
        else if (type==6){
            adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,sweet_items_l);
            separator1.setVisibility(View.INVISIBLE);
            separator2.setVisibility(View.INVISIBLE);
            separator3.setVisibility(View.INVISIBLE);
            separator4.setVisibility(View.INVISIBLE);
            separator5.setVisibility(View.INVISIBLE);
            separator6.setVisibility(View.VISIBLE);
            separator7.setVisibility(View.INVISIBLE);
        }
        else if (type==7){
            adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,others_items_l);
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
}
