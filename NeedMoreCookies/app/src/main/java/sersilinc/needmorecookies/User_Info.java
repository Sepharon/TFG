package sersilinc.needmorecookies;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by sergi on 19/02/16.
 */

public class User_Info {
    private final String TAG = "User Info: ";
    // Global variables for class
    // Random value variable
    Random ran;
    // Shows name, email and other info
    // Identifiers
    private int ID;
    // Name,email
    private String name;
    private String email;
    // Name of the list
    //List of list [[NAME,TYPE,HASH],[NAME,TYPE,HASH]..]
    List<List<String>> public_lists = new ArrayList<>();
    List<List<String>> private_lists = new ArrayList<>();
    // GoogleAPICLient
    //private GoogleApiClient mAPIClient;

    // Instantiate
    private static User_Info user_info = new User_Info();

    public static User_Info getInstance(){
        return user_info;
    }

    // Dummy class init
    public User_Info(){}
    // Actual class init
    public User_Info(int ID,String name,String email,GoogleApiClient mAPIClient){
        super();
        // Set variable with values
        this.ID = ID;
        this.name = name;
        this.email = email;
        //this.mAPIClient = mAPIClient;
        Log.v(TAG,"User Info added for " + name);
    }

    // Get values
    public int getID(){
        return ID;
    }
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
    /*public GoogleApiClient getmAPIClient() {
        return mAPIClient;
    }*/

    // Set values
    public void setID(int ID) {
        this.ID = ID;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPrivate_lists(List<String> private_list) {
        private_list.add(set_hash(private_list.get(0)));
        this.public_lists.add(private_list);
    }
    // Values from list [Name,Type]
    public void setPublic_lists(List<String> public_list) {
        public_list.add(set_hash(public_list.get(0)));
        this.public_lists.add(public_list);

    }
    /*public void setmAPIClient(GoogleApiClient mAPIClient) {
        this.mAPIClient = mAPIClient;
    }*/



    // Create Hash functions
    private String set_hash(String list_na){
        int n;
        String hash = "";
        StringBuilder stringBuilder = new StringBuilder();
        ran = new Random();
        // Random number between 0 and 8
        n = ran.nextInt(4);
        for (int i = 0; i < list_na.length(); i++){
            stringBuilder.append(list_na.charAt(i)<<n);
        }
        // Create SHA1 Hash
        try {
            hash = SHA1(stringBuilder.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return hash;
    }

    private static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte [] bytes = text.getBytes("UTF-8");
        md.update(bytes, 0, bytes.length);
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }
    // Convert to hex
    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public String toFormat(){
        return "User: " + getName() + " " + getEmail();
    }
}
