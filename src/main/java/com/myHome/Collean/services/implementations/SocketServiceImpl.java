package com.myHome.Collean.services.implementations;

import com.google.gson.Gson;
import com.myHome.Collean.constants.enums.MessageType;
import com.myHome.Collean.models.BroadcastingMessage;
import com.myHome.Collean.models.Message;
import com.myHome.Collean.models.MulticastingMessage;
import com.myHome.Collean.models.TicketingMessage;
import com.myHome.Collean.services.SocketService;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SocketServiceImpl implements SocketService {

    private Map<String, WebSocketSession> sessionMap;
    private Map<String,String> services;
    private Map<String,String> users;

    public SocketServiceImpl(){
        this.sessionMap = new HashMap<>();
        this.services = new HashMap<>();
        this.users = new HashMap<>();
    }

    @Override
    public void sendFailedMessage(Message message, String cause) {
        String senderId = null;
        if(message.getType().equals(MessageType.BROADCAST))
            senderId = new Gson().fromJson(message.getMessage(), BroadcastingMessage.class).getSenderId();
        if(message.getType().equals(MessageType.MULTICAST))
            senderId = new Gson().fromJson(message.getMessage(), MulticastingMessage.class).getSenderId();
        if(message.getType().equals(MessageType.TICKET))
            senderId = new Gson().fromJson(message.getMessage(), TicketingMessage.class).getSenderId();
        Optional.ofNullable(users.get(senderId)).ifPresent(sessionId->{
            Optional.ofNullable(sessionMap.get(sessionId)).ifPresent(webSocketSession -> {
                try {
                    webSocketSession.sendMessage(new TextMessage("FAILED<$$$>".concat(new Gson().toJson(message))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    @Override
    public void broadcastNow(BroadcastingMessage broadcastingMessage) {
        users.keySet().forEach(userId->{
            Optional.ofNullable(sessionMap.get(userId)).ifPresent(webSocketSession -> {
                try {
                    webSocketSession.sendMessage(new TextMessage(new Gson().toJson(broadcastingMessage)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
        services.keySet().forEach(serviceId->{
            Optional.ofNullable(sessionMap.get(serviceId)).ifPresent(webSocketSession -> {
                try {
                    webSocketSession.sendMessage(new TextMessage(new Gson().toJson(broadcastingMessage)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    @Override
    public void multicast(MulticastingMessage multicastingMessage) {
        multicastingMessage.getReceiverIds().forEach(receiverId->{
            Optional.ofNullable(users.get(receiverId)).ifPresent(socketId->{
               Optional.ofNullable(sessionMap.get(socketId)).ifPresent(webSocketSession -> {
                   try {
                       webSocketSession.sendMessage(new TextMessage(new Gson().toJson(multicastingMessage)));
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               });
            });
        });
    }

    @Override
    public void cutTicket(TicketingMessage ticketingMessage) {
        services.keySet().forEach(serviceId->{
            Optional.ofNullable(sessionMap.get(serviceId)).ifPresent(webSocketSession -> {
                try {
                    webSocketSession.sendMessage(new TextMessage(new Gson().toJson(ticketingMessage)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    @Override
    public Map<String, WebSocketSession> getSessionMap() {
        return sessionMap;
    }

    @Override
    public Map<String, String> getServicesToSessionIdMap() {
        return services;
    }

    @Override
    public Map<String, String> getusersToSessionIdMap() {
        return users;
    }

}
