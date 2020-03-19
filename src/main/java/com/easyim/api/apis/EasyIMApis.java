package com.easyim.api.apis;


import com.broker.base.protocol.hook.UserLoginForm;
import com.easyim.common.Topic;


public class EasyIMApis {
    public static IOApi<UserLoginForm, String> login = new IOApi<>(
            Topic.CONNECTION.name + "/" + Topic.CONNECTION.METHOD.AUTHORITY_REQUEST,
            "用户登录/请求授权", "SYNC", false, String.class);

}
