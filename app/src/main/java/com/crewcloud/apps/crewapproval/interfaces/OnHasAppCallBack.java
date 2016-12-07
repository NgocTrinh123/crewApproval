package com.crewcloud.apps.crewapproval.interfaces;


import com.crewcloud.apps.crewapproval.dtos.ErrorDto;

public interface OnHasAppCallBack {
    void hasApp();
    void noHas(ErrorDto dto);
}
