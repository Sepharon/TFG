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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
/**
 * CHange Public for shared?
 */

public class AddList extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private GoogleApiClient mGoogleApiClient;

    Button add_friend,save;
    EditText list_name;
    CheckBox pub,priv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_list);
        //Items on Layout
        list_name = (EditText) findViewById(R.id.list_name);
        priv = (CheckBox) findViewById(R.id.private_checkBox2);
        pub = (CheckBox) findViewById(R.id.public_checkBox);
        add_friend = (Button) findViewById(R.id.add_friends);
        save = (Button) findViewById(R.id.save);

        //Navigation
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_list);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_list);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_list);
        navigationView.setNavigationItemSelectedListener(this);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        // TODO: check checkboxes status
        // If this is clicked start new activity displaying a listview of all current friends
        add_friend.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO : START NEW ACTIVITY
        }
    });
        // If save is clicked save data to server and return it
        save.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Save list name, and if its public or private
            if (pub.isChecked() || priv.isChecked()) {
                Intent result_data = new Intent();
                result_data.putExtra("List_Name", list_name.getText().toString());
                result_data.putExtra("Type", priv.isChecked());
                setResult(MainActivity.RESULT_OK, result_data);
                finish();
                // TODO: SEND DATA TOT SERVER
            }
            else Toast.makeText(AddList.this,"You must choose a public or private list", Toast.LENGTH_LONG).show();
        }
        });

        pub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pub.isChecked() && priv.isChecked()) priv.toggle();
            }
        });

        priv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (priv.isChecked() && pub.isChecked()) pub.toggle();
            }
        });
    }


    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_list);
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
            Intent intent = new Intent(AddList.this,MapsActivity.class);
            // Start next activity
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(AddList.this, SettingsActivity.class);
            // Start next activity
            startActivity(intent);
            finish();
        }
        else if (id == R.id.nav_home) {
            Intent intent = new Intent(AddList.this,MainActivity.class);
            // Start next activity
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_share) {
            Intent intent = new Intent(AddList.this,Items.class);
            // Start next activity
            startActivity(intent);
            finish();
            //share();

        } else if (id == R.id.nav_logout) {
            signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_list);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //Sign Out from Google Account
    public void signOut() {
        User_Info usr_inf;
        usr_inf = User_Info.getInstance();
        Log.v("GOAPICLIENT2", "" + usr_inf.getmAPIClient());
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                Intent intent = new Intent(AddList.this, Login.class);
                // Start next activity
                startActivity(intent);
                finish();
            }
        });
    }

}
