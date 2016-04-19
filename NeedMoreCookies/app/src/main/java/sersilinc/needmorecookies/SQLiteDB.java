package sersilinc.needmorecookies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.List;

// TODO : CHECK IF NECESSARY TO STORE TIMESTAMP IN INTERNAL DB
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
    public final String KEY_PUBLIC = "Public";
    // Items
    public final String KEY_ID_ITEM = "ID_Item";
    public final String KEY_PRODUCT = "Product";
    public final String KEY_TYPE = "Type";
    public final String KEY_QUANTITY = "Quantity";
    public final String KEY_PRICE = "Price";
    // Others
    public final String KEY_CODE = "Code";
    public final String KEY_FLAG = "Flag";
    public final String KEY_CHANGE_TYPE = "Change_type";

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
                "Public INTEGER, " +
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
        long update_time = System.currentTimeMillis();
        Log.v(TAG,"Creating new list at " + update_time);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_LIST_NAME,list_name);
        values.put(KEY_UPDATE,update_time);
        values.put(KEY_FLAG,1);
        values.put(KEY_PUBLIC,Public);
        values.put(KEY_CHANGE_TYPE,"new_list");
        // Insert values
        long newRowID = db.insert(Shopping_list_table_name,null,values);
        db.close();
        values.clear();
        return newRowID;
    }

    // Update list name
    public int update_list(String[] key_value,String code){
        long update_time = System.currentTimeMillis();
        Log.v(TAG,"Changing list name at " + update_time);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // New name might be empty, meaning we just want to update timestamp
        if (key_value != null)
            values.put(key_value[0], key_value[1]);
        values.put(KEY_UPDATE,update_time);
        int count = db.update(
                Shopping_list_table_name,
                values,
                KEY_CODE+" = ?",
                new String[]{code});

        db.close();
        values.clear();
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
    // Reads the values from shopping list table
    public String read_shopping_lists(String query){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query,null);
        if (cursor == null) return null;
        cursor.moveToFirst();
        String result = cursor.getString(0);
        cursor.close();
        return result;
    }

    public Cursor read_multiple_entries(String query){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query,null);
        if (cursor == null) return null;
        return cursor;
    }

    public long add_new_item(String Product, String Type, String Quantity, String Price){
        long update_time = System.currentTimeMillis();
        long newRowID;
        Log.v(TAG,"Creating new item at " + update_time);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_PRODUCT,Product);
        values.put(KEY_TYPE,Type);
        values.put(KEY_QUANTITY,Quantity);
        values.put(KEY_PRICE,Price);
        values.put(KEY_FLAG,1);
        values.put(KEY_CHANGE_TYPE,"new_item");
        newRowID = db.insert(Items_table_name,null,values);

        db.close();
        values.clear();

        return newRowID;
    }

    public int update_item(String change_type,String key,String new_value,String code){
        long update_time = System.currentTimeMillis();
        Log.v(TAG,"Updating item at " + update_time);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        Log.v(TAG,"Updating " + key + " with " + new_value);
        values.put(key,new_value);
        values.put(KEY_FLAG,1);
        values.put(KEY_CHANGE_TYPE,change_type);
        int count = db.update(Items_table_name,
                values,
                KEY_CODE + "=?",
                new String[]{code});
        values.clear();
        db.close();
        // Number of affected rows
        return count;
    }
    // Delete list
    public void delete_item(String code){
        long update_time = System.currentTimeMillis();
        Log.v(TAG,"Erasing item at " + update_time);
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Items_table_name,
                KEY_CODE + "= ?",
                new String[]{code});
        db.close();
    }

    // Reads the values from item table
    public String read_item(String query){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query,null);
        if (cursor == null) return null;
        cursor.moveToFirst();
        String result = cursor.getString(0);
        cursor.close();
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
    public void delete_list_relation(int ID_Item){
        long update_time = System.currentTimeMillis();
        Log.v(TAG,"Erasing item at " + update_time);
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(List_table_name,
                KEY_ID_ITEM + "= ?",
                new String[]{String.valueOf(ID_Item)});
        db.close();
    }

}
