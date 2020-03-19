package com.easyim.api;


import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class ApiResponse<T> {
    public static final int HTTP_RESPONSE_FAILED = 0;
    public static final int HTTP_RESPONSE_SUCCEED = 200;
    public static final int HTTP_RESPONSE_UNAUTHORIZED = 401;
    public static final int HTTP_RESPONSE_PAGE_NOT_FOUND = 404;

    private int responseCode = HTTP_RESPONSE_FAILED;
    private String raw = "";
    private int errorCode = -1;
    private String errorMessage;
    private T data;

    public ApiResponse(Boolean succeed){
        if(succeed){
            this.errorCode = 0;
            this.responseCode = HTTP_RESPONSE_SUCCEED;
        }
        else {
            this.errorCode = -1;
            this.responseCode = 0;
        }
    }

    public ApiResponse(){
    }

    public boolean isSucceed(){
        return this.errorCode == 0 && this.responseCode == HTTP_RESPONSE_SUCCEED;
    }

    public boolean isFailed(){
        return !this.isSucceed();
    }

}
