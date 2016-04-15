package sersilinc.needmorecookies;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/*
REFERENCE: http://techlovejump.com/android-multicolumn-listview/
 */

//Custom adapter for List Views
public class ListViewAdapters extends BaseAdapter{

    //Columns of the list view
    private static final String FIRST_COLUMN="First";
    private static final String SECOND_COLUMN="Second";
    private static final String THIRD_COLUMN="Third";
    private static final String FOURTH_COLUMN="Fourth";

    //List
    private ArrayList<HashMap<String, String>> list;

    //UI elements
    private Activity activity;
    private TextView txtFirst;
    private TextView txtSecond;
    private TextView txtThird;
    private TextView txtFourth;
    private TextView last_user;
    private String source;
    private String type;

    public ListViewAdapters(Activity activity,ArrayList<HashMap<String, String>> list, String source, String type){
        super();
        this.activity=activity;
        this.list=list;
        this.source=source;
        this.type=type;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater=activity.getLayoutInflater();

        if(convertView == null){

            //Check if the List View is the header or the content. If True, list view is the content
            if (source.equals("Content")) {
                convertView = inflater.inflate(R.layout.row_view, null);
            } else if (source.equals("Header")){
                convertView = inflater.inflate(R.layout.row_header, null);
            } else if (source.equals("MainActivity")){
                convertView = inflater.inflate(R.layout.row_main, null);
            }

            if (!source.equals("MainActivity")) {
                //Get UI elements
                txtFirst = (TextView) convertView.findViewById(R.id.product);
                txtSecond = (TextView) convertView.findViewById(R.id.quantity);
                txtThird = (TextView) convertView.findViewById(R.id.price);

                if (type.equals("0")) {
                    txtFourth = (TextView) convertView.findViewById(R.id.last_user_entry);
                    last_user = (TextView) convertView.findViewById(R.id.last_user);
                    txtFourth.setVisibility(View.VISIBLE);
                    last_user.setVisibility(View.VISIBLE);
                    //Write to UI elements
                    HashMap<String, String> map = list.get(position);
                    txtFirst.setText(map.get(FIRST_COLUMN));
                    txtSecond.setText(map.get(SECOND_COLUMN));
                    txtThird.setText(map.get(THIRD_COLUMN));
                    txtFourth.setText(map.get(FOURTH_COLUMN));
                } else {
                    //Write to UI elements
                    HashMap<String, String> map = list.get(position);
                    txtFirst.setText(map.get(FIRST_COLUMN));
                    txtSecond.setText(map.get(SECOND_COLUMN));
                    txtThird.setText(map.get(THIRD_COLUMN));

                }
            }
            else {
                txtFirst = (TextView) convertView.findViewById(R.id.shop_list);
                txtSecond = (TextView) convertView.findViewById(R.id.last_added);
                //Write to UI elements
                HashMap<String, String> map = list.get(position);
                txtFirst.setText(map.get(FIRST_COLUMN));
                txtSecond.setText(map.get(SECOND_COLUMN));
            }

        }

        return convertView;
    }
}