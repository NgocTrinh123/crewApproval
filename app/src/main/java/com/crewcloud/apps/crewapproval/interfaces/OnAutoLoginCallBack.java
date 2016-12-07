package com.crewcloud.apps.crewapproval.interfaces;


import com.crewcloud.apps.crewapproval.dtos.ErrorDto;

/**
 * Created by Dat on 7/27/2016.
 */
public interface OnAutoLoginCallBack {
    void OnAutoLoginSuccess(String response);
    void OnAutoLoginFail(ErrorDto dto);
}
