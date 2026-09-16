package com.example.springboot.common;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * websocket服务
 */
@ServerEndpoint(value = "/chatServer/{userId}")
@Component
public class WebSocketServer {

    /**
     * 用于打印日志
     */
    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    /**
     * 记录当前在线连接数
     */
    private static final Map<Integer, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Integer userId) {
        sessionMap.put(userId, session);
        log.info("用户加入，userId={}", userId);
        broadcastSession();
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session, @PathParam("userId") Integer userId) {
        sessionMap.remove(userId,session);
        log.info("用户退出，userId={}", userId);
        broadcastSession();
    }

    /**
     * 收到客户端消息后调用的方法
     * 后台收到客户端发送过来的消息
     * onMessage 是一个消息的中转站
     * 接受 浏览器端 socket.send 发送过来的 json数据
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(Session session, String message, @PathParam("userId") Integer userId) {
        log.info("服务端收到用户userId={}的消息:{}", userId, message);
        JSONObject obj = JSONUtil.parseObj(message);

        String text = obj.getStr("text");
        String type = obj.getStr("type");
        String time = obj.getStr("time");
        Integer fromUserId = obj.getInt("fromUserId");
        Integer toUserId = obj.getInt("toUserId");

        Session toSession = sessionMap.get(toUserId);
        if (toSession != null) {
            JSONObject jsonObject = new JSONObject();

            jsonObject.set("text", text);
            jsonObject.set("type", type);
            jsonObject.set("time", time);
            jsonObject.set("fromUserId", fromUserId);
            jsonObject.set("toUserId", toUserId);
            jsonObject.set("messageType", "chat");

            sendMessage(toSession, jsonObject.toString());
            log.info("发送给用户userId={}，消息：{}", toUserId, jsonObject.toString());
        } else {
            log.info("发送失败，未找到用户userId={}的session", toUserId);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error(throwable.getMessage());
    }

    /**
     * 广播给所有在线连接:当前所有在线用户id
     */
    private void broadcastSession() {
        JSONObject jsonObject = new JSONObject();
        // 获取当前所有在线用户id
        jsonObject.set("userIds", sessionMap.keySet());
        jsonObject.set("messageType", "broadcast");
        // 广播给所有在线连接
        for (Session session : sessionMap.values()) {
            sendMessage(session, jsonObject.toString());
        }
        log.info("广播给所有在线连接:当前所有在线用户id");
    }

    /**
     * 服务端发送消息给客户端
     */
    private void sendMessage(Session toSession, String message) {
        try {
            log.info("服务端给客户端[{}]发送消息{}", toSession.getId(), message);
            toSession.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error("服务端发送消息给客户端失败", e);
        }
    }

}

