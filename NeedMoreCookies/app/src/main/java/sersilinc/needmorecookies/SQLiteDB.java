package sersilinc.needmorecookies;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sergi on 18/04/16.
 */

public class SQLiteDB extends SQLiteOpenHelper{

    private static int DATABASE_VERSION = 1;
    private static String DATABASE_NAME = "Shopping_Lists";

    public SQLiteDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        final String Shopping_list_table_name = "Shopping_List";
        final String Items_table_name = "Items";
        final String List_table_name = "List";
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

    }
}
