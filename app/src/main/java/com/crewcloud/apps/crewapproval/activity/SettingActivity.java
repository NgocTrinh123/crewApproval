package com.crewcloud.apps.crewapproval.activity;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.crewcloud.apps.crewapproval.CrewCloudApplication;
import com.crewcloud.apps.crewapproval.R;
import com.crewcloud.apps.crewapproval.dtos.ErrorDto;
import com.crewcloud.apps.crewapproval.interfaces.BaseHTTPCallBack;
import com.crewcloud.apps.crewapproval.util.HttpRequest;
import com.crewcloud.apps.crewapproval.util.PreferenceUtilities;
import com.crewcloud.apps.crewapproval.util.Util;
import com.crewcloud.apps.crewapproval.util.WebClient;
import com.fasterxml.jackson.databind.JsonNode;

public class SettingActivity extends BaseActivity implements View.OnClickListener {
    private ImageView img_avatar;
    private LinearLayout ln_profile, ln_general, ln_notify, ln_logout;
    public PreferenceUtilities prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_page_layout);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.myColor_PrimaryDark));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.nav_back_ic);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ln_profile = (LinearLayout) findViewById(R.id.ln_profile);
        ln_general = (LinearLayout) findViewById(R.id.ln_general);
        ln_notify = (LinearLayout) findViewById(R.id.ln_notify);
        ln_logout = (LinearLayout) findViewById(R.id.ln_logout);
        ln_profile.setOnClickListener(this);
        ln_general.setOnClickListener(this);
        ln_notify.setOnClickListener(this);
        ln_logout.setOnClickListener(this);
//        PreferenceUtilities preferenceUtilities = CreCloudApplication.getInstance().getPreferenceUtilities();
//        String serviceDomain = prefUtils.getCurrentServiceDomain();
//        String avatar = prefUtils.getAvatar();
//        String newAvatar = avatar.replaceAll("\"", "");
//        String mUrl = serviceDomain + newAvatar;

        img_avatar = (ImageView) findViewById(R.id.img_avatar);
//        UserDto userDto = UserDBHelper.getUser();
        Util.showImage(CrewCloudApplication.getInstance().getPreferenceUtilities().getUserAvatar(), img_avatar);
    }

    @Override
    public void onClick(View v) {
        if (v == ln_profile) {
//            Intent intent = new Intent(SettingActivity.this, LogoutActivity.class);
//            startActivity(intent);
            BaseActivity.Instance.callActivity(ProfileActivity.class);
        } else if (v == ln_general) {
            Toast.makeText(getApplicationContext(), "undev", Toast.LENGTH_SHORT).show();
        } else if (v == ln_notify) {
//            Intent intent = new Intent(SettingActivity.this, NotificationSettingActivity.class);
//            startActivity(intent);
            BaseActivity.Instance.callActivity(NotificationSettingActivity.class);
        } else if (v == ln_logout) {
            logout();
        }
    }

    public void logout() {
        HttpRequest.getInstance().deleteAndroidDevice(new BaseHTTPCallBack() {
            @Override
            public void onHTTPSuccess() {
                new SettingActivity.WebClientAsync_Logout_v2().execute();
            }

            @Override
            public void onHTTPFail(ErrorDto errorDto) {
                new SettingActivity.WebClientAsync_Logout_v2().execute();
            }
        });

    }

    private class WebClientAsync_Logout_v2 extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            final PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();

            WebClient.Logout_v2(preferenceUtilities.getCurrentMobileSessionId(),
                    "http://" + preferenceUtilities.getCurrentCompanyDomain(), new WebClient.OnWebClientListener() {
                        @Override
                        public void onSuccess(JsonNode jsonNode) {
//                            preferenceUtilities.setCurrentMobileSessionId("");
                        }

                        @Override
                        public void onFailure() {
                        }
                    });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();
            preferenceUtilities.setCurrentMobileSessionId("");
            preferenceUtilities.setCurrentCompanyNo(0);
            preferenceUtilities.setCurrentServiceDomain("");
            preferenceUtilities.setCurrentCompanyDomain("");
            preferenceUtilities.setCurrentUserID("");
            preferenceUtilities.setUserAvatar("");
            BaseActivity.Instance.startSingleActivity(LoginActivity.class);
            finish();
        }
    }
}