package sersilinc.needmorecookies;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

// Shows name, email and other info

public class User_Info {

    //Tag for Logs
    private final String TAG = "User Info: ";

    // Global variables for class
    // Name,email
    private String name;
    private String email;

    //Array that contains the name of the list, type and code
    //List of list [[NAME,TYPE,CODE,Timestamp],[NAME,TYPE,CODE,Timestamp]..]
    List<List<String>> public_lists = new ArrayList<>();
    List<List<String>> private_lists = new ArrayList<>();

    //Array that contains the name of the product, quantity, price and code
    //List of list [[NAME,QUANTITY,PRICE,TYPE,CODE,USER],[NAME,QUANTITY,PRICE,TYPE,CODE,USER]..]
    List<List<String>> item_lists = new ArrayList<>();

    //Instantiate
    private static User_Info user_info = new User_Info();

    public static User_Info getInstance(){
        return user_info;
    }

    // Dummy class init
    public User_Info(){}
    // Actual class init
    public User_Info(String name,String email){
        super();
        // Set variable with values
        this.name = name;
        this.email = email;
        //Log.v(TAG,"User Info added for " + name);
    }

    // Get values
    public String getName(){
        return name;
    }
    public String getEmail() {
        return email;
    }
    public List<List<String>> getPrivate_lists() {
        return private_lists;
    }
    public List<List<String>> getPublic_lists() {
        return public_lists;
    }
    public List<List<String>> getItems_lists() {
        return item_lists;
    }


    // Set values
    public void setName(String name) {
        this.name = name;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    // Values from list [Name,Type,Code,Timestamp]
    public void setPrivate_lists(List<String> private_list) {
        if (!private_lists.contains(private_list)){
            this.private_lists.add(private_list);
        }
    }
    // Values from list [Name,Type,Code,Timestamp]
    public void setPublic_lists(List<String> public_list) {
        if (!public_lists.contains(public_list)) {
            this.public_lists.add(public_list);
        }

    }

    // Values from list [Name,Quantity,Price,Type,Code,User]
    public void setItems_lists(List<String> item_list) {
        if (!item_lists.contains(item_list)) {
            this.item_lists.add(item_list);
        }

    }

    //Retrieve the name and the email of the user
    public String toFormat(){
        return "User: " + getName() + " " + getEmail();
    }
}
