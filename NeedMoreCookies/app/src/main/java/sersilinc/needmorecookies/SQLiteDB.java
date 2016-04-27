package sersilinc.needmorecookies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

public class SQLiteDB extends SQLiteOpenHelper{

    private final String TAG = "SQLiteDB";

    private static int DATABASE_VERSION = 1;
    private static String DATABASE_NAME = "Shopping_Lists";

    // Tables names
    public static final String Shopping_list_table_name = "Shopping_List";
    public static final String Items_table_name = "Items";
    public static final String List_table_name = "List";

    // Table values
    // Shopping List
    public final String KEY_ID_LIST = "ID_List";
    public final String KEY_LIST_NAME = "List_Name";
    public final String KEY_UPDATE = "Last_Update";
    public final String KEY_PUBLIC = "Private";
    // Items
    public final String KEY_ID_ITEM = "ID_Item";
    public final String KEY_PRODUCT = "Product";
    public final String KEY_TYPE = "Type";
    public final String KEY_QUANTITY = "Quantity";
    public final String KEY_PRICE = "Price";
    public final String KEY_CODE_LIST = "Code_List";
    public final String KEY_LAST_USER = "Last_User";

    // Others
    public final String KEY_CODE = "Code";
    public final String KEY_FLAG = "Flag";
    public final String KEY_CHANGE_TYPE = "Change_type";

