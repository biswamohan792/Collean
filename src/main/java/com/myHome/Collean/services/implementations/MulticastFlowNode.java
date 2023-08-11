package com.myHome.Collean.services.implementations;

import com.google.gson.Gson;
import com.myHome.Collean.constants.MulticastNodeConstants;
import com.myHome.Collean.constants.enums.MessageType;
import com.myHome.Collean.models.Message;
import com.myHome.Collean.models.MulticastingMessage;
import com.myHome.Collean.services.FlowNode;
import com.myHome.Collean.services.SocketService;
import com.myHome.Collean.utils.MessageTypeConverters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MulticastFlowNode implements FlowNode {
    private KafkaTemplate<String,Message> kafkaTemplate;
    private FlowNode next;
    private ExecutorService executorService;
    private SocketService socketService;

    @Autowired
    public MulticastFlowNode(
            KafkaTemplate<String,Message> kafkaTemplate,
            SocketService socketService
    ){
        this.kafkaTemplate = kafkaTemplate;
        this.socketService = socketService;
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @KafkaListener(topics = MulticastNodeConstants.KAFKA_TOPIC,groupId = "multicast-flow-node")
    private void consumeMessage(Message message){
        if(message.getType().equals(MessageType.MULTICAST)) {
            executorService.submit(() -> {
                try {
                    socketService.multicast(MessageTypeConverters.convertToMulticastMessage(message));
                    this.next.handle(message);
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public void handle(Message message) {
        this.kafkaTemplate.send(MulticastNodeConstants.KAFKA_TOPIC,message);
    }

    @Override
    public void setNext(FlowNode next) {
        this.next = next;
    }
}
