package com.easyim.common;


import java.util.Arrays;
import java.util.List;

/**
 * @author kong <androidsimu@163.com>
 * create by 2019/2/21 9:28
 * Description: easyim
 **/
public class Topic {
    public static final String TOPIC_PREFIX = "topic.";
    public static final String HTTP_API_PREFIX = "/v1/";

    public static class CONNECTION{
        public static final String name = TOPIC_PREFIX +"connection";
//        public static final String base_uri = "connection";
        public static final String base_uri = HTTP_API_PREFIX +"connection";
        public static class METHOD{
               public static final String AUTHORITY_REQUEST = "authority/request";

        }
    }
}
