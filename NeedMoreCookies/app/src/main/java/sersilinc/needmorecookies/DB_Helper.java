package sersilinc.needmorecookies;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
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

    public boolean add_new_list(String new_list,int type){
        Log.v(TAG,"added new list with name: "+new_list);
        long result = DataBase.add_new_list(new_list,type);
        return result != -1;
    }

    public boolean update_list_name(String new_name,String code){
        int result;
        String flag_status;
        flag_status = read_shopping_list(2,code);
        // List still need to synchronize
        if (flag_status.equals("0")){
            Log.v(TAG,"Flag is 0");
            DataBase.update_list(new String[]{DataBase.KEY_CHANGE_TYPE, "change_list_name"},code);
            DataBase.update_list(new String[]{DataBase.KEY_FLAG,"1"},code);
        }
        result = DataBase.update_list(new String[]{DataBase.KEY_LIST_NAME, new_name},code);
        Log.v(TAG,"Result of update is " + result);
        return result != 0;
    }

    public boolean update_timestamp(String code){
        int result = DataBase.update_list(null,code);
        return result!=0;
    }

    public boolean set_list_flag(String code, int flag){
        int result = DataBase.update_list(new String[]{DataBase.KEY_FLAG,"" + flag},code);
        return result!=0;
    }

    public void delete_list(String code){
        DataBase.delete_list(code);
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
            default:
                return null;
        }

        query = "SELECT " + key + "FROM " + table_name + "WHERE " + DataBase.KEY_CODE + "=" + code;
        result = DataBase.read_shopping_lists(query);
        if (result == null) return "Error";
        Log.v(TAG,"read " + result);
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
        do {
            // list name,timestamp , code,Public
            String [] entry = new String[]{result.getString(1),result.getString(2),result.getString(3),
                    result.getString(4)};
            r.add(entry);
        } while (result.moveToNext());

        return r;
    }
    public List<String[]> read_all_with_flag_set_list(){
        String table_name = SQLiteDB.Shopping_list_table_name;
        String query;
        List<String[]> r = new ArrayList<>();

        Cursor result;
        query = "SELECT * FROM " + table_name + "WHERE " +DataBase.KEY_FLAG + " = 1";
        result = DataBase.read_multiple_entries(query);
        result.moveToFirst();
        do {
            // list name, code,Public, change type
            String [] entry = new String[]{result.getString(1),result.getString(3),result.getString(4),
                    result.getString(6)};
            r.add(entry);
        } while (result.moveToNext());
        Log.v(TAG,"read_all_flag " + r.toString());
        return r;
    }

    public boolean add_new_item(String Product, String Type, String Quantity, String Price,String shopping_list_code){
        String ID_List,ID_Item;
        // AFEGINT A LIST, LLEGINT ID_ITEM
        ID_List = read_shopping_list(0,shopping_list_code);
        if (ID_List.equals("Error")) return false;
        // Add new item
        long result = DataBase.add_new_item(Product,Type,Quantity,Price);
        ID_Item = DataBase.read_item("SELECT " + DataBase.KEY_ID_ITEM + "FROM " + SQLiteDB.Items_table_name +"ORDER BY column DESC LIMIT 1");
        // Update timestamp
        update_timestamp(shopping_list_code);
        add_new_relation(Integer.parseInt(ID_List),Integer.parseInt(ID_Item));
        return result!=-1;
    }
    public boolean update_item (int update_value,String key,String value,String code){

        String change_type;
        switch (update_value){
            case 0:
                change_type = "new_name";
                break;
            case 1:
                change_type = "new_price";
                break;
            case 2:
                change_type = "new_quantity";
                break;
            case 3:
                change_type = "new_price";
                break;
            default:
                Log.w(TAG,"Unknown update item. This should never happen");
                return false;
        }
        // If sync flag is active do not change type
        if (read_item(5,code).equals("1")) change_type = "new_item";
        int result = DataBase.update_item(change_type,key,value,code);
        DataBase.update_item(change_type, DataBase.KEY_FLAG,"1",code);
        return result !=0;
    }

    public void delete_item(String code){
        String ID_Item;
        ID_Item = read_item(0,code);
        delete_relation(Integer.parseInt(ID_Item));
        DataBase.delete_item(code);
    }

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
        query = "SELECT " + key + "FROM " + table_name + "WHERE " + DataBase.KEY_CODE + " = " +code;
        result = DataBase.read_item(query);
        if (result == null) return "Error";
        Log.v(TAG,"read " + result);
        return result;
    }

    public List<String[]> read_all_with_flag_set_item(){
        String table_name = SQLiteDB.Items_table_name;
        String query;
        List<String[]> r = new ArrayList<>();

        Cursor result;
        query = "SELECT * FROM " + table_name + "WHERE " +DataBase.KEY_FLAG + " = 1";
        result = DataBase.read_multiple_entries(query);
        result.moveToFirst();
        do {
            // Product, type, quantity, price, code, change type
            String [] entry = new String[]{result.getString(1),result.getString(2),result.getString(3),
                    result.getString(4),result.getString(5),result.getString(7)};
            r.add(entry);
        } while (result.moveToNext());
        Log.v(TAG,"read_all " + r.toString());
        return r;
    }

    public boolean add_new_relation(int ID_List,int ID_Item){
        return DataBase.new_list(ID_List,ID_Item) != -1;
    }

    public void delete_relation(int ID_Item){
        DataBase.delete_list_relation(ID_Item);
    }
}
