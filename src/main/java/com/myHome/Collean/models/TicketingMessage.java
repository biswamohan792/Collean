package com.myHome.Collean.models;


import com.myHome.Collean.constants.enums.TicketSeverity;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Timestamp;

@Builder
@Data
@Document(collection = "ticket_messages")
public class TicketingMessage {
    private TicketSeverity severity;
    private String senderId;
    @Id
    private String messageId;
    private Timestamp creation;
    private String message;
}
