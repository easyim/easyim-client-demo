package com.easyim.api.apis;

import com.easyim.api.AIOApi;
import com.broker.base.protocol.ProtocolMessage;


/**
 * @author plhuang
 * @param <K>
 */
public class IOApi<K extends ProtocolMessage,V> extends AIOApi<K,V> {

    public IOApi(String url, String name, String type, boolean needAuth, Class<V> clz) {
        this.url = url;
        this.name = name;
        this.type = type;
        this.needAuth = needAuth;
        this.clz = clz;
    }

    @Override
    public String getHost() {
        return ApiConfig.getInstance().getHost();
    }

    @Override
    public Integer getPort(){
        return ApiConfig.getInstance().getPort();
    }

}
