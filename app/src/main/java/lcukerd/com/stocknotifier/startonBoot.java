package lcukerd.com.stocknotifier;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Programmer on 03-05-2017.
 */

public class startonBoot extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            DbInteract interact = new DbInteract(context);
            Intent callagain = new Intent(context,notifer.class);
            try {
                if (interact.checked() != null)
                    callagain.putExtra("id", interact.checked());
                else
                {
                    callagain.putExtra("id", new String[0]);
                    Log.d("main","passing null to notifier");
                }
            }
            catch (Exception e) {
                Log.e("Main","Null pointer exception");
            }
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, callagain, 0);
            AlarmManager notifalm;
            notifalm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            notifalm.setExact(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),alarmIntent);
        }
    }
}
