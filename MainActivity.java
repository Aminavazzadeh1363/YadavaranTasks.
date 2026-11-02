package ir.yadavaran.tasks;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    LinearLayout root, listBox;
    TextView summary;
    final SimpleDateFormat fmt=new SimpleDateFormat("yyyy/MM/dd  HH:mm", Locale.getDefault());
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},22);
        buildUi();
        refresh();
    }
    int dp(float n){return (int)(n*getResources().getDisplayMetrics().density);}
    TextView text(String s,int size){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(0xff202124);v.setGravity(Gravity.RIGHT);return v;}
    Button button(String s){Button b=new Button(this);b.setText(s);return b;}
    void buildUi(){
        ScrollView sc=new ScrollView(this); root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(16),dp(18),dp(16),dp(18)); root.setBackgroundColor(0xfff4f6fb);
        sc.addView(root); setContentView(sc);
        TextView title=text("یادآور ماهانه",26); title.setGravity(Gravity.CENTER); title.setTextColor(0xff263b73); root.addView(title);
        TextView subtitle=text("کارهایت را ثبت کن؛ در زمان مقرر به تو هشدار می‌دهیم.",14); subtitle.setGravity(Gravity.CENTER); root.addView(subtitle);
        Button add=button("+ افزودن کار جدید"); add.setOnClickListener(v->addTask()); root.addView(add);
        summary=text(""); summary.setPadding(0,dp(12),0,dp(8)); root.addView(summary);
        listBox=new LinearLayout(this); listBox.setOrientation(LinearLayout.VERTICAL); root.addView(listBox);
        TextView note=text("نکته: برای هشدار قابل‌اعتماد، اجازه اعلان را فعال کن و در تنظیمات گوشی، محدودیت باتری این برنامه را بردار.",12); note.setPadding(0,dp(20),0,0); root.addView(note);
    }
    void refresh(){
        if(listBox==null)return;
        listBox.removeAllViews(); List<TaskItem> tasks=TaskStore.all(this);
        tasks.sort((a,b)->Long.compare(a.time,b.time));
        int todo=0,done=0;
        for(TaskItem t:tasks)if(t.done)done++;else todo++;
        summary.setText("انجام‌نشده: "+todo+"     انجام‌شده: "+done);
        if(tasks.isEmpty()){TextView empty=text("هنوز کاری ثبت نکرده‌ای. از «افزودن کار جدید» شروع کن.",16);empty.setPadding(0,dp(20),0,dp(20));listBox.addView(empty);}
        for(TaskItem t:tasks){
            LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(12),dp(8),dp(12),dp(8));card.setBackgroundColor(t.done?0xffe2f3e8:0xffffffff);
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.setMargins(0,dp(5),0,dp(5));listBox.addView(card,cp);
            TextView name=text((t.done?"✓ ":"🔔 ")+t.title,18);card.addView(name);
            TextView when=text(fmt.format(new Date(t.time))+(t.monthly?"  • تکرار ماهانه":"")+(t.done?"  • انجام شد":"  • تکرار هشدار هر "+t.repeatMinutes+" دقیقه"),13);card.addView(when);
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.RIGHT);
            if(!t.done){Button doneBtn=button("انجام شد");doneBtn.setOnClickListener(v->{TaskStore.markDone(this,t.id);refresh();});row.addView(doneBtn);}
            Button edit=button("ویرایش");edit.setOnClickListener(v->editTask(t));row.addView(edit);
            Button del=button("حذف");del.setOnClickListener(v->new AlertDialog.Builder(this).setMessage("این کار حذف شود؟").setNegativeButton("لغو",null).setPositiveButton("حذف",(d,w)->{AlarmScheduler.cancel(this,t.id);List<TaskItem>a=TaskStore.all(this);a.removeIf(x->x.id==t.id);TaskStore.saveAll(this,a);refresh();}).show());row.addView(del);
            card.addView(row);
        }
    }
    void addTask(){showEditor(null);}
    void editTask(TaskItem t){showEditor(t);}
    void showEditor(TaskItem old){
        EditText title=new EditText(this);title.setSingleLine(true);title.setHint("عنوان کار را بنویس");
        Calendar cal=Calendar.getInstance(); if(old!=null)cal.setTimeInMillis(old.time);
        final Calendar chosen=(Calendar)cal.clone();
        TextView dateText=text("",16), timeText=text("",16);
        Runnable updateDate=()->dateText.setText("تاریخ: "+String.format(Locale.getDefault(),"%04d/%02d/%02d",chosen.get(Calendar.YEAR),chosen.get(Calendar.MONTH)+1,chosen.get(Calendar.DAY_OF_MONTH)));
        Runnable updateTime=()->timeText.setText("ساعت: "+String.format(Locale.getDefault(),"%02d:%02d",chosen.get(Calendar.HOUR_OF_DAY),chosen.get(Calendar.MINUTE)));
        updateDate.run();updateTime.run();
        Button date=button("انتخاب تاریخ");date.setOnClickListener(v->new DatePickerDialog(this,(dp,y,m,d)->{chosen.set(y,m,d);updateDate.run();},chosen.get(Calendar.YEAR),chosen.get(Calendar.MONTH),chosen.get(Calendar.DAY_OF_MONTH)).show());
        Button time=button("انتخاب ساعت");time.setOnClickListener(v->new TimePickerDialog(this,(tp,h,m)->{chosen.set(Calendar.HOUR_OF_DAY,h);chosen.set(Calendar.MINUTE,m);chosen.set(Calendar.SECOND,0);updateTime.run();},chosen.get(Calendar.HOUR_OF_DAY),chosen.get(Calendar.MINUTE),true).show());
        CheckBox monthly=new CheckBox(this);monthly.setText("تکرار خودکار هر ماه");monthly.setChecked(old!=null&&old.monthly);
        CheckBox repeat=new CheckBox(this);repeat.setText("تکرار هشدار تا انجام کار (هر 10 دقیقه)");repeat.setChecked(old==null||old.repeatMinutes>0);
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(12),0,dp(12),0);
        box.addView(title);box.addView(dateText);box.addView(date);box.addView(timeText);box.addView(time);box.addView(monthly);box.addView(repeat);
        if(old!=null)title.setText(old.title);
        new AlertDialog.Builder(this).setTitle(old==null?"افزودن کار":"ویرایش کار").setView(box)
            .setNegativeButton("انصراف",null).setPositiveButton("ذخیره",(dialog,which)->{
                String s=title.getText().toString().trim();if(s.isEmpty()){Toast.makeText(this,"عنوان کار را وارد کن",Toast.LENGTH_SHORT).show();return;}
                long id=old==null?System.currentTimeMillis():old.id;
                TaskItem t=new TaskItem(id,s,chosen.getTimeInMillis(),monthly.isChecked(),repeat.isChecked()?10:0,false);
                TaskStore.upsert(this,t);AlarmScheduler.cancel(this,id);if(t.time>System.currentTimeMillis())AlarmScheduler.schedule(this,t);
                if(Build.VERSION.SDK_INT>=31){try{android.app.AlarmManager am=(android.app.AlarmManager)getSystemService(ALARM_SERVICE);if(!am.canScheduleExactAlarms()){startActivity(new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));}}catch(Exception ignored){}}
                refresh();
            }).show();
    }
}
