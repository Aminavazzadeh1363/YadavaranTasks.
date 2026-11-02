package ir.yadavaran.tasks;

import org.json.JSONObject;

public class TaskItem {
    public long id, time;
    public String title;
    public boolean done, monthly;
    public int repeatMinutes;

    public TaskItem(long id, String title, long time, boolean monthly, int repeatMinutes, boolean done) {
        this.id=id; this.title=title; this.time=time; this.monthly=monthly; this.repeatMinutes=repeatMinutes; this.done=done;
    }
    public JSONObject toJson() {
        JSONObject o=new JSONObject();
        try { o.put("id",id); o.put("title",title); o.put("time",time); o.put("monthly",monthly); o.put("repeatMinutes",repeatMinutes); o.put("done",done); } catch(Exception ignored) {}
        return o;
    }
    public static TaskItem fromJson(JSONObject o) {
        return new TaskItem(o.optLong("id"),o.optString("title"),o.optLong("time"),o.optBoolean("monthly"),o.optInt("repeatMinutes",10),o.optBoolean("done"));
    }
}
