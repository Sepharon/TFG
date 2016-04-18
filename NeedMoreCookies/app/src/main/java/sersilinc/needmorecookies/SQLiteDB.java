package sersilinc.needmorecookies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.Settings;
import android.util.Log;

/**
 * Created by sergi on 18/04/16.
 */

public class SQLiteDB extends SQLiteOpenHelper{

    private final String TAG = "SQLiteDB";

    private static int DATABASE_VERSION = 1;
    private static String DATABASE_NAME = "Shopping_Lists";

    // Tables names
    private static final String Shopping_list_table_name = "Shopping_List";
    private static final String Items_table_name = "Items";
    private static final String List_table_name = "List";

    // Table values
    // Shopping List
    private final String KEY_ID_LIST = "ID_List";
    private final String KEY_LIST_NAME = "List_Name";
    private final String KEY_UPDATE = "Last_Update";
    // Items
    private final String KEY_ID_ITEM = "ID_Item";
    private final String KEY_PRODUCT = "Product";
    private final String KEY_TYPE = "Type";
    private final String KEY_QUANTITY = "Quantity";
    private final String KEY_PRICE = "Price";
    // Others
    private final String KEY_CODE = "Code";
    private final String KEY_FLAG = "Flag";
    private final String KEY_CHANGE_TYPE = "Change_type";

    public SQLiteDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // Prepare query for first table
        String SHOPPING_LIST_TABLE = "CREATE TABLE " + Shopping_list_table_name + " ( " +
                "ID_List INTEGER PRIMARY KEY NOT NULL AUTOINCREMENT, " +
                "List_Name TEXT NOT NULL, " +
                "Last_Update TEXT NOT NULL, " +
                "Code TEXT, " +
                "Flag INTEGER DEFAULT 1, " +
                "Change_type TEXT)";
        // Create table
        db.execSQL(SHOPPING_LIST_TABLE);
        // Prepare query for second table
        String ITEMS_TABLE = "CREATE TABLE " + Items_table_name + " ( " +
                "ID_Item INTEGER PRIMARY KEY NOT NULL AUTOINCREMENT, " +
                "Product TEXT NOT NULL, " +
                "Type TEXT NOT NULL, " +
                "Quantity TEXT NOT NULL, " +
                "Price TEXT, " +
                "Code TEXT, " +
                "Flag INTEGER DEFAULT 1, " +
                "Change_type TEXT)";
        db.execSQL(ITEMS_TABLE);
        String LIST_TABLE = "CREATE TABLE " + List_table_name + " ( " +
                "ID_List INTEGER PRIMARY KEY, " +
                "ID_Item INTEGER PRIMARY KEY, " +
                "FOREIGN KEY (ID_List) REFERENCES" + Shopping_list_table_name + "(ID_List)," +
                "FOREIGN KEY (ID_Ite,) REFERENCES" + Items_table_name + "(ID_Item))";
        db.execSQL(LIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Shopping_list_table_name);
        db.execSQL("DROP TABLE IF EXISTS " + Items_table_name);
        db.execSQL("DROP TABLE IF EXISTS " + List_table_name);
        onCreate(db);
        DATABASE_VERSION = newVersion;
    }
    // TODO : Code might be needed to eb created in app instead of server
    // Add a new list to the DB
    public long add_new_list(String list_name){
        // Get current time since epoch
        long update_time = System.currentTimeMillis();
        Log.v(TAG,"Creating new list at " + update_time);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_LIST_NAME,list_name);
        values.put(KEY_UPDATE,update_time);
        values.put(KEY_FLAG,1);
        values.put(KEY_CHANGE_TYPE,"new_list");
        // Insert values
        long newRowID = db.insert(Shopping_list_table_name,null,values);
        db.close();
        return newRowID;
    }

    // Update list name
    public int update_list(String new_name,String code){
        long update_time = System.currentTimeMillis();
        Log.v(TAG,"Changing list name at " + update_time);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_LIST_NAME,new_name);
        values.put(KEY_UPDATE,update_time);
        int count = db.update(
                Shopping_list_table_name,
                values,
                KEY_CODE+" = ?",
                new String[]{code});

        db.close();
        return count;
    }
    // Delete list
    public void delete_list(String code){
        long update_time = System.currentTimeMillis();
        Log.v(TAG,"Erasing list at " + update_time);
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Shopping_list_table_name,
                KEY_CODE + "= ?",
                new String[]{code});
        db.close();
    }
    // Reads the values from
    public String read_shopping_lists(String query){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query,null);
        if (cursor == null) return "";
        cursor.moveToFirst();
        return cursor.getString(0);
    }
}
