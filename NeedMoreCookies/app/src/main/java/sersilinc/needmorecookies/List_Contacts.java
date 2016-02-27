package sersilinc.needmorecookies;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.renderscript.ScriptGroup;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

// TODO : need to talk about this. How do we get friends? gmail? phone number? name? or put a password?

public class List_Contacts extends AppCompatActivity {
    List<String> name1 = new ArrayList<String>();
    List<String> phno1 = new ArrayList<String>();
    List<String> photos = new ArrayList<>();
    MyAdapter ma ;
    Button select;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_list__contacts);

        getAllContacts(this.getContentResolver());
        ListView lv= (ListView) findViewById(R.id.lv);
        ma = new MyAdapter();
        lv.setAdapter(ma);
        //lv.setOnItemClickListener(this);
        lv.setItemsCanFocus(false);
        lv.setTextFilterEnabled(true);
        // adding
        select = (Button) findViewById(R.id.button1);
        select.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v) {
                StringBuilder checkedcontacts= new StringBuilder();
                System.out.println(".............."+ma.mCheckStates.size());
                for(int i = 0; i < name1.size(); i++) {
                    if(ma.mCheckStates.get(i)==true) {
                        checkedcontacts.append(name1.get(i).toString());
                        checkedcontacts.append("\n");
                    }
                    else {
                        System.out.println("Not Checked......"+name1.get(i).toString());
                    }
                }
                Toast.makeText(List_Contacts.this, checkedcontacts, Toast.LENGTH_LONG).show();
            }
        });


    }
    /*@Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub
        ma.toggle(arg2);
    }*/

    public  void getAllContacts(ContentResolver cr) {

        Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext())
        {
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            //int id = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
            String photo_uri =phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
            System.out.println(".................." + phoneNumber);
            Log.v("asdas","" + photo_uri);
            name1.add(name);
            phno1.add(phoneNumber);
            photos.add(photo_uri);

        }

        phones.close();
    }
    class MyAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener
    {  private SparseBooleanArray mCheckStates;
        LayoutInflater mInflater;
        TextView tv1,tv;
        CheckBox cb;
        MyAdapter()
        {
            mCheckStates = new SparseBooleanArray(name1.size());
            mInflater = (LayoutInflater)List_Contacts.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return name1.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub

            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO: add images
            View vi=convertView;
            if(convertView==null)
                vi = mInflater.inflate(R.layout.row, null);
            ImageView imgViewer = (ImageView) findViewById(R.id.char_image);
            TextView tv= (TextView) vi.findViewById(R.id.textView1);
            tv1= (TextView) vi.findViewById(R.id.textView2);
            cb = (CheckBox) vi.findViewById(R.id.checkBox1);
            if (photos.get(position) != null) {
                Uri u = Uri.parse(photos.get(position));
                if (u != null) {
                    imgViewer.setImageURI(u);
                }
            }

            tv.setText("Name: "+ name1.get(position));
            tv1.setText("Phone No: "+ phno1.get(position));
            cb.setTag(position);
            cb.setChecked(mCheckStates.get(position, false));
            cb.setOnCheckedChangeListener(this);

            return vi;
        }
        public boolean isChecked(int position) {
            return mCheckStates.get(position, false);
        }

        public void setChecked(int position, boolean isChecked) {
            mCheckStates.put(position, isChecked);
            System.out.println("hello...........");
            notifyDataSetChanged();
        }

        public void toggle(int position) {
            setChecked(position, !isChecked(position));
        }
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            // TODO Auto-generated method stub

            mCheckStates.put((Integer) buttonView.getTag(), isChecked);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }
}