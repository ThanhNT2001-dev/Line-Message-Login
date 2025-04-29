package com.example.demo.controller.LineMessage;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.StickerMessage;

public class StickerHandler implements CommandHandler<TextMessageContent>{

    private final LineMessagingClient lineMessagingClient;

    public StickerHandler (LineMessagingClient lineMessagingClient) {
        this.lineMessagingClient = lineMessagingClient;
    }

    @Override
    public void handle(MessageEvent<TextMessageContent> event) {
        String replyToken = event.getReplyToken();

        StickerMessage sticker = new StickerMessage("8515", "16581242");

        ReplyMessage replyRequest = new ReplyMessage(
                replyToken,
                Collections.singletonList(sticker)
        );

        try {
            lineMessagingClient.replyMessage(replyRequest).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
    
}
