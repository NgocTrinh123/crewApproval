package com.crewcloud.apps.crewapproval.activity;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.crewcloud.apps.crewapproval.CrewCloudApplication;
import com.crewcloud.apps.crewapproval.R;
import com.crewcloud.apps.crewapproval.util.HttpRequest;
import com.crewcloud.apps.crewapproval.util.PreferenceUtilities;


import java.util.Calendar;

public class NotificationSettingActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    View view_getnotify, view_sound, view_vibrate, view_notifi_time;
    TextView tv_getnoti, tv_sound, tv_vibrate, tv_time, tv_starthour, tv_endhour;
    Switch Switch_notify, Switch_sound, Switch_vibrate, Switch_time;
    PreferenceUtilities mPref;
    String regid = "", notificationOptions = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_page);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        init();

    }

    public void init() {
        mPref = CrewCloudApplication.getInstance().getPreferenceUtilities();
//        regid = mPref.getGCMregistrationid();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.nav_back_ic);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        view_getnotify = (View) findViewById(R.id.view_getnotify);
        view_sound = (View) findViewById(R.id.view_sound);
        view_vibrate = (View) findViewById(R.id.view_vibrate);
        view_notifi_time = (View) findViewById(R.id.view_notifi_time);
        tv_getnoti = (TextView) view_getnotify.findViewById(R.id.tv_switch);
        tv_sound = (TextView) view_sound.findViewById(R.id.tv_switch);
        tv_vibrate = (TextView) view_vibrate.findViewById(R.id.tv_switch);
        tv_time = (TextView) view_notifi_time.findViewById(R.id.tv_switch);
        tv_starthour = (TextView) findViewById(R.id.tv_starthour);
        tv_endhour = (TextView) findViewById(R.id.tv_endhour);
        String _start = mPref.getSTART_TIME();
        String _end = mPref.getEND_TIME();
        if (_start.length() == 0) {
            _start = "AM 08:00";
            mPref.setSTART_TIME(_start);
        }
        if (_end.length() == 0) {
            _end = "PM 06:00";
            mPref.setEND_TIME(_end);
        }
        tv_starthour.setText(_start);
        tv_endhour.setText(_end);
        tv_starthour.setOnClickListener(this);
        tv_endhour.setOnClickListener(this);
        tv_getnoti.setText(getResources().getString(R.string.getnotify));
        tv_sound.setText(getResources().getString(R.string.sound));
        tv_vibrate.setText(getResources().getString(R.string.vibrate));
        tv_time.setText(getResources().getString(R.string.notifi_time));
        Switch_notify = (Switch) view_getnotify.findViewById(R.id.switch1);
        Switch_sound = (Switch) view_sound.findViewById(R.id.switch1);
        Switch_vibrate = (Switch) view_vibrate.findViewById(R.id.switch1);
        Switch_time = (Switch) view_notifi_time.findViewById(R.id.switch1);
        Switch_notify.setOnCheckedChangeListener(this);
        Switch_notify.setChecked(mPref.getNOTIFI_MAIL());
        Switch_sound.setChecked(mPref.getNOTIFI_SOUND());
        Switch_vibrate.setChecked(mPref.getNOTIFI_VIBRATE());
        Switch_time.setChecked(mPref.getNOTIFI_TIME());
        setEnable(Switch_notify.isChecked());
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setEnable(isChecked);
    }

    public void setEnable(boolean isChecked){
        Switch_sound.setEnabled(isChecked);
        Switch_vibrate.setEnabled(isChecked);
        Switch_time.setEnabled(isChecked);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPref.setNOTIFI_MAIL(Switch_notify.isChecked());
        mPref.setNOTIFI_SOUND(Switch_sound.isChecked());
        mPref.setNOTIFI_VIBRATE(Switch_vibrate.isChecked());
        mPref.setNOTIFI_TIME(Switch_time.isChecked());
        mPref.setSTART_TIME(tv_starthour.getText().toString().trim());
        mPref.setEND_TIME(tv_endhour.getText().toString().trim());
        notificationOptions = "{" +
                "\"enabled\": " + Switch_notify.isChecked() + "," +
                "\"sound\": " + Switch_sound.isChecked() + "," +
                "\"vibrate\": " + Switch_vibrate.isChecked() + "," +
                "\"notitime\": " + Switch_time.isChecked() + "," +
                "\"starttime\": \"" + getFullHour(tv_starthour) + "\"," +
                "\"endtime\": \"" + getFullHour(tv_endhour) + "\"" + "}";
        notificationOptions = notificationOptions.trim();
//        Log.e(TAG, "notificationOptions:"+notificationOptions);
//        Log.e(TAG, "regid:"+regid);
        HttpRequest.getInstance().updateAndroidDevice(mPref.getGCMregistrationid(),notificationOptions);
    }

    @Override
    public void onClick(View v) {
        if (v == tv_starthour) {
            ShowTimerDialog(tv_starthour, tv_endhour);
        } else if (v == tv_endhour) {
            ShowTimerDialogEnd(tv_endhour, tv_starthour);
        }
    }

    public void ShowTimerDialog(final TextView tv, final TextView tv2) {
        Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        int h = getHour(tv2);
                        int m = getMinute(tv2);
                        if (hourOfDay < h) {
                            tv.setText(getFullHour(hourOfDay, minute));
                        } else if (hourOfDay > h) {
                            tv.setText(getFullHour(hourOfDay, minute));
                            tv2.setText(getFullHour(hourOfDay, minute));
                        } else {
                            if (minute < m) {
                                tv.setText(getFullHour(hourOfDay, minute));
                            } else {
                                tv.setText(getFullHour(hourOfDay, minute));
                                tv2.setText(getFullHour(hourOfDay, minute));
                            }
                        }
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    public void ShowTimerDialogEnd(final TextView tv, final TextView tv2) {
        Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        int h = getHour(tv2);
                        int m = getMinute(tv2);
                        if (hourOfDay > h) {
                            tv.setText(getFullHour(hourOfDay, minute));
                        } else if (hourOfDay < h) {
                            tv.setText(getFullHour(hourOfDay, minute));
                            tv2.setText(getFullHour(hourOfDay, minute));
                        } else {
                            if (minute > m) {
                                tv.setText(getFullHour(hourOfDay, minute));
                            } else {
                                tv.setText(getFullHour(hourOfDay, minute));
                                tv2.setText(getFullHour(hourOfDay, minute));
                            }
                        }
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    public String getFullHour(int hour, int minute) {
        String str_h = "", str_m = "";
        String AM = "AM";
        if (hour > 12) {
            AM = "PM";
            hour -= 12;
        } else {
            AM = "AM";
        }
        if (hour < 10) str_h = "0" + hour;
        else str_h = "" + hour;
        if (minute < 10) str_m = "0" + minute;
        else str_m = "" + minute;
        String text = AM + " " + str_h + ":" + str_m;
        return text;
    }

    public int getHour(TextView tv) {
        int h = 0;
        String str[] = tv.getText().toString().split(" ");
        h = Integer.parseInt(str[1].split(":")[0]);
        if (str[0].equalsIgnoreCase("PM")) h += 12;
        return h;
    }

    public int getMinute(TextView tv) {
        int h = 0;
        String str[] = tv.getText().toString().split(" ");
        h = Integer.parseInt(str[1].split(":")[1]);
        return h;
    }

    public String getFullHour(TextView tv) {
        String hour = "", minute = "";
        int h = getHour(tv);
        int m = getMinute(tv);
        if (h < 10) hour = "0" + h;
        else hour = "" + h;
        if (m < 10) minute = "0" + m;
        else minute = "" + m;
        String text = hour + ":" + minute;
        return text;
    }
}
