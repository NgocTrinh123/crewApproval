package com.crewcloud.apps.crewapproval.util;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.crewcloud.apps.crewapproval.CrewCloudApplication;
import com.crewcloud.apps.crewapproval.R;
import com.crewcloud.apps.crewapproval.activity.LoginActivity;
import com.crewcloud.apps.crewapproval.dtos.ErrorDto;
import com.crewcloud.apps.crewapproval.activity.BaseActivity;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;

public class WebServiceManager {

    public void doJsonObjectRequest(int requestMethod, final String url, final JSONObject bodyParam, final RequestListener<String> listener) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(requestMethod, url, bodyParam, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject json = new JSONObject(response.getString("d"));

                    if (url.contains(Urls.URL_HAS_APPLICATION)) {
                        listener.onSuccess(json.toString());
                    } else {
                        int isSuccess = json.getInt("success");
                        if (isSuccess == 1) {
                            listener.onSuccess(json.getString("data"));
                        } else {
                            ErrorDto errorDto = new Gson().fromJson(json.getString("error"), ErrorDto.class);
                            if (errorDto == null) {

                                errorDto = new ErrorDto();
                                errorDto.message = Util.getString(R.string.no_network_error);
                            } else {
                                if (errorDto.code == 0 && !url.contains(Urls.URL_CHECK_SESSION)) {
                                    // Custom store session error
                                    CrewCloudApplication.getInstance().getPreferenceUtilities().clearLogin();
                                    BaseActivity.Instance.startSingleActivity(LoginActivity.class);
                                }else{
                                    Util.printLogs("Form is invalid");
                                }
                            }

                            listener.onFailure(errorDto);
                        }
                    }

                } catch (JSONException e) {

                    ErrorDto errorDto = new ErrorDto();
                    errorDto.message = Util.getString(R.string.no_network_error);
                    listener.onFailure(errorDto);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ErrorDto errorDto = new ErrorDto();
                if (null != error) {
                    listener.onFailure(errorDto);
                } else {
                    listener.onFailure(errorDto);
                }
            }
        });
        CrewCloudApplication.getInstance().addToRequestQueue(jsonObjectRequest, url);
    }

    public interface RequestListener<T> {
        void onSuccess(T response);
        void onFailure(ErrorDto error);
    }
}