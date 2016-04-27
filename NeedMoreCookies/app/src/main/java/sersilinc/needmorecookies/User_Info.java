package sersilinc.needmorecookies;

import java.util.ArrayList;
import java.util.List;

// Shows name, email and other info
public class User_Info {

    // Global variables for class
    // Name,email
    private String name;
    private String email;

    private boolean offline_mode;

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
    }

    // Get values
    public String getName(){
        return name;
    }
    public String getEmail() {
        return email;
    }
    public Boolean getOffline_mode() { return offline_mode;}
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
    public void setOffline_mode(Boolean status) {this.offline_mode = status;}

    // Values from list [Name,Quantity,Price,Type,Code,User]
    public void setItems_lists(List<String> item_list) {
        if (!item_lists.contains(item_list)) {
            this.item_lists.add(item_list);
        }
    }
}
