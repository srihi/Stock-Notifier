package lcukerd.com.stocknotifier;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.R.attr.mimeType;

/**
 * Created by Programmer on 29-04-2017.
 */

public class notifer extends WakefulBroadcastReceiver{


    private Context context;

    @Override
    public void onReceive(final Context contextn, final Intent intent)
    {
        Log.d("Notifier","started");
        String id[] = intent.getStringArrayExtra("id");

        if (id!=null) {
            context = contextn;
            backgroundsync back = new backgroundsync();
            back.execute(id);

            Calendar c =Calendar.getInstance();

            Log.d("Calender",String.valueOf(c.get(Calendar.HOUR_OF_DAY)));
            Intent callagain = new Intent(contextn, notifer.class);
            callagain.putExtra("id", id);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(contextn, 0, callagain, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager notifalm;
            notifalm = (AlarmManager) contextn.getSystemService(Context.ALARM_SERVICE);
            if (c.get(Calendar.HOUR_OF_DAY)<16)
                notifalm.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000, alarmIntent);
            else
            {
                c.add(Calendar.DAY_OF_MONTH,1);
                c.set(Calendar.HOUR_OF_DAY,9);
                c.set(Calendar.MINUTE,0);
                Log.d("Calender",String.valueOf(c.getTimeInMillis()));
                notifalm.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), alarmIntent);
            }
        }
    }

    class backgroundsync extends AsyncTask<String[],Void,Void>
    {
        @Override
        protected Void doInBackground(String[][] id)
        {
            String baseAddress = "http://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY";
            String apiKey = "J63P";
            URL url;
            DbInteract interact = new DbInteract(context);
            HttpURLConnection urlconnection = null;
            FileOutputStream outputStream;


            for (int i=0;i<id[0].length;i++) {
                String temp, DATA = "";
                String symbol = interact.readsym(id[0][i]);

                Uri Url = Uri.parse(baseAddress)
                        .buildUpon()
                        .appendQueryParameter("symbol", symbol)
                        .appendQueryParameter("interval", "1min")
                        .appendQueryParameter("apikey", apiKey)
                        .build();
                Log.d("Notifier", Url.toString());

                try {
                    url = new URL(Url.toString());
                    urlconnection = (HttpURLConnection) url.openConnection();
                    urlconnection.setRequestMethod("GET");
                    urlconnection.connect();

                    InputStream inputStream = urlconnection.getInputStream();
                    if (inputStream == null) {
                        Log.e("inputstream", "empty");
                        continue;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    while ((temp = reader.readLine()) != null) {
                        DATA += temp;
                    }
                    Log.d("unedited", DATA);
                    outputStream = context.openFileOutput(symbol, context.MODE_PRIVATE);
                    outputStream.write(DATA.getBytes());
                    outputStream.close();
                }
                catch (IOException e) {
                    Log.e("Notifier", "Error in url ", e);
                    continue;
                }

                JSONObject jsonObject = null;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
                String currtime;
                try {
                    String List = "Time Series (1min)";
                    jsonObject = new JSONObject(DATA);
                    jsonObject = jsonObject.getJSONObject(List);
                }
                catch (JSONException e) {
                    Log.e("Notifier", "Error in json");
                }

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis() - 34200000);
                currtime = sdf.format(calendar.getTime());
                Float closeva;

                try {
                    JSONObject tempobj = jsonObject.getJSONObject(currtime);                   //change here
                    closeva = Float.parseFloat(tempobj.getString("4. close"));
                    if (interact.readreqd(symbol).equals("")==false)
                    {
                        float reqdcloseva = Float.parseFloat(interact.readreqd(symbol));
                        if (reqdcloseva < 0)
                        {
                            if (closeva <= (0 - reqdcloseva))
                                showNotification(context, closeva, symbol, "low", DATA);
                        } else
                        {
                            if (closeva >= reqdcloseva)
                                showNotification(context, closeva, symbol, "high", DATA);
                        }
                    }
                }
                catch(JSONException e)
                {
                    Log.e("Notifier", "Error in json");
                    calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis() - 34200000 - 60000);
                    currtime = sdf.format(calendar.getTime());
                    try {
                        JSONObject tempobj = jsonObject.getJSONObject(currtime);                   //change here
                        closeva = Float.parseFloat(tempobj.getString("4. close"));
                        float reqdcloseva = Float.parseFloat(interact.readreqd(symbol));
                        if (reqdcloseva < 0)
                        {
                            if (closeva <= (0-reqdcloseva))
                                showNotification(context, closeva, symbol,"low",DATA);
                        }
                        else
                        {
                            if (closeva >= reqdcloseva)
                                showNotification(context, closeva, symbol,"high",DATA);
                        }
                    }
                    catch (JSONException ef)
                    {
                        Log.e("Notifier", "Error in json final");
                    }
                }
                catch (NullPointerException e)
                {
                    Log.e("Notifier","no reqd symbol");
                }

            }
            return null;
        }
        void showNotification(Context context,Float closeva,String symbol,String state,String DATA)
        {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_trending_up_black_24dp)
                            .setContentTitle(symbol + " price is " + state + " : " + String.valueOf(closeva));
            mBuilder.setAutoCancel(true);
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mBuilder.setSound(alarmSound);
            Intent resultIntent = new Intent(context, detailactivity.class);
            resultIntent.putExtra("symbol",symbol);
            resultIntent.putExtra("data",DATA);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(detailactivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
            mNotificationManager.notify(m, mBuilder.build());
        }
    }

    /*File file = new File(Environment.getExternalStorageDirectory(), symbol);
    FileWriter out = new FileWriter(file);
                        out.write(DATA);
                        out.close();

                        Log.d("Notifier","File write successful");*/
}
