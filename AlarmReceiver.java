package ir.yadavaran.tasks;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String CHANNEL="task_reminders";
    @Override public void onReceive(Context c,Intent intent){
        long id=intent.getLongExtra("id",-1);
        TaskItem t=TaskStore.find(c,id);
        if(t==null||t.done)return;
        NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=26){
            Uri sound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            NotificationChannel ch=new NotificationChannel(CHANNEL,"یادآوری کارها",NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("هشدارهای کارهای ثبت‌شده");
            ch.enableVibration(true); ch.setSound(sound,null); nm.createNotificationChannel(ch);
        }
        Intent done=new Intent(c,DoneReceiver.class); done.putExtra("id",id);
        PendingIntent action=PendingIntent.getBroadcast(c,(int)(id & 0x7fffffff),done,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Intent open=new Intent(c,MainActivity.class);
        PendingIntent content=PendingIntent.getActivity(c,(int)((id+17)&0x7fffffff),open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder b=new NotificationCompat.Builder(c,CHANNEL)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("یادآور ماهانه")
            .setContentText(t.title).setStyle(new NotificationCompat.BigTextStyle().bigText("زمان انجام این کار رسیده است: "+t.title))
            .setPriority(NotificationCompat.PRIORITY_MAX).setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true).setContentIntent(content)
            .addAction(android.R.drawable.checkbox_on_background,"انجام شد",action);
        nm.notify((int)(id & 0x7fffffff),b.build());
        // Repeat until the user marks it complete.
        if(t.repeatMinutes>0){
            t.time=System.currentTimeMillis()+t.repeatMinutes*60_000L;
            TaskStore.upsert(c,t);
            AlarmScheduler.schedule(c,t);
        }
    }
}
