package com.crewcloud.apps.crewapproval.util;

import android.util.Log;

import com.android.volley.Request;
import com.crewcloud.apps.crewapproval.CrewCloudApplication;
import com.crewcloud.apps.crewapproval.dtos.ErrorDto;
import com.crewcloud.apps.crewapproval.dtos.UserDto;
import com.crewcloud.apps.crewapproval.interfaces.BaseHTTPCallBack;
import com.crewcloud.apps.crewapproval.interfaces.OnAutoLoginCallBack;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private static HttpRequest mInstance;
    private static String root_link;

    public static HttpRequest getInstance() {
        if (null == mInstance) {
            mInstance = new HttpRequest();
        }

        root_link = CrewCloudApplication.getInstance().getPreferenceUtilities().getCurrentCompanyDomain();

        return mInstance;
    }

    public void login(final BaseHTTPCallBack baseHTTPCallBack, final String userID, final String password, final String companyDomain, String server_link) {
        final String url = server_link + Urls.URL_GET_LOGIN;
        Map<String, String> params = new HashMap<>();
        params.put("languageCode", Util.getPhoneLanguage());
        params.put("timeZoneOffset", "" + Util.getTimeOffsetInMinute());
        params.put("companyDomain", companyDomain);
        params.put("password", password);
        params.put("userID", userID);
        params.put("mobileOSVersion", "Android " + android.os.Build.VERSION.RELEASE);
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                Util.printLogs("User info =" + response);
                Gson gson = new Gson();
                UserDto userDto = gson.fromJson(response, UserDto.class);

                userDto.prefs.setCurrentMobileSessionId(userDto.session);
                userDto.prefs.setCurrentUserIsAdmin(userDto.PermissionType);
                userDto.prefs.setCurrentCompanyNo(userDto.CompanyNo);
                userDto.prefs.setCurrentUserNo(userDto.Id);
                userDto.prefs.setCurrentUserID(userDto.userID);
                userDto.prefs.setUserAvatar(userDto.avatar);
                userDto.prefs.setEmail(userDto.MailAddress);
                userDto.prefs.setFullName(userDto.FullName);
                userDto.prefs.setCompanyName(userDto.NameCompany);
                userDto.prefs.setDomain(companyDomain);
                userDto.prefs.setPass(password);
                userDto.prefs.setUserID(userID);


                baseHTTPCallBack.onHTTPSuccess();
            }

            @Override
            public void onFailure(ErrorDto error) {
                baseHTTPCallBack.onHTTPFail(error);
            }
        });
    }

    public void checkLogin(final BaseHTTPCallBack baseHTTPCallBack) {
        final String url = root_link + Urls.URL_CHECK_SESSION;
        Map<String, String> params = new HashMap<>();
        params.put("sessionId", "" + CrewCloudApplication.getInstance().getPreferenceUtilities().getCurrentMobileSessionId());
        params.put("languageCode", Util.getPhoneLanguage());
        params.put("timeZoneOffset", "" + Util.getTimeOffsetInMinute());
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                Gson gson = new Gson();
                UserDto userDto = gson.fromJson(response, UserDto.class);
                userDto.prefs.setCurrentMobileSessionId(userDto.session);
                userDto.prefs.setCurrentUserIsAdmin(userDto.PermissionType);
                userDto.prefs.setCurrentCompanyNo(userDto.CompanyNo);
                userDto.prefs.setCurrentUserNo(userDto.Id);
                userDto.prefs.setCurrentUserID(userDto.userID);
                //UserDBHelper.addUser(userDto);
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPSuccess();
                }
            }

            @Override
            public void onFailure(ErrorDto error) {
                if (baseHTTPCallBack != null) {
                    baseHTTPCallBack.onHTTPFail(error);
                }
            }
        });
    }

    public void AutoLogin(String companyDomain, String userID, String server_link, final OnAutoLoginCallBack callBack) {
        final String url = server_link + Urls.URL_AUTO_LOGIN;
        Map<String, String> params = new HashMap<>();
        params.put("languageCode", Util.getPhoneLanguage());
        params.put("timeZoneOffset", "" + Util.getTimeOffsetInMinute());
        params.put("companyDomain", companyDomain);
        params.put("userID", userID);
        params.put("mobileOSVersion", "Android " + android.os.Build.VERSION.RELEASE);
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                Util.printLogs("User info =" + response);
                Gson gson = new Gson();
                UserDto userDto = gson.fromJson(response, UserDto.class);
                userDto.prefs.setCurrentMobileSessionId(userDto.session);
                userDto.prefs.setCurrentUserIsAdmin(userDto.PermissionType);
                userDto.prefs.setCurrentCompanyNo(userDto.CompanyNo);
                userDto.prefs.setCurrentUserNo(userDto.Id);
                //UserDBHelper.addUser(userDto);

                callBack.OnAutoLoginSuccess(response);
            }

            @Override
            public void onFailure(ErrorDto error) {
                callBack.OnAutoLoginFail(error);
            }
        });
    }

    //----------------------------------------------- Notification ---------------------------------------------------------------

    public final static String URL_INSERT_ANDROID_DEVICE = "/UI/_EAPPMobile/EAPPMobileService.asmx/InsertAndroidDevice";
    public final static String URL_DELETE_ANDROID_DEVICE = "/UI/_EAPPMobile/EAPPMobileService.asmx/DeleteAndroidDevice";
    public final static String URL_UPDATE_ANDROID_DEVICE = "/UI/_EAPPMobile/EAPPMobileService.asmx/UpdateAndroidDevice_NotificationOptions";
    public final static String URL_UPDATE_TIMEZONE_ANDROID_DEVICE = "/UI/_EAPPMobile/EAPPMobileService.asmx/UpdateAndroidDevice_TimezoneOffset";

    public static void insertAndroidDevice(final BaseHTTPCallBack callBack, String regid, String json) {
        final String url = CrewCloudApplication.getInstance().getPreferenceUtilities().getCurrentServiceDomain() + URL_INSERT_ANDROID_DEVICE;

        Map<String, String> params = new HashMap<>();
        params.put("sessionId", "" + CrewCloudApplication.getInstance().getPreferenceUtilities().getCurrentMobileSessionId());
        params.put("timeZoneOffset", "" + Util.getTimezoneOffsetInMinutes());
        params.put("deviceID", regid);
        params.put("osVersion", "Android " + android.os.Build.VERSION.RELEASE);
        params.put("languageCode", Util.getPhoneLanguage());
        params.put("notificationOptions", json);
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                callBack.onHTTPSuccess();
//                Log.e(TAG,"response:"+response);
            }

            @Override
            public void onFailure(ErrorDto error) {

//                baseHTTPCallBack.onHTTPFail(error);
            }
        });
    }

    public static void updateAndroidDevice(String regid, String json) {
        final String url = CrewCloudApplication.getInstance().getPreferenceUtilities().getCurrentServiceDomain() + URL_UPDATE_ANDROID_DEVICE;

        Map<String, String> params = new HashMap<>();
        params.put("sessionId", "" + CrewCloudApplication.getInstance().getPreferenceUtilities().getCurrentMobileSessionId());
        params.put("timeZoneOffset", "" + Util.getTimezoneOffsetInMinutes());
        params.put("deviceID", regid);
        params.put("languageCode", Util.getPhoneLanguage());
        params.put("notificationOptions", json);
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {

                Log.e("Update API", "Update:" + response);
            }

            @Override
            public void onFailure(ErrorDto error) {

//                baseHTTPCallBack.onHTTPFail(error);
            }
        });
    }

    public static void deleteAndroidDevice(final BaseHTTPCallBack callBack) {
        final String url = CrewCloudApplication.getInstance().getPreferenceUtilities().getCurrentServiceDomain() + URL_DELETE_ANDROID_DEVICE;
        Map<String, String> params = new HashMap<>();
        params.put("sessionId", "" + CrewCloudApplication.getInstance().getPreferenceUtilities().getCurrentMobileSessionId());
        params.put("timeZoneOffset", "" + Util.getTimezoneOffsetInMinutes());
        params.put("languageCode", Util.getPhoneLanguage());
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                Log.e("Delete", " :" + response);
                callBack.onHTTPSuccess();
//                Log.e(TAG,"response:"+response);
            }

            @Override
            public void onFailure(ErrorDto error) {

//                baseHTTPCallBack.onHTTPFail(error);
            }
        });
    }

    public static void updateTimeZone(String regid) {
        final String url = CrewCloudApplication.getInstance().getPreferenceUtilities().getCurrentServiceDomain() + URL_UPDATE_TIMEZONE_ANDROID_DEVICE;

        Map<String, String> params = new HashMap<>();
        params.put("sessionId", "" + CrewCloudApplication.getInstance().getPreferenceUtilities().getCurrentMobileSessionId());
        params.put("timeZoneOffset", "" + Util.getTimezoneOffsetInMinutes());
        params.put("deviceID", regid);
        params.put("languageCode", Util.getPhoneLanguage());
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {

                Log.e("CrewApproval", "Update TimeZone response:" + response);
            }

            @Override
            public void onFailure(ErrorDto error) {

//                baseHTTPCallBack.onHTTPFail(error);
            }
        });
    }
}