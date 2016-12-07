package com.crewcloud.apps.crewapproval.activity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.*;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.webkit.*;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crewcloud.apps.crewapproval.CrewCloudApplication;
import com.crewcloud.apps.crewapproval.R;
import com.crewcloud.apps.crewapproval.base.WebContentChromeClient;
import com.crewcloud.apps.crewapproval.base.WebContentClient;
import com.crewcloud.apps.crewapproval.util.DeviceUtilities;
import com.crewcloud.apps.crewapproval.util.HttpRequest;
import com.crewcloud.apps.crewapproval.util.PreferenceUtilities;
import com.crewcloud.apps.crewapproval.util.WebClient;
import com.fasterxml.jackson.databind.JsonNode;

import java.lang.ref.WeakReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private WebView wvContent = null;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.hide();
        }

        wvContent = (WebView) findViewById(R.id.wvContent);
        mProgressBar = (ProgressBar) findViewById(R.id.pbProgress);
        initWebContent();
    }

    @Override
    protected void onResume() {
        super.onResume();

        PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();
        int timezone = preferenceUtilities.getTIME_ZONE();
        int Cur = DeviceUtilities.getTimeZoneOffset();
        if (timezone != Cur) {
            preferenceUtilities.setTIME_ZONE(Cur);
            HttpRequest.getInstance().updateTimeZone(preferenceUtilities.getGCMregistrationid());
        }
    }

    private void initWebContent() {
        WebSettings webSettings = wvContent.getSettings();

        webSettings.setAppCacheEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setSaveFormData(false);
        webSettings.setSupportZoom(false);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationEnabled(true);

        wvContent.setWebChromeClient(new WebContentChromeClient());
        wvContent.setWebViewClient(new WebContentClient(this, mProgressBar));

        wvContent.setVerticalScrollBarEnabled(true);
        wvContent.setHorizontalScrollBarEnabled(true);

        wvContent.addJavascriptInterface(new JavaScriptExtension(), "crewcloud");

        mFileDownloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
        wvContent.setDownloadListener(mDownloadListener);

        PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();

        String domain = preferenceUtilities.getCurrentCompanyDomain();

        CookieManager.getInstance().setCookie("http://" + domain, "skey0=" + preferenceUtilities.getCurrentMobileSessionId());
        CookieManager.getInstance().setCookie("http://" + domain, "skey1=" + "123123123123132");
        CookieManager.getInstance().setCookie("http://" + domain, "skey2=" + DeviceUtilities.getLanguageCode());
        CookieManager.getInstance().setCookie("http://" + domain, "skey3=" + preferenceUtilities.getCurrentCompanyNo());

        wvContent.loadUrl("http://" + domain + "/UI/_EAPPMobile/Main.aspx");
    }

    private final class JavaScriptExtension {

        @JavascriptInterface
        public void openSetting() {
            BaseActivity.Instance.callActivity(SettingActivity.class);
        }
    }

    public void logout() {
        new WebClientAsync_Logout_v2().execute();
    }

    private class WebClientAsync_Logout_v2 extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();

            WebClient.Logout_v2(preferenceUtilities.getCurrentMobileSessionId(),
                    "http://" + preferenceUtilities.getCurrentCompanyDomain(), new WebClient.OnWebClientListener() {
                        @Override
                        public void onSuccess(JsonNode jsonNode) {

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
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    // ----------------------------------------------------------------------------------------------

    private boolean mIsBackPressed = false;

    private static class ActivityHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public ActivityHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                activity.setBackPressed(false);
            }
        }
    }

    private final ActivityHandler mActivityHandler = new ActivityHandler(this);

    public void setBackPressed(boolean isBackPressed) {
        mIsBackPressed = isBackPressed;
    }

    @Override
    public void onBackPressed() {
        if (wvContent.canGoBack()) {
            wvContent.goBack();
        } else {
            if (!mIsBackPressed) {
                Toast.makeText(this, R.string.mainActivity_message_exit, Toast.LENGTH_SHORT).show();
                mIsBackPressed = true;
                mActivityHandler.sendEmptyMessageDelayed(0, 2000);
            } else {
                finish();
            }
        }
    }

    // ----------------------------------------------------------------------------------------------

    private DownloadManager mFileDownloadManager = null;
    private final Pattern CONTENT_DISPOSITION_PATTERN = Pattern.compile("attachment\\s*;\\s*filename\\s*=\\s*\"*([^\"]*)\"*");

    private DownloadListener mDownloadListener = new DownloadListener() {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
            String fileName = parseContentDisposition(contentDisposition);
            Uri uriToDownload = Uri.parse(url);
            DownloadManager.Request fileDownloadRequest = new DownloadManager.Request(uriToDownload);
            fileDownloadRequest.setTitle(fileName);
            fileDownloadRequest.setDescription("전자결재 첨부파일 다운로드");
            fileDownloadRequest.setDestinationInExternalPublicDir("/Download", fileName);
            fileDownloadRequest.setVisibleInDownloadsUi(true);
            fileDownloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            Environment.getExternalStoragePublicDirectory("/Download").mkdir();
            mFileDownloadManager.enqueue(fileDownloadRequest);

            Toast.makeText(MainActivity.this, "다운로드를 시작합니다.", Toast.LENGTH_SHORT).show();
        }

        private String parseContentDisposition(String contentDisposition) {
            try {
                Matcher m = CONTENT_DISPOSITION_PATTERN.matcher(contentDisposition);
                if (m.find()) {
                    return java.net.URLDecoder.decode(m.group(1), "UTF-8");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return "";
        }
    };
}