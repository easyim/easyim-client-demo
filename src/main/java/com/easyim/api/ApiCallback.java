package com.easyim.api;


@FunctionalInterface
public interface ApiCallback {
    void apply(ApiResponse response);
}
