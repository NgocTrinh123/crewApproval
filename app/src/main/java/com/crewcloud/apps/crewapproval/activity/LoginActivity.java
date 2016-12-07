package com.crewcloud.apps.crewapproval.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crewcloud.apps.crewapproval.CrewCloudApplication;
import com.crewcloud.apps.crewapproval.R;
import com.crewcloud.apps.crewapproval.dtos.ErrorDto;
import com.crewcloud.apps.crewapproval.interfaces.BaseHTTPCallBack;
import com.crewcloud.apps.crewapproval.interfaces.OnAutoLoginCallBack;
import com.crewcloud.apps.crewapproval.interfaces.OnHasAppCallBack;
import com.crewcloud.apps.crewapproval.util.HttpRequest;
import com.crewcloud.apps.crewapproval.util.PreferenceUtilities;
import com.crewcloud.apps.crewapproval.util.SoftKeyboardDetectorView;
import com.crewcloud.apps.crewapproval.util.Util;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends BaseActivity implements BaseHTTPCallBack, OnHasAppCallBack {
    private RelativeLayout include_logo;
    private ImageView img_login_logo;
    private TextView tv_login_logo_text;
    private EditText login_edt_server, login_edt_username, login_edt_password;
    private RelativeLayout login_btn_login;
    private LinearLayout ll_login_sign_up;
    public PreferenceUtilities mPrefs;
    private boolean mFirstLogin = true;
    private String mInputUsername, mInputPassword;

    protected int mActivityNumber = 0;
    private boolean mFirstStart = false;

    private boolean isAutoLoginShow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_v2);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.myColor_PrimaryDark));
        }
        registerInBackground();

        mPrefs = CrewCloudApplication.getInstance().getPreferenceUtilities();

        include_logo = (RelativeLayout) findViewById(R.id.include_logo);
        include_logo.setVisibility(View.VISIBLE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_ACTION);
        registerReceiver(accountReceiver, intentFilter);

        final SoftKeyboardDetectorView softKeyboardDetectorView = new SoftKeyboardDetectorView(this);
        addContentView(softKeyboardDetectorView, new FrameLayout.LayoutParams(-1, -1));

        softKeyboardDetectorView.setOnShownKeyboard(new SoftKeyboardDetectorView.OnShownKeyboardListener() {
            @Override
            public void onShowSoftKeyboard() {
                if (img_login_logo != null) {
                    img_login_logo.setVisibility(View.GONE);
                    ll_login_sign_up.setVisibility(View.GONE);

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv_login_logo_text.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    tv_login_logo_text.setLayoutParams(params);
                    tv_login_logo_text.setText(Util.getString(R.string.app_name));
                }
            }
        });

        softKeyboardDetectorView.setOnHiddenKeyboard(new SoftKeyboardDetectorView.OnHiddenKeyboardListener() {
            @Override
            public void onHiddenSoftKeyboard() {
                if (img_login_logo != null) {

                    img_login_logo.setVisibility(View.VISIBLE);
                    ll_login_sign_up.setVisibility(View.GONE);

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv_login_logo_text.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                    tv_login_logo_text.setLayoutParams(params);
                    tv_login_logo_text.setText(Util.getString(R.string.app_name));

                }
            }
        });

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.myColor_PrimaryDark));
        }

        Bundle bundle = getIntent().getExtras();

        if (bundle != null && bundle.getInt("count_id") != 0) {
            mActivityNumber = bundle.getInt("count_id");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(accountReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPrefs.getIntroCount() < 1) {
            mFirstStart = true;
            mPrefs.putaeSortType(2);
        }
        initAtStart();
    }

    public void initAtStart() {
//        if (mPrefs.getIntroCount() < 2 && !mHasIntro && mActivityNumber == 0) {
//            callActivity(IntroActivity.class);
//            mHasIntro = true;
//        } else {
        firstChecking();
//        }
    }

    private void firstChecking() {
        if (mFirstLogin) {
            include_logo = (RelativeLayout) findViewById(R.id.include_logo);

            if (Util.isNetworkAvailable()) {
                if (mFirstStart) {
                    doLogin();
                    mFirstStart = false;
                } else {
                    doLogin();
                }
            } else {
                showNetworkDialog();
            }
        }
    }

    private void doLogin() {
        if (Util.checkStringValue(mPrefs.getCurrentMobileSessionId())) {
            HttpRequest.getInstance().checkLogin(this);
        } else {
            include_logo.setVisibility(View.GONE);
            mFirstLogin = false;
            init();
        }
    }

    @Override
    public void showNetworkDialog() {
        if (Util.isWifiEnable()) {
            displayAddAlertDialog(getString(R.string.app_name), getString(R.string.no_connection_error), getString(R.string.string_ok), null,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }, null);
        } else {
            displayAddAlertDialog(getString(R.string.app_name), getString(R.string.no_wifi_error), getString(R.string.turn_wifi_on), getString(R.string.string_cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent wireLess = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            startActivity(wireLess);
                            finish();
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
        }
    }

    public static String BROADCAST_ACTION = "com.dazone.crewcloud.account.receive";

    BroadcastReceiver accountReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String receiverPackageName = intent.getExtras().getString("receiverPackageName");
            if (LoginActivity.this.getPackageName().equals(receiverPackageName)) {
                //String senderPackageName = intent.getExtras().getString("senderPackageName");
                String companyID = intent.getExtras().getString("companyID");
                String userID = intent.getExtras().getString("userID");
                if (!TextUtils.isEmpty(companyID) && !TextUtils.isEmpty(userID) && !isAutoLoginShow) {
                    isAutoLoginShow = true;
                    showPopupAutoLogin(companyID, userID);
                }
            }
        }
    };

    private void showPopupAutoLogin(final String companyID, final String userID) {
        /*AutoLoginFragment cdd = new AutoLoginFragment(this, companyID, userID);
        cdd.setTitle(Util.getString(R.string.auto_login_title));
        cdd.show();*/

        String alert1 = Util.getString(R.string.auto_login_company_ID) + companyID;
        String alert2 = Util.getString(R.string.auto_login_user_ID) + userID;
        String alert3 = Util.getString(R.string.auto_login_text);

        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this)
                .setTitle(Util.getString(R.string.auto_login_title))
                .setMessage(alert1 + "\n" + alert2 + "\n\n" + alert3)
                .setPositiveButton(Util.getString(R.string.auto_login_button_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        autoLogin(companyID, userID);
                    }
                })
                .setNegativeButton(Util.getString(R.string.auto_login_button_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        //builder.show();

        final AlertDialog alertDialog = builder.create();

        alertDialog.show();

        TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
        if (textView != null) {
            //textView.setTextSize(18);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        }

        /*FragmentManager fm = getSupportFragmentManager();
        AutoLoginFragment autoLoginFragment = new AutoLoginFragment();
        Bundle bundle = new Bundle();
        bundle.putString("companyID", companyID);
        bundle.putString("userID", userID);
        autoLoginFragment.setArguments(bundle);
        autoLoginFragment.show(fm, "AutoLogin");*/
    }

    public void autoLogin(String companyID, String userID) {

        mInputUsername = userID;
        server_site = companyID;

        server_site = getServerSite(server_site);
        String company_domain = server_site;

        if (!company_domain.startsWith("http")) {
            server_site = "http://" + server_site;
        }

        String temp_server_site = server_site;

        if (temp_server_site.contains(".bizsw.co.kr")) {
            temp_server_site = "http://www.bizsw.co.kr:8080";
        } else {
            if (temp_server_site.contains("crewcloud")) {
                temp_server_site = "http://www.crewcloud.net";
            }
        }
        showProgressDialog();

        PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();
        preferenceUtilities.setCurrentServiceDomain(temp_server_site); // Domain
        preferenceUtilities.setCurrentCompanyDomain(company_domain); // group ID

        HttpRequest.getInstance().AutoLogin(company_domain, mInputUsername, temp_server_site, new OnAutoLoginCallBack() {
            @Override
            public void OnAutoLoginSuccess(String response) {
                if (!TextUtils.isEmpty(server_site)) {
                    CrewCloudApplication.getInstance().getPreferenceUtilities().setCurrentServiceDomain(server_site);
                    CrewCloudApplication.getInstance().getPreferenceUtilities().setCurrentUserID(mInputUsername);
                }

                loginSuccess();
            }

            @Override
            public void OnAutoLoginFail(ErrorDto dto) {
                if (mFirstLogin) {
                    dismissProgressDialog();

                    mFirstLogin = false;
                    include_logo.setVisibility(View.GONE);
                    init();
                } else {
                    dismissProgressDialog();
                    String error_msg = dto.message;

                    if (TextUtils.isEmpty(error_msg)) {
                        error_msg = getString(R.string.connection_falsed);
                    }

                    showSaveDialog(error_msg);
                }
            }
        });
    }

    private void init() {
        /** SEND BROADCAST */
        Intent intent = new Intent();
        intent.setAction("com.dazone.crewcloud.account.get");
        intent.putExtra("senderPackageName", this.getPackageName());
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(intent);

        img_login_logo = (ImageView) findViewById(R.id.img_login_logo);
        tv_login_logo_text = (TextView) findViewById(R.id.tv_login_logo_text);
        login_edt_username = (EditText) findViewById(R.id.login_edt_username);
        login_edt_password = (EditText) findViewById(R.id.login_edt_password);
        login_edt_server = (EditText) findViewById(R.id.login_edt_server);
        ll_login_sign_up = (LinearLayout) findViewById(R.id.ll_login_sign_up);

        login_edt_username.setPrivateImeOptions("defaultInputmode=english;");
        login_edt_server.setPrivateImeOptions("defaultInputmode=english;");

        login_edt_username.setText(new PreferenceUtilities().getUserId());
        login_edt_password.setText(new PreferenceUtilities().getPass());
        login_edt_server.setText(new PreferenceUtilities().getDomain());

        login_edt_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String result = s.toString().replaceAll(" ", "");
                if (!s.toString().equals(result)) {
                    login_edt_username.setText(result);
                    login_edt_username.setSelection(result.length());
                }
            }
        });

        login_edt_server.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String result = s.toString().replaceAll(" ", "");

                if (!s.toString().equals(result)) {
                    login_edt_server.setText(result);
                    login_edt_server.setSelection(result.length());
                }
            }
        });

        login_edt_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    login_btn_login.callOnClick();
                }

                return false;
            }
        });

        login_btn_login = (RelativeLayout) findViewById(R.id.login_btn_login);
        RelativeLayout login_btn_sign_up = (RelativeLayout) findViewById(R.id.login_btn_sign_up);

        if (login_btn_sign_up != null) {
            login_btn_sign_up.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Intent intent = new Intent(LoginV2Activity.this, SignUpActivity.class);
                    //startActivity(intent);
                    //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });
        }

        if (login_btn_login != null) login_btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInputUsername = login_edt_username.getText().toString();
                mInputPassword = login_edt_password.getText().toString();
                server_site = login_edt_server.getText().toString();

                if (TextUtils.isEmpty(checkStringValue(server_site, mInputUsername, mInputPassword))) {
                    server_site = getServerSite(server_site);
                    String company_domain = server_site;
                    if (!company_domain.startsWith("http")) {
                        server_site = "http://" + server_site;
                    }

                    String temp_server_site = server_site;

                    if (temp_server_site.contains(".bizsw.co.kr")) {
                        temp_server_site = "http://www.bizsw.co.kr:8080";
                    } else {
                        if (temp_server_site.contains("crewcloud")) {
                            temp_server_site = "http://www.crewcloud.net";
                        }
                    }

                    showProgressDialog();

                    PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();

                    preferenceUtilities.setCurrentServiceDomain(temp_server_site); // Domain
                    preferenceUtilities.setCurrentCompanyDomain(company_domain); // group ID

                    HttpRequest.getInstance().login(LoginActivity.this, mInputUsername, mInputPassword, company_domain, temp_server_site);
                } else {
                    displayAddAlertDialog(getString(R.string.app_name), checkStringValue(server_site, mInputUsername, mInputPassword), getString(R.string.string_ok), null,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }, null);
                }
            }
        });
    }

    private String checkStringValue(String server_site, String username, String password) {
        String result = "";

        if (TextUtils.isEmpty(server_site)) {
            result += getString(R.string.string_server_site);
        }

        if (TextUtils.isEmpty(username)) {
            if (TextUtils.isEmpty(result)) {
                result += getString(R.string.login_username);
            } else {
                result += ", " + getString(R.string.login_username);
            }
        }

        if (TextUtils.isEmpty(password)) {
            if (TextUtils.isEmpty(result)) {
                result += getString(R.string.login_password);
            } else {
                result += ", " + getString(R.string.login_password);
            }
        }

        if (TextUtils.isEmpty(result)) {
            return result;
        } else {
            return result + " " + getString(R.string.login_empty_input);
        }
    }

    private String getServerSite(String server_site) {
        String[] domains = server_site.split("[.]");
        if (server_site.contains(".bizsw.co.kr") && !server_site.contains("8080")) {
            return server_site.replace(".bizsw.co.kr", ".bizsw.co.kr:8080");
        }

        if (domains.length == 1) {
            return domains[0] + ".crewcloud.net";
        } else {
            return server_site;
        }
    }

    @Override
    public void onHTTPSuccess() {
        if (!TextUtils.isEmpty(server_site)) {
            CrewCloudApplication.getInstance().getPreferenceUtilities().setCurrentServiceDomain(server_site);
        }

        loginSuccess();
    }

    private void loginSuccess() {
        insertAndroidDevice();
        dismissProgressDialog();
    }

    public void insertAndroidDevice() {
        String start_time = mPrefs.getSTART_TIME();
        String end_time = mPrefs.getEND_TIME();
        if (start_time.length() == 0) {
            start_time = "AM 08:00";
            mPrefs.setSTART_TIME(start_time);
        }
        if (end_time.length() == 0) {
            end_time = "PM 06:00";
            mPrefs.setEND_TIME(end_time);
        }

        String notificationOptions = "{" +
                "\"enabled\": " + mPrefs.getNOTIFI_MAIL() + "," +
                "\"sound\": " + mPrefs.getNOTIFI_SOUND() + "," +
                "\"vibrate\": " + mPrefs.getNOTIFI_VIBRATE() + "," +
                "\"notitime\": " + mPrefs.getNOTIFI_TIME() + "," +
                "\"starttime\": \"" + Util.getFullHour(start_time) + "\"," +
                "\"endtime\": \"" + Util.getFullHour(end_time) + "\"" + "}";
        notificationOptions = notificationOptions.trim();
        HttpRequest.getInstance().insertAndroidDevice(new BaseHTTPCallBack() {
            @Override
            public void onHTTPSuccess() {
                callActivity(MainActivity.class);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }

            @Override
            public void onHTTPFail(ErrorDto errorDto) {

            }
        }, regid, notificationOptions);
    }

    private void registerInBackground() {
        new register().execute("");
    }

    String regid;
    String msg = "";

    public class register extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                regid = FirebaseInstanceId.getInstance().getToken();
                msg = "Device registered, registration ID=" + regid;
            } catch (Exception ex) {
                msg = "Error :" + ex.getMessage();
            }

            return null;
        }

        protected void onPostExecute(Void unused) {
            mPrefs.setGCMregistrationid(regid);
//            ConnectionUtils.getInstance().InsertDevice(regid, notificationOptions);
        }
    }

    @Override
    public void onHTTPFail(ErrorDto errorDto) {
        if (mFirstLogin) {
            dismissProgressDialog();

            mFirstLogin = false;
            include_logo.setVisibility(View.GONE);
            init();
        } else {
            dismissProgressDialog();
            String error_msg = errorDto.message;

            if (TextUtils.isEmpty(error_msg)) {
                error_msg = getString(R.string.connection_falsed);
            }

            showSaveDialog(error_msg);
        }
    }

    @Override
    public void hasApp() {
        loginSuccess();
    }

    @Override
    public void noHas(ErrorDto errorDto) {
        if (mFirstLogin) {
            mFirstLogin = false;
            include_logo.setVisibility(View.GONE);
            init();
        } else {
            dismissProgressDialog();
            showSaveDialog(errorDto.message);
        }
    }

    private void showSaveDialog(String message) {
        displayAddAlertDialog(getString(R.string.app_name), message, getString(R.string.string_ok), null,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }, null);
    }
}