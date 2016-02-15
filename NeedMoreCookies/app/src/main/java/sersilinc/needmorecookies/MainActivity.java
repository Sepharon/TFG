package sersilinc.needmorecookies;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.gms.auth.api.signin.GoogleSignInOptions.Builder;

public class MainActivity extends AppCompatActivity {
    // call functions from service usuing data.function_name()
    //Weather_Data data;
    Messenger mService = null;
    boolean is_bound = false;


    TextView Data1;
    TextView Data2;


    String data1="";
    String data2="";


    Button b;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (is_bound) {
            unbindService(mConnection);
            is_bound = false;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.sign_in_button:
                        signIn();
                        break;
                    // ...
                }

            }
        });

        Intent intent = new Intent(this, RequestGet.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter("miss_temps");
        this.registerReceiver(new MyReceiver(), filter);

        Data1 = (TextView)findViewById(R.id.data1);
        Data2 = (TextView)findViewById(R.id.data2);


        // Sending data to Service
        Log.v("Activity:", "Sending message");
        b = (Button) findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_bound) {
                    //if (!is_bound) return;
                    // Create and send a message to the service, using a supported 'what' value
                    Log.v("Activity:", "Getting ready");
                    Message msg = Message.obtain(null, RequestGet.MSG_GET_DATA);
                    Bundle bundle = new Bundle();


                    msg.setData(bundle);
                    try {
                        mService.send(msg);
                        Log.v("Activity:", "Message sent");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    // Done sending data
                }
            }
        });


        new CountDownTimer(300000, 1000) { //5min
            public void onTick(long millisUntilFinished) {}
            public void onFinish() {
                //Write function to be called
                reload();
                start();
            }
        }.start();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_settings) {
            //openAbout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    public void openAbout() {
        Intent intent = new Intent(MainActivity.this, About.class);
        startActivity(intent);
    }
*/

    // Binding function
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.v("Main Activity:", "Binding service");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            //Weather_Data.LocalBinder binder = (Weather_Data.LocalBinder) service;
            //data = binder.getService();
            mService = new Messenger(service);
            is_bound = true;
        }


        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            is_bound = false;
        }
    };

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            data1 = intent.getStringExtra("data1");
            data2 = intent.getStringExtra("data2");

            Data1.setText(data1);
            Data2.setText(data2);

            Log.v("Activity One result", data1);
        }
    }

    public void reload(){
        Data1.setText(data1);
        Data2.setText(data2);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }


}
