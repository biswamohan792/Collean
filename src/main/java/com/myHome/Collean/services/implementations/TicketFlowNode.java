package com.myHome.Collean.services.implementations;

import com.google.gson.Gson;
import com.myHome.Collean.constants.TicketNodeConstants;
import com.myHome.Collean.constants.enums.MessageType;
import com.myHome.Collean.models.Message;
import com.myHome.Collean.models.TicketingMessage;
import com.myHome.Collean.services.FlowNode;
import com.myHome.Collean.services.SocketService;
import com.myHome.Collean.utils.MessageTypeConverters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TicketFlowNode implements FlowNode {
    private FlowNode next;
    private KafkaTemplate<String,Message> kafkaTemplate;
    private SocketService socketService;
    private ExecutorService executorService;

    @Autowired
    public TicketFlowNode(
            KafkaTemplate<String,Message> kafkaTemplate,
            SocketService socketService
    ){
        this.kafkaTemplate = kafkaTemplate;
        this.socketService = socketService;
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @KafkaListener(topics = TicketNodeConstants.KAFKA_TOPIC,groupId = "ticket-flow-node")
    private void consumeMessage(Message message){
        if(message.getType().equals(MessageType.TICKET)) {
            executorService.submit(() -> {
                try {
                    socketService.cutTicket(MessageTypeConverters.convertToTicketMessage(message));
                    this.next.handle(message);
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public void handle(Message message) {
        this.kafkaTemplate.send(TicketNodeConstants.KAFKA_TOPIC,message);
    }

    @Override
    public void setNext(FlowNode next) {
        this.next = next;
    }
}
