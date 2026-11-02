package ir.yadavaran.tasks;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmScheduler {
    static PendingIntent pending(Context c,long id){
        Intent i=new Intent(c,AlarmReceiver.class); i.putExtra("id",id);
        return PendingIntent.getBroadcast(c,(int)(id & 0x7fffffff),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    public static void schedule(Context c,TaskItem t){
        if(t.done)return;
        AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        long when=Math.max(t.time,System.currentTimeMillis()+1500);
        try {
            if(Build.VERSION.SDK_INT>=23) am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pending(c,t.id));
            else am.set(AlarmManager.RTC_WAKEUP,when,pending(c,t.id));
        } catch(SecurityException ignored) {}
    }
    public static void cancel(Context c,long id){
        AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pending(c,id));
    }
}
