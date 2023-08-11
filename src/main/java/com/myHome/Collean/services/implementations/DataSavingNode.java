package com.myHome.Collean.services.implementations;

import com.myHome.Collean.constants.DataSavingNodeConstants;
import com.myHome.Collean.constants.enums.MessageType;
import com.myHome.Collean.models.Message;
import com.myHome.Collean.repositories.BroadcastingMessageRepository;
import com.myHome.Collean.repositories.MulticastingMessageRepository;
import com.myHome.Collean.repositories.TicketingMessageRepository;
import com.myHome.Collean.services.FlowNode;
import com.myHome.Collean.utils.MessageTypeConverters;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

public class DataSavingNode implements FlowNode {
    private ExecutorService executorService;
    private Semaphore semaphore;
    private BroadcastingMessageRepository broadcastingMessageRepository;
    private MulticastingMessageRepository multicastingMessageRepository;
    private TicketingMessageRepository ticketingMessageRepository;
    private Map<MessageType, Function<Message,Void>> messageTypeFunctionStrategyFactory;

    @Autowired
    public DataSavingNode(
            BroadcastingMessageRepository broadcastingMessageRepository,
            MulticastingMessageRepository multicastingMessageRepository,
            TicketingMessageRepository ticketingMessageRepository
    ){
        this.broadcastingMessageRepository = broadcastingMessageRepository;
        this.multicastingMessageRepository = multicastingMessageRepository;
        this.ticketingMessageRepository = ticketingMessageRepository;
        this.executorService = Executors.newFixedThreadPool(DataSavingNodeConstants.NO_OF_IO_THREADS);
        this.semaphore = new Semaphore(DataSavingNodeConstants.MAX_CONCURRENT_WRITES);
        this.messageTypeFunctionStrategyFactory = new HashMap<>();
        initMessageTypeStrategies();
    }

    private void initMessageTypeStrategies(){
        this.messageTypeFunctionStrategyFactory.put(MessageType.BROADCAST,(message) -> {
            var broadcastMessage = MessageTypeConverters.convertToBroadcastMessage(message);
            broadcastingMessageRepository.save(broadcastMessage);
            return null;
        });
        this.messageTypeFunctionStrategyFactory.put(MessageType.MULTICAST,(message) -> {
            var multicastMessage = MessageTypeConverters.convertToMulticastMessage(message);
            multicastingMessageRepository.save(multicastMessage);
            return null;
        });
        this.messageTypeFunctionStrategyFactory.put(MessageType.TICKET,(message) -> {
            var ticket = MessageTypeConverters.convertToTicketMessage(message);
            ticketingMessageRepository.save(ticket);
            return null;
        });
    }
    @Override
    public void handle(Message message) {
        executorService.submit(()->{
           try{
               semaphore.acquire();
               this.messageTypeFunctionStrategyFactory.get(message.getType())
                       .apply(message);
           }catch (Exception e){
               e.printStackTrace();
           }finally {
               semaphore.release();
           }
        });
    }

    @Override
    public void setNext(FlowNode next) {
        throw new RuntimeException("Final Tail Node!");
    }
}
