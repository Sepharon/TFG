package sersilinc.needmorecookies;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by sergi on 19/04/16.
 * The aim of this class is to help other classes deal with the DB by offering more high end functions
 * rather than the low end functions found in the SQLiteDB class
 */

public class DB_Helper {

    private final String TAG = "DB_Helper";

    private static SQLiteDB DataBase;

    public DB_Helper(Context context) {
        Log.v(TAG,"Initializing DB_Helper");
        DataBase = new SQLiteDB(context);
    }

    public void destroy_class(){
        DataBase = null;
    }

    public String add_new_list(String new_list,int type){
        Log.v(TAG,"added new list with name: "+new_list);
        DataBase.add_new_list(new_list,type);
        return get_code_last_list();
    }

    public boolean update_list_name(String new_name,String code){
        int result;
        Log.d(TAG,"Updating name: " + new_name + " "+ code);
        String flag_status;
        flag_status = read_shopping_list(2,code);
        // List still need to synchronize
        if (flag_status.equals("0") && User_Info.getInstance().getOffline_mode()){
            Log.d(TAG,"Flag is 0");
            DataBase.update_list(new String[]{DataBase.KEY_CHANGE_TYPE, "change_list_name"},code);
            DataBase.update_list(new String[]{DataBase.KEY_FLAG,"1"},code);
        }
        result = DataBase.update_list(new String[]{DataBase.KEY_LIST_NAME, new_name},code);
        return result != 0;
    }

    public boolean update_list_code(String new_code,String code){
        Log.d(TAG,"Updating code");
        int result = DataBase.update_list(new String[]{DataBase.KEY_CODE, new_code},code);
        return result!=0;
    }

    public boolean update_list_public(int pub,String code){
        Log.d(TAG,"Updating code");
        int result = DataBase.update_list(new String[]{DataBase.KEY_PUBLIC, String.valueOf(pub)},code);
        return result!=0;
    }

    public boolean update_list_change(String new_change,String code){
        Log.d(TAG,"Updating list change type to: " + new_change);
        int result  = DataBase.update_list(new String[]{DataBase.KEY_CHANGE_TYPE, new_change},code);
        int result2 = DataBase.update_list(new String[]{DataBase.KEY_FLAG,"1"},code);
        return (result&result2)!=0;
    }

    public boolean update_timestamp_android(String code){
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        Log.v(TAG,"Changing list name at " + timeStamp);
        int result = DataBase.update_list(new String[] {DataBase.KEY_UPDATE,String.valueOf(timeStamp)},code);
        if (!User_Info.getInstance().getOffline_mode())
            DataBase.update_list(new String[]{DataBase.KEY_FLAG,"1"},code);
        return result!=0;
    }
    public boolean update_timestamp_server(String code,String time){
        Log.v(TAG,"Changing list name at " + time);
        int result = DataBase.update_list(new String[] {DataBase.KEY_UPDATE,String.valueOf(time)},code);
        return result!=0;
    }

    public boolean set_list_flag(String code, int flag){
        if (!User_Info.getInstance().getOffline_mode() && flag == 1) return false;
        int result = DataBase.update_list(new String[]{DataBase.KEY_FLAG,"" + flag},code);
        return result!=0;
    }

    public void delete_list(String code){
        String ID_List = read_shopping_list(0,code);
        if (ID_List.equals("Error"))
            ID_List = "-1";
        DataBase.delete_list(code);
        DataBase.delete_list_relation(Integer.parseInt(ID_List));
    }
    public String get_code_last_list(){
        String query = "SELECT " + DataBase.KEY_CODE + " FROM " +DataBase.Shopping_list_table_name +
                " WHERE ID_List=(SELECT MAX(ID_List) FROM " + DataBase.Shopping_list_table_name +")";
        return DataBase.read_shopping_lists(query);
    }

