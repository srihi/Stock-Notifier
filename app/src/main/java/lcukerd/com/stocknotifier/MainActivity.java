package lcukerd.com.stocknotifier;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import org.json.JSONArray;
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

public class MainActivity extends AppCompatActivity {

    private LinearLayout stock_linearLayout,value_linearLayout;
    private DbInteract interact;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    protected void onStart()
    {
        super.onStart();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent callagain = new Intent(this,notifer.class);
        callagain.putExtra("id",new String[]{"2","3"});
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, callagain, 0);
        AlarmManager notifalm;
        notifalm = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        notifalm.setExact(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),alarmIntent);

        interact = new DbInteract(this);
        context = this;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder eventName = new AlertDialog.Builder(context,R.style.dialogStyle);
                LayoutInflater inflater = (LayoutInflater)context.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
                View dialogb = inflater.inflate(R.layout.dialog_add_name, null);
                eventName.setView(dialogb);
                final AlertDialog dialog = eventName.create();
                dialog.show();
                final InputMethodManager imm = (InputMethodManager)   getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                Button okay =(Button) dialog.findViewById(R.id.okay),cancel = (Button) dialog.findViewById(R.id.cancel);
                final EditText nameOfEvent = (EditText) dialog.findViewById(R.id.eventName);

                okay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newEntry = nameOfEvent.getText().toString();
                        nameOfEvent.clearFocus();
                        imm.hideSoftInputFromWindow(nameOfEvent.getWindowToken(), 0);
                        dialog.dismiss();
                        interact.addSymbol(newEntry);
                        recreate();
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nameOfEvent.clearFocus();
                        imm.hideSoftInputFromWindow(nameOfEvent.getWindowToken(), 0);
                        dialog.dismiss();
                    }
                });

            }
        });
        stock_linearLayout = (LinearLayout) findViewById(R.id.stock_linear);
        value_linearLayout = (LinearLayout) findViewById(R.id.value_linear);
        createList list = new createList();
        list.execute(interact.readtable());

    }



    public class createList extends AsyncTask<Cursor,Void,String[][]> {

        private int noOfStocks=0;
        private HttpURLConnection urlconnection = null;
        private URL url;

        protected String[][] doInBackground(Cursor[] cursors)
        {
            noOfStocks = cursors[0].getCount();
            String sortedData[][] = new String[noOfStocks][2];
            String close = new String() ;
            int count=0;
            while(cursors[0].moveToNext())
            {
                Log.d("Table ",cursors[0].getString(cursors[0].getColumnIndex(eventDBcontract.ListofItem.columnID)));
                String DATA="",temp;
                String symbol = cursors[0].getString(cursors[0].getColumnIndex(eventDBcontract.ListofItem.columnsym));
                String baseAddress="http://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY";
                String apiKey="J63P";
                Uri Url = Uri.parse(baseAddress)
                        .buildUpon()
                        .appendQueryParameter("symbol",symbol)
                        .appendQueryParameter("interval","1min")
                        .appendQueryParameter("apikey",apiKey)
                        .build();
                Log.d("built URL",Url.toString());
                try
                {
                    url= new URL(Url.toString());
                    urlconnection= (HttpURLConnection) url.openConnection();
                    urlconnection.setRequestMethod("GET");
                    urlconnection.connect();

                    InputStream inputStream = urlconnection.getInputStream();
                    if (inputStream==null)
                    {
                        Log.d("inputstream","empty");
                        return null;
                    }
                    BufferedReader reader= new BufferedReader(new InputStreamReader(inputStream));

                    while((temp = reader.readLine())!=null)
                    {
                        DATA += temp;
                    }

                    Log.d("unedited",DATA);


                }
                catch(IOException e)
                {
                    Log.e("createList", "Error in url ", e);
                    return null;
                }

                try {
                    String List = "Time Series (1min)";

                    JSONObject jsonObject = new JSONObject(DATA);
                    jsonObject = jsonObject.getJSONObject(List);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:00");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis()+9000000);
                    String currtime = sdf.format(calendar.getTime());
                    Log.d("Current Time",currtime);
                    jsonObject = jsonObject.getJSONObject(currtime);                   //change here

                    close = jsonObject.getString("4. close");

                    Log.d("Extracted ", symbol  + " " + close +"\n");

                }
                catch (JSONException e)
                {
                    Log.e("createList","Error in json",e);
                    close = "null";
                }
                sortedData[count][0]=symbol;
                sortedData[count++][1]=close;

            }
            return sortedData;
        }

        protected void onPostExecute(String[][] data)
        {
            Button stocks,value;

            for (int i=0;i<noOfStocks;i++)
            {
                final String stock_name = data[i][0];
                String stock_cur = data[i][1];
                stocks = new Button(context);
                stocks.setGravity(View.TEXT_DIRECTION_LTR);
                stocks.setText("  " + stock_name);
                stocks.setTextSize(14);
                stocks.setBackgroundColor(Color.rgb(224, 224, 224));
                stocks.setTextColor(Color.rgb(0,0,0));
                stock_linearLayout.addView(stocks);

                value = new Button(context);
                value.setGravity(View.TEXT_DIRECTION_LTR);
                value.setText("  " + stock_cur);
                value.setTextSize(14);
                value.setBackgroundColor(Color.rgb(224, 224, 224));
                value.setTextColor(Color.rgb(0,0,0));
                value_linearLayout.addView(value);

                stocks.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                        View layout = inflater.inflate(R.layout.popup, (ViewGroup) findViewById(R.id.pop));
                        PopupWindow pw = new PopupWindow(layout, 400, 200, true);
                        int coord[] = new int[2];
                        v.getLocationOnScreen(coord);
                        pw.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), android.R.color.transparent)));
                        pw.setOutsideTouchable(true);
                        pw.showAtLocation(v, Gravity.NO_GRAVITY, coord[0] + 50, coord[1]+50);
                        Button del = (Button) layout.findViewById(R.id.del);
                        del.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                interact.deleteSym(stock_name);
                                recreate();
                            }
                        });

                        return true;
                    }
                });

                stocks.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context,detailactivity.class);
                        intent.putExtra("symbol",stock_name);
                        startActivity(intent);
                    }
                });

                value.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context,detailactivity.class);
                        intent.putExtra("symbol",stock_name);
                        startActivity(intent);
                    }
                });
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
