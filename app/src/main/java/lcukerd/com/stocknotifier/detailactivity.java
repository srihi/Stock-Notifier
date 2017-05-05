package lcukerd.com.stocknotifier;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class detailactivity extends AppCompatActivity {

    private DbInteract interact;
    private TextView time,close;
    private String sym;
    private String data;
    private JSONObject jsonObject;
    private String price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent caller = getIntent();
        sym = caller.getStringExtra("symbol");
        data = caller.getStringExtra("data");
        setContentView(R.layout.activity_detailactivity);
        getSupportActionBar().setTitle(sym);
        interact = new DbInteract(this);
        time = (TextView) findViewById(R.id.time);
        close = (TextView) findViewById(R.id.close);
    }
    @Override
    protected void onStart()
    {
        super.onStart();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00"),showformat = new SimpleDateFormat("hh:mm");
        String currtime,showtime;
        try {
            String List = "Time Series (1min)";
            jsonObject = new JSONObject(data);
            jsonObject = jsonObject.getJSONObject(List);
        } catch (JSONException e) {
            Log.e("createList", "Error in json", e);
        }
        for (int i = 0; i < 10; i++) {

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis() - 34200000  - i * 60000);
            currtime = sdf.format(calendar.getTime());
            calendar.setTimeInMillis(System.currentTimeMillis() - i * 60000);
            showtime = showformat.format(calendar.getTime());
            Log.d("Current Time", currtime + String.valueOf(i));
            String closeva;

            try {
                JSONObject tempobj = jsonObject.getJSONObject(currtime);                   //change here
                closeva = tempobj.getString("4. close");
                if (i==1)
                {
                    price=closeva;
                }
            }
            catch(JSONException e)
            {
                Log.e("createList", "Error in json", e);
                break;
            }

            time.append("\n"+showtime);
            close.append("\n"+closeva);
        }

        //createTable updateTable = new createTable();
        //updateTable.execute(sym);
    }

    public class createTable extends AsyncTask<String,Void,String> {

        private HttpURLConnection urlconnection = null;
        private URL url;

        protected String doInBackground(String[] symbol) {
            String baseAddress = "http://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY";
            String apiKey = "J63P", temp, DATA = "";
            Uri Url = Uri.parse(baseAddress)
                    .buildUpon()
                    .appendQueryParameter("symbol", symbol[0])
                    .appendQueryParameter("interval", "1min")
                    .appendQueryParameter("apikey", apiKey)
                    .build();
            Log.d("built URL", Url.toString());
            try {
                url = new URL(Url.toString());
                urlconnection = (HttpURLConnection) url.openConnection();
                urlconnection.setRequestMethod("GET");
                urlconnection.connect();

                InputStream inputStream = urlconnection.getInputStream();
                if (inputStream == null) {
                    Log.d("inputstream", "empty");
                    return null;
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                while ((temp = reader.readLine()) != null) {
                    DATA += temp;
                }
                Log.d("unedited", DATA);
            } catch (IOException e) {
                Log.e("createList", "Error in url ", e);
                return null;
            }

            return DATA;
        }

        protected void onPostExecute(String data) {
            JSONObject jsonObject;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:00"),showformat = new SimpleDateFormat("hh:mm");
            String currtime,showtime;
            try {
                String List = "Time Series (1min)";
                jsonObject = new JSONObject(data);
                jsonObject = jsonObject.getJSONObject(List);
            } catch (JSONException e) {
                Log.e("createList", "Error in json", e);
                return;
            }
            for (int i = 1; i < 11; i++) {

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis() + 9000000 - i * 60000);
                currtime = sdf.format(calendar.getTime());
                calendar.setTimeInMillis(System.currentTimeMillis() - i * 60000);
                showtime = showformat.format(calendar.getTime());
                Log.d("Current Time", currtime + String.valueOf(i));
                String closeva;

                try {
                    JSONObject tempobj = jsonObject.getJSONObject(currtime);                   //change here
                    closeva = tempobj.getString("4. close");
                }
                catch(JSONException e)
                {
                    Log.e("createList", "Error in json", e);
                    return;
                }

                time.append("\n"+showtime);
                close.append("\n"+closeva);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_addreqd) {
            {
                showdialog();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    void showdialog()
    {
        AlertDialog.Builder eventName = new AlertDialog.Builder(this,R.style.dialogStyle);
        LayoutInflater inflater = (LayoutInflater)this.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        View dialogb = inflater.inflate(R.layout.dialog_add_reqdva, null);
        eventName.setView(dialogb);
        final AlertDialog dialog = eventName.create();
        dialog.show();

        Button okay =(Button) dialog.findViewById(R.id.dokay),cancel = (Button) dialog.findViewById(R.id.dcancel);
        final EditText reqdvalue = (EditText) dialog.findViewById(R.id.dreqdvalue);

        TextView left = (TextView) dialog.findViewById(R.id.dleft) ,right = (TextView) dialog.findViewById(R.id.dright);
        left.setText(">=");
        right.setText("<=");

        TextView symbolw = (TextView) dialog.findViewById(R.id.dsymbol);
        symbolw.setText(sym);

        TextView pricew = (TextView) dialog.findViewById(R.id.dprice);
        if (price!=null)
           pricew.setText(price);
        else
            pricew.setText("null");


        final SwitchCompat chooser = (SwitchCompat) dialog.findViewById(R.id.dswitch);


        String oldvalue = interact.readreqd(sym);
        if ((oldvalue!=null)&&(oldvalue.equals("")==false))
        {
            float oldvaluef = Float.parseFloat(oldvalue);
            if (oldvaluef<0)
            {
                chooser.setChecked(true);
                oldvalue = String.valueOf(0-oldvaluef);
            }
            else
            {
                chooser.setChecked(false);
            }
            reqdvalue.setText(oldvalue);
        }

        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reqd = reqdvalue.getText().toString();
                if (chooser.isChecked())
                    reqd = "-"+reqd;
                if (reqd.equals("")==false)
                    interact.addreqd(sym,reqd);
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

}
