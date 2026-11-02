package ir.yadavaran.tasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c,Intent i){
        if(Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())){
            for(TaskItem t:TaskStore.all(c)) if(!t.done) AlarmScheduler.schedule(c,t);
        }
    }
}
