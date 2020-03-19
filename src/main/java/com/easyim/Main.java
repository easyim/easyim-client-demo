package com.easyim;


import com.broker.base.protocol.hook.UserLoginForm;
import com.easyim.api.AIOApi;
import com.easyim.api.ApiResponse;
import com.easyim.api.apis.ApiConfig;
import com.easyim.api.apis.EasyIMApis;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 *  easyim-client demo
 * */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        String userAuid = "admin";
        String userToken = "admin";
        if(args.length >= 1){
            ApiConfig.getInstance().setHost(args[0].split(":")[0]);
            ApiConfig.getInstance().setPort(Integer.parseInt(args[0].contains(":") ? args[0].split(":")[1] : "9091"));
        }
        if(args.length >= 2){
            userAuid = args[1];
        }
        if(args.length >= 3){
            userToken = args[2];
        }

        ApiResponse<String> responseMessage = EasyIMApis.login.call(new UserLoginForm().setAuid(userAuid).setToken(userToken));
        // socket 实例
        Socket socket = AIOApi.getSocket();
        socket.on("topic.message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if(args.length < 3){
                    return;
                }
                String msgs = args[0].toString();
                String ackId = args[2].toString();
                socket.emit("topic.clientack", args[2]); // 使用 topic.clientack 发送回执.
                System.out.println("receive message:" + msgs + "  AUTO_ACK_ID = "+ackId);
            }
        });

        while (true){
            Thread.sleep(1000);
        }
    }
}