    public String read_code(String list_name){
        String query = "SELECT " + DataBase.KEY_CODE + " FROM " + DataBase.Shopping_list_table_name
                + " WHERE " + DataBase.KEY_LIST_NAME + String.format("='%s'",list_name);
        String result = DataBase.read_shopping_lists(query);
        return result;
    }
    public String read_shopping_list(int value,String code){
        String table_name = SQLiteDB.Shopping_list_table_name;
        String key,query,result;
        switch(value) {
            case 0:
                key = DataBase.KEY_ID_LIST;
                break;
            case 1:
                key = DataBase.KEY_LIST_NAME;
                break;
            case 2:
                key = DataBase.KEY_FLAG;
                break;
            case 3:
                key = DataBase.KEY_CHANGE_TYPE;
                break;
            case 4:
                key = DataBase.KEY_UPDATE;
                break;
            case 5:
                key = DataBase.KEY_PUBLIC;
                break;
            default:
                return null;
        }

        query = "SELECT " + key + " FROM " + table_name + " WHERE " + DataBase.KEY_CODE + String.format("='%s'",code);
        try {
            result = DataBase.read_shopping_lists(query);
            if (result == null) return "Error";
        } catch(android.database.CursorIndexOutOfBoundsException e){
            return "Error";
        }
        return result;
    }
    public List<String[]> read_all_lists(){
        String table_name = SQLiteDB.Shopping_list_table_name;
        String query;
        List<String[]> r = new ArrayList<>();

        Cursor result;
        query = "SELECT * FROM " + table_name;
        result = DataBase.read_multiple_entries(query);
        result.moveToFirst();
        try {
            do {
                Log.d(TAG,"type: " + result.getString(3));
                if (!result.getString(6).equals("delete_list")) {
                    // list name,timestamp , code,Public
                    String[] entry = new String[]{result.getString(1), result.getString(2), result.getString(3),
                            result.getString(4),result.getString(5)};
                    r.add(entry);
                }
            } while (result.moveToNext());
        } catch (android.database.CursorIndexOutOfBoundsException e){
            e.printStackTrace();
            Log.w(TAG,"Empty DB");
        }
        return r;
    }
    public List<String[]> read_all_with_flag_set_list(){
        String table_name = SQLiteDB.Shopping_list_table_name;
        String query;
        List<String[]> r = new ArrayList<>();

        Cursor result;
        query = "SELECT * FROM " + table_name + " WHERE " +DataBase.KEY_FLAG + " = 1";
        result = DataBase.read_multiple_entries(query);
        result.moveToFirst();
        try {
            do {
                // list name, code,Public, change type
                String [] entry = new String[]{result.getString(1),result.getString(3),result.getString(4),
                        result.getString(6)};
                r.add(entry);
            } while (result.moveToNext());
        } catch (android.database.CursorIndexOutOfBoundsException e){
            e.printStackTrace();
            Log.w(TAG,"Empty DB");
        }
        Log.v(TAG,"read_all_flag " + r.toString());
        return r;
    }
    // Afegeix un item nou i retorna el codi de aquest. Tambe afegeix una relacio entre llista i item
    public String add_new_item(String Product, String Type, String Quantity, String Price,String shopping_list_code,String user,String Code_item){
        String ID_List,ID_Item;

        //ID_List = read_shopping_list(0,shopping_list_code);
        //if (ID_List.equals("Error")) return "Error";
        // Add new item
        long result;
        if (Code_item.equals("")) {
            result = DataBase.add_new_item(Product, Type, Quantity, Price, shopping_list_code, user, "");
        } else {
            result = DataBase.add_new_item(Product, Type, Quantity, Price, shopping_list_code, user, Code_item);
        }
        //ID_Item = DataBase.read_item("SELECT " + DataBase.KEY_ID_ITEM + " FROM " + SQLiteDB.Items_table_name +" ORDER BY column DESC LIMIT 1");
        // Update timestamp
        update_timestamp_android(shopping_list_code);
        //add_new_relation(Integer.parseInt(ID_List),Integer.parseInt(ID_Item));
        return get_code_last_item();
    }

    /* Edita el nom, preu o quantitat de un item. Si el item te la sync flag activa i el ultim change type
     es new_name nomes canvia el nom,preu o quantitat */
    public boolean update_item_change (int update_value,String value,String code){
        String key;
        String change_type;
        switch (update_value){
            case 0:
                change_type = "new_name";
                key = DataBase.KEY_PRODUCT;
                break;
            case 1:
                change_type = "new_price";
                key = DataBase.KEY_PRICE;
                break;
            case 2:
                change_type = "new_quantity";
                key = DataBase.KEY_QUANTITY;
                break;
            default:
                Log.w(TAG,"Unknown update item. This should never happen");
                return false;
        }
        // If sync flag is active do not change type
        if (read_item(5,code).equals("1") && read_item(6,code).equals("new_item")) change_type = "new_item";
        // set values
        int result1 = DataBase.update_item(new String[]{key,value},code);
        // change type
        int result2 = DataBase.update_item(new String[]{DataBase.KEY_CHANGE_TYPE,change_type},code);
        return ((result1 > 0) && (result2 > 0));
    }

    // Canvia el codi de un item
    public boolean update_item_itemcode(String new_code,String code){
        Log.d(TAG,"Updating code");
        int result = DataBase.update_item(new String[]{DataBase.KEY_CODE, new_code},code);
        return result!=0;
    }
    // canvia la flag de sincronitzacio del item
    public boolean set_item_flag(String code, int flag){
        int result = DataBase.update_item(new String[]{DataBase.KEY_FLAG,"" + flag},code);
        return result!=0;
    }

