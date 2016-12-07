package com.crewcloud.apps.crewapproval.base;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.MailTo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.widget.ProgressBar;
import com.crewcloud.apps.crewapproval.activity.MainActivity;

public class WebContentClient extends WebViewClient {

    private MainActivity ParentActivity = null;
    private ProgressBar mProgressbar;

    public WebContentClient(MainActivity activity, ProgressBar progressBar) {
        this.ParentActivity = activity;
        this.mProgressbar = progressBar;
    }

    private Intent newEmailIntent(Context context, String address, String subject, String body, String cc) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { address });
        intent.putExtra(Intent.EXTRA_TEXT, body);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_CC, cc);
        intent.setType("message/rfc822");
        return intent;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if(url.startsWith("tel:")) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse(url));
            ParentActivity.startActivity(intent);
            return true;
        }
        if (url.startsWith("mailto:")) {
            MailTo mt = MailTo.parse(url);
            Intent email = newEmailIntent(ParentActivity, mt.getTo(), mt.getSubject(), mt.getBody(), mt.getCc());
            ParentActivity.startActivity(Intent.createChooser(email, "Choose an Email client :"));

            return true;

        } else if (url.startsWith("logout:")) {
            ParentActivity.logout();
            return true;
        } else {
            view.loadUrl(url);
        }

        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        //ParentActivity.MainActivityHandler.sendEmptyMessage(MainActivity.ACTION_SHOW_PROGRESSBAR);
        this.mProgressbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        //ParentActivity.MainActivityHandler.sendEmptyMessage(MainActivity.ACTION_HIDE_PROGRESSBAR);
        this.mProgressbar.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().sync();
        }
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        super.onReceivedSslError(view, handler, error);
        handler.proceed();
    }
}