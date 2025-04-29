package com.example.demo.controller.LineMessage;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;

public class DefaultHandler implements CommandHandler<TextMessageContent>{

    private final LineMessagingClient lineMessagingClient;

    public DefaultHandler (LineMessagingClient lineMessagingClient) {
        this.lineMessagingClient = lineMessagingClient;
    }

    @Override
    public void handle(MessageEvent<TextMessageContent> event) {
        String replyToken = event.getReplyToken();

        TextMessage textMessage = new TextMessage("Xin lỗi, tôi không hiểu câu lệnh của bạn!");

        ReplyMessage replyRequest = new ReplyMessage(
                replyToken,
                Collections.singletonList(textMessage)
        );

        try {
            lineMessagingClient.replyMessage(replyRequest).get(); // blocking call, can also use thenAccept
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace(); // hoặc log.error nếu dùng SLF4J
        }
    }
    
}
