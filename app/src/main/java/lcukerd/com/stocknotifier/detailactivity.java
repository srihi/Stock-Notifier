package lcukerd.com.stocknotifier;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
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

    private EditText reqdclose;
    private DbInteract interact;
    private TextView time,close;
    private String sym;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent caller = getIntent();
        sym = caller.getStringExtra("symbol");
        setContentView(R.layout.activity_detailactivity);
        interact = new DbInteract(this);
        time = (TextView) findViewById(R.id.time);
        close = (TextView) findViewById(R.id.close);
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        reqdclose = (EditText) findViewById(R.id.reqd);
        String oldvalue = interact.readreqd(sym);
        if ((oldvalue!=null)&&(oldvalue.equals("")==false))
            reqdclose.setText(oldvalue);
        createTable updateTable = new createTable();
        updateTable.execute(sym);
    }
    protected void onStop()
    {
        super.onStop();
        String reqd = reqdclose.getText().toString();
        if (reqd.equals("")==false)
            interact.addreqd(sym,reqd);
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
                Log.d("Current Time", currtime);
                String closeva;

                try {
                    jsonObject = jsonObject.getJSONObject(currtime);                   //change here
                    closeva = jsonObject.getString("4. close");
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
}
