package com.example.demo.controller.LineMessage;

import java.util.List;

import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.*;
import com.linecorp.bot.messaging.model.ReplyMessageRequest;
import com.linecorp.bot.webhook.model.MessageEvent;

public class HappyHandler implements CommandHandler {

    private final MessagingApiClient messagingApiClient;

    public HappyHandler (MessagingApiClient messagingApiClient) {
        this.messagingApiClient = messagingApiClient;
    }

    @Override
    public void handle(MessageEvent event) {
        StickerMessage sticker = new StickerMessage("1070", "17841");
        messagingApiClient.replyMessage(new ReplyMessageRequest(
                event.replyToken(), List.of(sticker), false));
    }

}
