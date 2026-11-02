package ir.yadavaran.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TaskStore {
    private static final String PREF="tasks_db", KEY="items";
    private static SharedPreferences p(Context c){ return c.getSharedPreferences(PREF,Context.MODE_PRIVATE); }
    public static synchronized List<TaskItem> all(Context c){
        List<TaskItem> a=new ArrayList<>();
        try { JSONArray j=new JSONArray(p(c).getString(KEY,"[]")); for(int i=0;i<j.length();i++) a.add(TaskItem.fromJson(j.getJSONObject(i))); } catch(Exception ignored){}
        return a;
    }
    public static synchronized void saveAll(Context c,List<TaskItem> a){
        JSONArray j=new JSONArray(); for(TaskItem t:a) j.put(t.toJson()); p(c).edit().putString(KEY,j.toString()).apply();
    }
    public static synchronized void upsert(Context c,TaskItem t){
        List<TaskItem>a=all(c); boolean found=false;
        for(int i=0;i<a.size();i++) if(a.get(i).id==t.id){a.set(i,t);found=true;break;}
        if(!found)a.add(t); saveAll(c,a);
    }
    public static synchronized TaskItem find(Context c,long id){ for(TaskItem t:all(c))if(t.id==id)return t; return null; }
    public static synchronized void markDone(Context c,long id){
        TaskItem t=find(c,id); if(t==null)return;
        t.done=true; upsert(c,t);
        if(t.monthly){
            Calendar cal=Calendar.getInstance(); cal.setTimeInMillis(t.time); cal.add(Calendar.MONTH,1);
            TaskItem next=new TaskItem(System.currentTimeMillis(),t.title,cal.getTimeInMillis(),true,t.repeatMinutes,false);
            upsert(c,next); AlarmScheduler.schedule(c,next);
        }
        AlarmScheduler.cancel(c,id);
    }
}
