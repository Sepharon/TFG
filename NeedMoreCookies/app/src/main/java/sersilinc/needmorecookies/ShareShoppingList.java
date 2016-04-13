package sersilinc.needmorecookies;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ShareShoppingList extends Service {
    // TAG
    private final String TAG = "Share Shopping List";
    // Binder
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        ShareShoppingList getService(){
            return ShareShoppingList.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG,"Binding");
        return mBinder;
    }
}
