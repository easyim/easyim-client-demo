package com.easyim.api;


import com.broker.base.protocol.ProtocolMessage;
import com.broker.base.protocol.hook.UserLoginForm;
import com.broker.base.protocol.request.RequestMessage;
import com.broker.base.protocol.response.ResponseMessage;
import com.broker.base.utils.ObjectUtils;
import com.easyim.api.apis.EasyIMApis;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;


@Slf4j
public abstract class AIOApi<K extends ProtocolMessage, V> implements IHttpConfig {
    private static final String LOG_TAG =  AIOApi.class.getName();

    public final static String REQUEST_IO = "SYNC";
    public final static Integer CONNECT_TIMEOUT_MS  = 10*1000; // socket 连接超时
    public final static Integer READ_TIMEOUT_MS  = 10*1000; // socket 读取超时

    protected String host;
    protected boolean needAuth = false;
    protected String url;
    protected String fullUrl;
    protected String name;
    protected String type;
    protected Class<V> clz;


    private static Socket socket;
    private static boolean connected = false;
    private static final Object connectLock = new Object();

    // 缓存的数字签名
    public static String jwt;

    public static Socket getSocket() {
        return socket;
    }


    public AIOApi(String url, String name, String type, boolean needAuth, Class<V> clz) {
        this.url = url;
        this.name = name;
        this.type = type;
        this.needAuth = needAuth;
        this.clz = clz;
    }

    public AIOApi(){}

    public ApiResponse<V> call(String fullUrl, K params){
        this.fullUrl = fullUrl;
        return this.call(params);
    }

    public ApiResponse<V> call(K params){
        String url = "";
        if(socket == null){
            try {
                socket = IO.socket("http://" + getHost() + ":" + getPort());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            if(socket == null){
                throw new RuntimeException("Socket init failed.");
            }
        }

        if(ObjectUtils.strNotEmpty(this.fullUrl)){
            url = this.fullUrl;
        }
        else {
            this.host = this.getHost();
//            url = this.host + "/" + this.url;
            url = this.url;
        }
        RequestMessage<K> request = buildRequest(url, params);
        System.out.println("==> Ready to call:["+ request.getTopic() + "]  method:" + request.getMethod() + " with params:" + ObjectUtils.json(request.getProtocolMessage()));
        try {
            // 等待连接完成.
            tryConnect();
            // socket 未连接
            if(!connected){
                return new ApiResponse<>(false);
            }
            // 使用数字签名
            if(this.needAuth && ObjectUtils.strEmpty(jwt)){
                ApiResponse<String> tokenResponse = EasyIMApis.login.call(new UserLoginForm()
                        .setAuid("admin")
                        .setToken("admin")
                        .setAppKey("TSDKTEST00001"));
                if(tokenResponse.isSucceed()){
                    jwt = tokenResponse.getData();
                    request.getProtocolMessage().setJwt(jwt);
                }
            }

            ResponseMessage<V> responseMessage = sendRequest(request);
            ApiResponse<V> apiResponse = response2ApiResponse(responseMessage);
            System.out.println("==> Ready to back:["+ request.getTopic() + "]  method:" + request.getMethod() +
                    " raw:" + apiResponse.getRaw()  +
                    "ERROR_MESSAGE:" + apiResponse.getErrorMessage() +
                    " request id:" + responseMessage.getRequestId());
            return apiResponse;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ApiResponse<V>();
    }

    private ApiResponse<V> response2ApiResponse(ResponseMessage<V> responseMessage){
        ApiResponse<V> apiResponse = new ApiResponse<>(responseMessage.getResponse().ok());
        apiResponse.setData(responseMessage.getResponse().getData());
        apiResponse.setErrorCode((int) responseMessage.getResponse().getCode());
        apiResponse.setErrorMessage(responseMessage.getResponse().getMsg());
        apiResponse.setRaw(responseMessage.getResponse() !=null ? ObjectUtils.json(responseMessage.getResponse()) : "");

        return apiResponse;
    }

    private boolean tryConnect() {
        synchronized (connectLock){
            if(connected)return true;

            socket.on(Socket.EVENT_CONNECT, args -> {
                connected = true;
            }).on(Socket.EVENT_DISCONNECT, args -> {
                connected = false;
            });
            socket.connect();
            int timeout = CONNECT_TIMEOUT_MS;
            while (!connected && timeout > 0){
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                timeout -= 10;
            }
            if(timeout <=0 ){
                log.error("socket connect timeout!");
                return false;
            }
        }

        return connected;
    }

    private ResponseMessage<V> sendRequest(RequestMessage request){
        if(ObjectUtils.strEmpty(request.getTopic())){
//            return new ResponseMessage<V>(false, " url == null ");
            return ResponseMessage.failed(request.getRequestId(), " url == null ");
        }
        final String requestId = request.getRequestId();
        request.setRequestId(requestId);
        AtomicReference<ResponseMessage<V>> response = new AtomicReference<>();
        socket.emit(request.getTopic(), new JSONObject(ObjectUtils.maps(request.getProtocolMessage())), (Ack) args -> {
            if(args.length == 0){
                return;
            }
            ResponseMessage<V> responseMessage = ObjectUtils.beans(args[0].toString(), ResponseMessage.class);
            if(!requestId.equals(responseMessage.getRequestId())){
                log.error(" requestId 不一致, 数据可能被篡改了.");
            }
            response.set(responseMessage);
        });

        int timeout = READ_TIMEOUT_MS;
        while (response.get() == null && timeout > 0){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            timeout -= 10;
        }
        if(timeout <=0 ){
            log.error("socket connect timeout!");
            return new ResponseMessage<>(false);
        }

        return response.get();

    }


    private RequestMessage<K> buildRequest(String url, K params){
        if(AIOApi.REQUEST_IO.equals(this.type)){
            RequestMessage<K> requestMessage = new RequestMessage<>();
            requestMessage.setTopic(url.substring(0, url.indexOf("/")));
            requestMessage.setMethod(url.substring(url.indexOf("/")+1));
            requestMessage.setRequestId("request_" + new Date().getTime()); // TODO: 创建客户端唯一性的request_id
            params.setRequestId(requestMessage.getRequestId());
            params.setTimestamp(new Date().getTime());
            params.setMethod(requestMessage.getMethod());
            requestMessage.setProtocolMessage(params);
            if(this.needAuth){
                requestMessage.getProtocolMessage().setJwt("");
            }
            return requestMessage;
        }
        else {
            throw new RuntimeException("not support for type:" + this.type);
        }
    }


    public abstract  String getHost();
    public abstract  Integer getPort();

}

