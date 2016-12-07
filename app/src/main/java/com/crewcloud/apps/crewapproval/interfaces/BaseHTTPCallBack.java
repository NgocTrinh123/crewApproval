package com.crewcloud.apps.crewapproval.interfaces;


import com.crewcloud.apps.crewapproval.dtos.ErrorDto;

public interface BaseHTTPCallBack {
    void onHTTPSuccess();
    void onHTTPFail(ErrorDto errorDto);
}
