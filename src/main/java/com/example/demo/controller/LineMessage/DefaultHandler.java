package com.example.demo.controller.LineMessage;

import java.util.List;

import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.*;
import com.linecorp.bot.messaging.model.ReplyMessageRequest;
import com.linecorp.bot.webhook.model.MessageEvent;

public class DefaultHandler implements CommandHandler{

    private final MessagingApiClient messagingApiClient;

    public DefaultHandler (MessagingApiClient messagingApiClient) {
        this.messagingApiClient = messagingApiClient;
    }

    @Override
    public void handle(MessageEvent event) {
        messagingApiClient.replyMessage(new ReplyMessageRequest(
                event.replyToken(), List.of(new TextMessage("Xin lỗi, tôi không hiểu câu lệnh của bạn!")), false));
    }
    
}
