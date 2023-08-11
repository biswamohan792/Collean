package com.myHome.Collean.services.implementations.filtrationNode;

import com.myHome.Collean.constants.FiltrationNodeConstants;
import com.myHome.Collean.models.Message;
import com.myHome.Collean.services.FlowNode;
import com.myHome.Collean.services.SocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class FiltrationFlowNode implements FlowNode {
    private SocketService socketService;
    private TextClassifier textClassifier;
    private KafkaTemplate<String,Message> kafkaTemplate;
    private FlowNode next;
    private ExecutorService executorService;

    @Autowired
    public FiltrationFlowNode(
            SocketService socketService,
            TextClassifier textClassifier,
            KafkaTemplate<String,Message> kafkaTemplate
    ){
        this.socketService = socketService;
        this.textClassifier = textClassifier;
        this.kafkaTemplate = kafkaTemplate;
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @KafkaListener(topics = FiltrationNodeConstants.KAFKA_TOPIC,groupId = "filtration-flow-node")
    private void consumeMessage(Message message){
        executorService.submit(()->{
            if(textClassifier.isGoodComment(message.getMessage())) next.handle(message);
            else socketService.sendFailedMessage(message, FiltrationNodeConstants.REASON_BAD_WORDS);
        });
    }

    @Override
    public void handle(Message message) {
        this.kafkaTemplate.send(FiltrationNodeConstants.KAFKA_TOPIC,message);
    }

    @Override
    public void setNext(FlowNode next) {
        this.next=next;
    }
}
