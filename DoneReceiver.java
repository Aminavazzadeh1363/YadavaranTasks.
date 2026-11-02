package ir.yadavaran.tasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationManager;

public class DoneReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c,Intent i){
        long id=i.getLongExtra("id",-1);
        TaskStore.markDone(c,id);
        NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel((int)(id & 0x7fffffff));
    }
}
