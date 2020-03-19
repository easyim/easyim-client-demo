package com.easyim.api.apis;


import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class ApiConfig {
    private String host = "localhost";
    private Integer port = 9091;

    private static ApiConfig config;
    public static ApiConfig getInstance(){
        if(config == null){
            config = new ApiConfig();
        }
        return config;
    }
}
