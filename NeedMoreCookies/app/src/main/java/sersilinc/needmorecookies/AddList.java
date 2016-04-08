package sersilinc.needmorecookies;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class AddList extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //GoogleApiClient
    private GoogleApiClient mGoogleApiClient;

    //UI elements
    private Button add_friend;
    private ImageButton save;
    private EditText list_name;
    private Switch pub, priv;

    //Others
    private boolean first_time = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_list);

        /**[START UI elements]**/
        list_name = (EditText) findViewById(R.id.list_name);
        priv = (Switch) findViewById(R.id.private_switch);
        pub = (Switch) findViewById(R.id.public_switch);
        //add_friend = (Button) findViewById(R.id.add_friends);
        save = (ImageButton) findViewById(R.id.save);
        /**[END UI elements]**/

        /**[START Navigation]**/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_list);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_list);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_list);
        navigationView.setNavigationItemSelectedListener(this);
        /**[END Navigation]**/


        /**[START GoogleApiClient]**/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        /**[END GoogleApiClient]**/

        /**[START onCLickListeners]**/
        // If save is clicked save data to server and return it
        save.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Save list name, and if its public or private
            if (!list_name.getText().toString().equals("")) {
                if (pub.isChecked() || priv.isChecked()) {
                    Log.v("AddList: ", "Returning data");
                    Intent result_data = new Intent();
                    result_data.putExtra("List_Name", list_name.getText().toString());
                    result_data.putExtra("Type", "" + priv.isChecked());
                    setResult(MainActivity.RESULT_OK, result_data);
                    finish();
                } else
                    Toast.makeText(AddList.this, "You must choose a public or private list", Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(AddList.this, R.string.no_name_toast, Toast.LENGTH_LONG).show();
        }
        });

        pub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pub.isChecked() && !priv.isChecked()){
                    first_time = true;
                    save.setVisibility(View.GONE);
                }
                if (pub.isChecked() && first_time) start_animation();
                if (pub.isChecked() && priv.isChecked()) priv.toggle();

            }
        });

        priv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pub.isChecked() && !priv.isChecked()){
                    first_time = true;
                    save.setVisibility(View.GONE);
                }
                if (priv.isChecked() && first_time) start_animation();
                if (priv.isChecked() && pub.isChecked()) pub.toggle();

            }
        });
        /**[END onClickListeners]**/
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
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_locations) {
            Intent intent = new Intent(AddList.this,MapsActivity.class);
            // Start next activity
            startActivity(intent);
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(AddList.this, SettingsActivity.class);
            // Start next activity
            startActivity(intent);
        }
        else if (id == R.id.nav_home) {
            Intent intent = new Intent(AddList.this,MainActivity.class);
            // Start next activity
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            //Intent intent = new Intent(AddList.this,Items.class);
            // Start next activity
            //startActivity(intent);
            //share();

        } else if (id == R.id.nav_logout) {
            signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_list);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void start_animation(){
        first_time = false;
        Animation fadein = new AlphaAnimation(0,1);
        fadein.setDuration(1000);
        save.setVisibility(View.VISIBLE);
        save.setAnimation(fadein);
    }


    //Sign Out from Google Account
    private void signOut() {
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
