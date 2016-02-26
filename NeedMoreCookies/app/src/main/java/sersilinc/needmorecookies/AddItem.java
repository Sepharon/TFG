package sersilinc.needmorecookies;

import android.content.Context;
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

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class AddItem extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private GoogleApiClient mGoogleApiClient;

    EditText List_name;
    CheckBox priv,pub;
    Button   add_friends,save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
    }






    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_item);
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
            Intent intent = new Intent(AddItem.this,MapsActivity.class);
            // Start next activity
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(AddItem.this, SettingsActivity.class);
            // Start next activity
            startActivity(intent);
            finish();
        }
        else if (id == R.id.nav_home) {
            Intent intent = new Intent(AddItem.this,MainActivity.class);
            // Start next activity
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_share) {
            Intent intent = new Intent(AddItem.this,Items.class);
            // Start next activity
            startActivity(intent);
            finish();
            //share();

        } else if (id == R.id.nav_logout) {
            signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_item);
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
                Intent intent = new Intent(AddItem.this, Login.class);
                // Start next activity
                startActivity(intent);
                finish();
            }
        });
    }

}