    // Agafa el codi del ultim item que s'ha afegit
    // Quant afegeixes un item nou ja et retorna el codi
    public String get_code_last_item(){
        String query = "SELECT " + DataBase.KEY_CODE + " FROM " +DataBase.Items_table_name +
                " WHERE ID_Item=(SELECT MAX(ID_Item) FROM " + DataBase.Items_table_name +")";
        return DataBase.read_item(query);
    }
    // Esborra un item
    public void delete_item(String code){
        String ID_Item;
        //ID_Item = read_item(0,code);
        //delete_relation(Integer.parseInt(ID_Item));
        DataBase.delete_item(code);
    }
    //Llegeix un item
    public String read_item(int value,String code){
        String table_name = SQLiteDB.Items_table_name;
        String key,query,result;

        switch (value){
            case 0:
                key = DataBase.KEY_ID_ITEM;
                break;
            case 1:
                key = DataBase.KEY_PRODUCT;
                break;
            case 2:
                key = DataBase.KEY_TYPE;
                break;
            case 3:
                key = DataBase.KEY_QUANTITY;
                break;
            case 4:
                key = DataBase.KEY_PRICE;
                break;
            case 5:
                key = DataBase.KEY_FLAG;
                break;
            case 6:
                key = DataBase.KEY_CHANGE_TYPE;
                break;
            default:
                Log.v(TAG,"unknown value in read_item");
                return "Error";
        }
        query = "SELECT " + key + " FROM " + table_name + " WHERE " + DataBase.KEY_CODE + String.format("='%s'",code);
        result = DataBase.read_item(query);
        if (result == null) return "Error";
        Log.v(TAG,"read " + result);
        return result;
    }
    // Llegeix tots els items
    public List<String[]> read_all_items(String code){
        String table_name = SQLiteDB.Items_table_name;
        String query;
        List<String[]> r = new ArrayList<>();

        Cursor result;
        query = "SELECT * FROM " + table_name+" WHERE "+DataBase.KEY_CODE_LIST+String.format("='%s'",code);
        result = DataBase.read_multiple_entries(query);
        result.moveToFirst();
        try {
            do {
                Log.d(TAG,"code: " + result.getString(5));
                if (!result.getString(7).equals("delete_item")) {

                    /*
                    "ID_Item INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Product TEXT NOT NULL, " +
                "Type TEXT NOT NULL, " +
                "Quantity TEXT NOT NULL, " +
                "Price TEXT, " +
                "Code TEXT, " +
                "Flag INTEGER DEFAULT 0, " +
                "Change_type TEXT, "+
                "Code_List TEXT NOT NULL,"+
                "Last_User TEXT)";
                     */
                    // Product, Quantity, Price, Type, Last_User, Code_item
                    String[] entry = new String[]{result.getString(1), result.getString(3), result.getString(4),
                            result.getString(2), result.getString(9), result.getString(5)};
                    r.add(entry);
                }
            } while (result.moveToNext());
        } catch (android.database.CursorIndexOutOfBoundsException e){
            e.printStackTrace();
            Log.w(TAG,"Empty DB");
        }
        return r;
    }
    // Llegeix tots els items amb la flag de sync activa
    public List<String[]> read_all_with_flag_set_item(){
        String table_name = SQLiteDB.Items_table_name;
        String query;
        List<String[]> r = new ArrayList<>();

        Cursor result;
        query = "SELECT * FROM " + table_name + " WHERE " +DataBase.KEY_FLAG + " = 1";
        result = DataBase.read_multiple_entries(query);
        result.moveToFirst();
        try {
            do {
            /*
                "ID_Item INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Product TEXT NOT NULL, " +
                "Type TEXT NOT NULL, " +
                "Quantity TEXT NOT NULL, " +
                "Price TEXT, " +
                "Code TEXT, " +
                "Flag INTEGER DEFAULT 0, " +
                "Change_type TEXT, "+
                "Code_List TEXT NOT NULL,"+
                "Last_User TEXT)";
                     */
                // Product, Quantity, Price, Type, Last_User, Code_item, change type, code_list
                String[] entry = new String[]{result.getString(1), result.getString(3), result.getString(4),
                        result.getString(2), result.getString(9), result.getString(5), result.getString(7), result.getString(8)};
                r.add(entry);
            } while (result.moveToNext());
        } catch (android.database.CursorIndexOutOfBoundsException e) {
            e.printStackTrace();
            Log.w(TAG, "Empty DB");
        }
        Log.v(TAG,"read_all " + r.toString());
        return r;

    }
    /*
    public boolean add_new_relation(int ID_List,int ID_Item){
        return DataBase.new_list(ID_List,ID_Item) != -1;
    }

    public void delete_relation(int ID_Item){
        DataBase.delete_list_relation(ID_Item);
    }*/
}
