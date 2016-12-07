package com.crewcloud.apps.crewapproval;

import android.app.Application;
import android.text.TextUtils;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.crewcloud.apps.crewapproval.util.PreferenceUtilities;

public class CrewCloudApplication extends Application {
    private static CrewCloudApplication mInstance;
    private static PreferenceUtilities mPreferenceUtilities;
    private RequestQueue mRequestQueue;

    public static String getProjectCode() {
        return "_EAPP";
    }

    public CrewCloudApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized CrewCloudApplication getInstance() {
        return mInstance;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setRetryPolicy(new DefaultRetryPolicy(15000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? "CrewApproval" : tag);
        getRequestQueue().add(req);
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public synchronized PreferenceUtilities getPreferenceUtilities() {
        if (mPreferenceUtilities == null) {
            mPreferenceUtilities = new PreferenceUtilities();
        }

        return mPreferenceUtilities;
    }
}