package lcukerd.com.stocknotifier;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private LinearLayout stock_linearLayout,value_linearLayout,check_linearLayout;
    private DbInteract interact;
    private Context context;
    private PendingIntent alarmIntent;
    private AlarmManager notifalm;
    private Boolean displaychecked;
    private createList list[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ComponentName receiver = new ComponentName(this, startonBoot.class);
        PackageManager pm = this.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

    }
    @Override
    protected void onStart()
    {
        super.onStart();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        interact = new DbInteract(this);
        context = this;

        notifalm = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        setalarm();

        stock_linearLayout = (LinearLayout) findViewById(R.id.stock_linear);
        value_linearLayout = (LinearLayout) findViewById(R.id.value_linear);
        check_linearLayout = (LinearLayout) findViewById(R.id.check_linear);
        createList lists = new createList();
        displaychecked=true;
        lists.execute(interact.readtable());

    }

    private void setalarm()
    {
        Intent callagain = new Intent(context,notifer.class);
        callagain.putExtra("id", interact.checked());
        alarmIntent = PendingIntent.getBroadcast(context, 0, callagain, PendingIntent.FLAG_CANCEL_CURRENT);
        notifalm.setExact(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+60000,alarmIntent);
    }
    private void cancelalarm()
    {
        notifalm.cancel(alarmIntent);
    }

    protected void onStop()
    {
        super.onStop();
        for (int i=0;(list[i]!=null)&&(i<interact.readtable().getCount());i++)
            list[i].cancel(true);
    }



    public class createList extends AsyncTask<Cursor,Void,String[][]> {

        private int noOfStocks=0;
        private HttpURLConnection urlconnection = null;
        private URL url;
        private String DATA[] ;
        private Cursor cursortemp;
        private Button stocks,value;

        protected String[][] doInBackground(Cursor[] cursors)
        {
            noOfStocks = cursors[0].getCount();
            String sortedData[][] = new String[noOfStocks][2];
            String close = new String() ;
            int count=0;
            if (cursors[0]!=null)
                cursortemp = cursors[0];
            DATA = new String[noOfStocks];
            while(cursors[0].moveToNext())
            {
                DATA[count] = "";
                String temp;
                String symbol = cursors[0].getString(cursors[0].getColumnIndex(eventDBcontract.ListofItem.columnsym));
                if ((cursors[0].getString(cursors[0].getColumnIndex(eventDBcontract.ListofItem.columnchecked)).equals("0"))&&(displaychecked==false))
                {
                    if (retrievefromnet(count,symbol)==0)
                        return null;

                    close = workonJson(count);
                    sortedData[count][0] = symbol;
                    sortedData[count++][1] = close;
                }
                else if ((cursors[0].getString(cursors[0].getColumnIndex(eventDBcontract.ListofItem.columnchecked)).equals("1"))&&(displaychecked==true))
                {
                    try
                    {
                        InputStream inputStream;
                        inputStream = context.openFileInput(symbol);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        while ((temp = reader.readLine()) != null) {
                            DATA[count] += temp;
                        }
                        Log.d("DATa Main",DATA[count]);
                    }
                    catch (FileNotFoundException e)
                    {
                        Log.e("createList","File not found "+symbol);
                        if (retrievefromnet(count,symbol)==0)
                            return null;
                    }
                    catch (IOException e)
                    {
                        Log.e("createList","IOException "+symbol);
                    }

                    close = workonJson(count);
                    sortedData[count][0] = symbol;
                    sortedData[count++][1] = close;
                }
            }
            noOfStocks = count;
            return sortedData;
        }

        protected void onPostExecute(final String[][] data)
        {
            CheckBox keepeye;

            for (int i=0;i<noOfStocks;i++)
            {
                try {
                    final String stock_name = data[i][0];
                    final String stock_cur = data[i][1];
                    if (displaychecked==true) {
                        showstockname(stock_name);
                    }
                    value = new Button(context);
                    value.setGravity(View.TEXT_DIRECTION_LTR);
                    value.setText("  " + stock_cur);
                    value.setTextSize(14);
                    value.setBackgroundColor(Color.rgb(224, 224, 224));
                    value.setTextColor(Color.rgb(0, 0, 0));
                    value_linearLayout.addView(value);

                    keepeye = new CheckBox(context);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0,0,0,48);
                    keepeye.setLayoutParams(params);
                    if (interact.ischecked(stock_name)==true)
                        keepeye.setChecked(true);
                    keepeye.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (((CheckBox)v).isChecked()==true)
                            {
                                cancelalarm();
                                interact.check(stock_name,"1");
                            }
                            else
                            {
                                cancelalarm();
                                interact.check(stock_name,"0");
                            }
                            setalarm();
                        }
                    });
                    check_linearLayout.addView(keepeye);

                final int j = i;
                    value.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, detailactivity.class);
                            intent.putExtra("symbol", stock_name);
                            intent.putExtra("data", DATA[j]);
                            startActivity(intent);
                        }
                    });
                }
                catch (Exception e)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Cannot connect to server!")
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            });
                    builder.create().show();
                }
            }
            if (displaychecked==true)
            {
                displaychecked=false;
                list = new createList[cursortemp.getCount()+1];
                int i=0;
                if (cursortemp.moveToFirst()) {
                    do {
                        if (cursortemp.getString(cursortemp.getColumnIndex(eventDBcontract.ListofItem.columnchecked)).equals("0")) {
                            showstockname(cursortemp.getString(cursortemp.getColumnIndex(eventDBcontract.ListofItem.columnsym)));
                            list[i] = new createList();
                            list[i++].execute(interact.returnSymCur(cursortemp.getString(cursortemp.getColumnIndex(eventDBcontract.ListofItem.columnsym))));
                        }
                    } while (cursortemp.moveToNext());
                }
            }
        }
        private int retrievefromnet(int count,String symbol)
        {
            String temp;
            String baseAddress = "http://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY";
            String apiKey = "J63P";
            Uri Url = Uri.parse(baseAddress)
                    .buildUpon()
                    .appendQueryParameter("symbol", symbol)
                    .appendQueryParameter("interval", "1min")
                    .appendQueryParameter("apikey", apiKey)
                    .build();
            Log.d("createList","built URL: " +Url.toString());
            try {
                url = new URL(Url.toString());
                urlconnection = (HttpURLConnection) url.openConnection();
                urlconnection.setRequestMethod("GET");
                urlconnection.connect();

                InputStream inputStream = urlconnection.getInputStream();
                if (inputStream == null) {
                    return 0;
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                while ((temp = reader.readLine()) != null) {
                    DATA[count] += temp;
                }
                Log.d("unedited", DATA[count]);

            } catch (IOException e) {
                Log.e("createList", "Error in url ", e);
                return 0;
            }
            return 1;
        }
        private String workonJson(int count)
        {
            try {
                String List = "Time Series (1min)";

                JSONObject jsonObject = new JSONObject(DATA[count]);
                jsonObject = jsonObject.getJSONObject(List);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis() - 34200000 );
                String currtime = sdf.format(calendar.getTime());
                Log.d("currtime",currtime);
                jsonObject = jsonObject.getJSONObject(currtime);

                return jsonObject.getString("4. close");

            } catch (JSONException e) {
                Log.e("createList", "Error in json");
                return null;
            }
        }
        private void showstockname(final String stock_name)
        {
            stocks = new Button(context);
            stocks.setGravity(View.TEXT_DIRECTION_LTR);
            stocks.setText("  " + stock_name);
            stocks.setTextSize(14);
            stocks.setBackgroundColor(Color.rgb(224, 224, 224));
            stocks.setTextColor(Color.rgb(0, 0, 0));
            stock_linearLayout.addView(stocks);

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
                    pw.showAtLocation(v, Gravity.NO_GRAVITY, coord[0] + 50, coord[1] + 50);
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

            /*stocks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, detailactivity.class);
                    intent.putExtra("symbol", stock_name);
                    intent.putExtra("data", DATA[j]);
                    startActivity(intent);
                }
            });*/
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
        if (id == R.id.action_add) {
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
