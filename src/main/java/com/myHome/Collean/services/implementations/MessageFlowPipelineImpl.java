package com.myHome.Collean.services.implementations;

import com.myHome.Collean.models.Message;
import com.myHome.Collean.services.FlowNode;
import com.myHome.Collean.services.MessageFlowPipeline;
import com.myHome.Collean.services.implementations.filtrationNode.FiltrationFlowNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class MessageFlowPipelineImpl implements MessageFlowPipeline {
    private FlowNode chainHead,chainTail;
    @Autowired
    public MessageFlowPipelineImpl(
            FiltrationFlowNode filtrationFlowNode,
            BroadcastFlowNode broadcastFlowNode,
            TicketFlowNode ticketFlowNode,
            MulticastFlowNode multicastFlowNode,
            DataSavingNode dataSavingNode
    ){
        addFlowNode(filtrationFlowNode);
        addFlowNode(ticketFlowNode);
        addFlowNode(broadcastFlowNode);
        addFlowNode(multicastFlowNode);
        addFlowNode(dataSavingNode);
    }

    private void addFlowNode(FlowNode flowNode) {
        if(Objects.isNull(chainHead)){
            chainHead = chainTail = flowNode;
            return;
        }
        chainTail.setNext(flowNode);
        chainTail=flowNode;
    }

    @Override
    public void pourMessage(Message message) {
        chainHead.handle(message);
    }
}
