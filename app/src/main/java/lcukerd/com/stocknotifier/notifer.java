package lcukerd.com.stocknotifier;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

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

/**
 * Created by Programmer on 29-04-2017.
 */

public class notifer extends WakefulBroadcastReceiver{


    private Context context;

    @Override
    public void onReceive(Context contextn,Intent intent)
    {
        Log.d("Notifier","started");
        String id[] = intent.getStringArrayExtra("id");

        if (id.length!=0) {
            context = contextn;
            backgroundsync back = new backgroundsync();
            back.execute(id);

            Intent callagain = new Intent(context, notifer.class);
            callagain.putExtra("id", id);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, callagain, 0);
            AlarmManager notifalm;
            notifalm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            notifalm.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000, alarmIntent);
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

            for (int i=0;i<id[0].length;i++) {
                String temp, DATA = "";
                String symbol = interact.readsym(id[0][i]);

                Uri Url = Uri.parse(baseAddress)
                        .buildUpon()
                        .appendQueryParameter("symbol", symbol)
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
                        continue;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    while ((temp = reader.readLine()) != null) {
                        DATA += temp;
                    }
                    Log.d("unedited", DATA);
                }
                catch (IOException e) {
                    Log.e("createList", "Error in url ", e);
                    continue;
                }

                JSONObject jsonObject = null;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:00");
                String currtime;
                try {
                    String List = "Time Series (1min)";
                    jsonObject = new JSONObject(DATA);
                    jsonObject = jsonObject.getJSONObject(List);
                }
                catch (JSONException e) {
                    Log.e("createList", "Error in json");
                }

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis() + 9000000 * 60000);
                currtime = sdf.format(calendar.getTime());
                Float closeva;

                try {
                    JSONObject tempobj = jsonObject.getJSONObject(currtime);                   //change here
                    closeva = Float.parseFloat(tempobj.getString("4. close"));
                    if (closeva < Float.parseFloat(interact.readreqd(symbol)))
                        showNotification(context,closeva,symbol);
                }
                catch(JSONException e)
                {
                    Log.e("createList", "Error in json", e);
                }

            }
            return null;
        }
        void showNotification(Context context,Float closeva,String symbol)
        {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            //.setSmallIcon(R.drawable.notification_img)
                            .setContentTitle(symbol + " price is low : " + String.valueOf(closeva));
            mBuilder.setAutoCancel(true);
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mBuilder.setSound(alarmSound);
            Intent resultIntent = new Intent(context, detailactivity.class);
            resultIntent.putExtra("symbol",symbol);

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
            mNotificationManager.notify(5, mBuilder.build());
        }

    }
}
