package sersilinc.needmorecookies;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class Update_List extends Service {

    private final String TAG = "Update List Service: ";

    private final IBinder mBinder = new Update_List_Binder();
    public Update_List() {
    }

    public class Update_List_Binder extends Binder {
        public Update_List getService(){
            return Update_List.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG,"onBind function");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG,"Created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        Log.v(TAG,"StartCommand");


        return START_STICKY;
    }

   // public get_updates()
}
