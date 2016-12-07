package com.crewcloud.apps.crewapproval.interfaces;


import com.crewcloud.apps.crewapproval.dtos.ErrorDto;

public interface BaseHTTPCallBackWithString {
    void onHTTPSuccess(String message);
    void onHTTPFail(ErrorDto errorDto);
}