    public SQLiteDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v(TAG,"Creating DB");
        // Prepare query for first table
        String SHOPPING_LIST_TABLE = "CREATE TABLE " + Shopping_list_table_name + " ( " +
                "ID_List INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "List_Name TEXT NOT NULL, " +
                "Last_Update TEXT NOT NULL, " +
                "Code TEXT UNIQUE, " +
                "Private INTEGER, " +
                "Flag INTEGER DEFAULT 1, " +
                "Change_type TEXT)";
        // Create table
        db.execSQL(SHOPPING_LIST_TABLE);
        // Prepare query for second table
        String ITEMS_TABLE = "CREATE TABLE " + Items_table_name + " ( " +
                "ID_Item INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Product TEXT NOT NULL, " +
                "Type TEXT NOT NULL, " +
                "Quantity TEXT NOT NULL, " +
                "Price TEXT, " +
                "Code TEXT UNIQUE, " +
                "Flag INTEGER DEFAULT 0, " +
                "Change_type TEXT, "+
                "Code_List TEXT NOT NULL, "+
                "Last_User TEXT)";
        db.execSQL(ITEMS_TABLE);
        String LIST_TABLE = "CREATE TABLE " + List_table_name + " ( " +
                "ID_List INTEGER PRIMARY KEY, " +
                "ID_Item INTEGER, " +
                "FOREIGN KEY (ID_List) REFERENCES " + Shopping_list_table_name + " (ID_List), " +
                "FOREIGN KEY (ID_Item) REFERENCES " + Items_table_name + " (ID_Item))";
        db.execSQL(LIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG,"THIS WILL DELETE THE CONTENTS OF THE DB");
        db.execSQL("DROP TABLE IF EXISTS " + Shopping_list_table_name);
        db.execSQL("DROP TABLE IF EXISTS " + Items_table_name);
        db.execSQL("DROP TABLE IF EXISTS " + List_table_name);
        onCreate(db);
        DATABASE_VERSION = newVersion;
    }
    // TODO : Code might be needed to eb created in app instead of server
    // Add a new list to the DB
    public long add_new_list(String list_name,int Public){
        // Get current time since epoch
        //long update_time = System.currentTimeMillis();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        Log.v(TAG,"Creating new list at " + timeStamp);
        Log.v(TAG,"With name: " + list_name);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_LIST_NAME,list_name);
        values.put(KEY_UPDATE,timeStamp);
        if (User_Info.getInstance().getOffline_mode()) {
            Log.v(TAG,"We are offline");
            values.put(KEY_FLAG, 1);
        }
        else
            values.put(KEY_FLAG,0);
        /* For the time being we generate a temporary code,
        this code will be changed once the list is sent to the server
        */
        values.put(KEY_CODE, UUID.randomUUID().toString().replaceAll("-", ""));
        values.put(KEY_PUBLIC,Public);
        values.put(KEY_CHANGE_TYPE,"new_list");
        // Insert values
        db.beginTransaction();
        long newRowID = db.insert(Shopping_list_table_name,null,values);
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        values.clear();
        return newRowID;
    }

    // Update list name
    public int update_list(String[] key_value,String code){
        Log.d(TAG,"Updating list: " + code);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // New name might be empty, meaning we just want to update timestamp

        values.put(key_value[0], key_value[1]);
        db.beginTransaction();
        int count = db.update(
                Shopping_list_table_name,
                values,
                KEY_CODE+" = ?",
                new String[]{code});
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        values.clear();
        Log.v(TAG,"count: " + count);
        return count;
    }
    // Delete list
    public void delete_list(String code){
        long update_time = System.currentTimeMillis();
        Log.v(TAG,"Erasing list at " + update_time);
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        db.delete(Shopping_list_table_name,
                KEY_CODE + "= ?",
                new String[]{code});
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

    }
    // Reads the values from shopping list table
    public String read_shopping_lists(String query){
        String result;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query,null);
        if (cursor == null) return null;
        cursor.moveToFirst();
        try {
        result = cursor.getString(0);
        } catch(android.database.CursorIndexOutOfBoundsException e){
            return null;
        } finally {
            cursor.close();
        }
        return result;
    }

    public Cursor read_multiple_entries(String query){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query,null);
        if (cursor == null) return null;
        return cursor;
    }

    public long add_new_item(String Product, String Type, String Quantity, String Price, String Code_list, String user){
        long update_time = System.currentTimeMillis();
        long newRowID;
        Log.v(TAG,"Creating new item at " + update_time);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();


        values.put(KEY_CODE, UUID.randomUUID().toString().replaceAll("-", ""));

        values.put(KEY_PRODUCT,Product);
        values.put(KEY_TYPE,Type);
        values.put(KEY_QUANTITY,Quantity);
        values.put(KEY_PRICE,Price);
        values.put(KEY_CODE_LIST, Code_list);
        values.put(KEY_LAST_USER, user);
        if (User_Info.getInstance().getOffline_mode()) {
            Log.v(TAG,"We are offline");
            values.put(KEY_FLAG, 1);
        }
        else
            values.put(KEY_FLAG,0);
        values.put(KEY_CHANGE_TYPE,"new_item");
        db.beginTransaction();
        newRowID = db.insert(Items_table_name,null,values);
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        values.clear();

        return newRowID;
    }

    public int update_item(String[] key_value,String code){
        long update_time = System.currentTimeMillis();
        Log.v(TAG,"Updating item at " + update_time);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        Log.v(TAG,"Updating " + key_value[0] + " with " + key_value[1]);
        values.put(key_value[0],key_value[1]);
        db.beginTransaction();
        int count = db.update(Items_table_name,
                values,
                KEY_CODE + "=?",
                new String[]{code});
        db.setTransactionSuccessful();
        db.endTransaction();
        values.clear();
        db.close();
        // Number of affected rows
        return count;
    }
    // Delete item
    public void delete_item(String code){
        long update_time = System.currentTimeMillis();
        Log.v(TAG,"Erasing item at " + update_time);
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        db.delete(Items_table_name,
                KEY_CODE + "= ?",
                new String[]{code});
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    // Delete all items of a shopping list
    public void delete_all_items(String code_list){
        long update_time = System.currentTimeMillis();
        Log.v(TAG,"Erasing item at " + update_time);
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        db.delete(Items_table_name,
                KEY_CODE_LIST + "= ?",
                new String[]{code_list});
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }


    // Reads the values from item table
    public String read_item(String query){
        String result;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query,null);
        if (cursor == null) return null;
        cursor.moveToFirst();
        try {
            result = cursor.getString(0);
        } catch(android.database.CursorIndexOutOfBoundsException e){
            return null;
        } finally {
            cursor.close();
        }
        return result;
    }


    public long new_list(int ID_List, int ID_Item){
        long newRowID;
        Log.v(TAG,"Creating new item list relation at ");

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_ID_LIST,ID_List);
        values.put(KEY_ID_ITEM,ID_Item);

        newRowID = db.insert(List_table_name,
                null,
                values);
        return  newRowID;
    }

    // Delete relation
    public void delete_list_relation(int ID){
        long update_time = System.currentTimeMillis();
        Log.v(TAG,"Erasing item at " + update_time);
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(List_table_name,
                KEY_ID_ITEM + "= ?",
                new String[]{String.valueOf(ID)});
        db.close();
    }

}
