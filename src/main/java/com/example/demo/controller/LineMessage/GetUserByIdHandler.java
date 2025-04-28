package com.example.demo.controller.LineMessage;

import java.util.List;

import com.example.demo.domain.User;
import com.example.demo.service.UserService;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.*;
import com.linecorp.bot.webhook.model.MessageEvent;
import com.linecorp.bot.webhook.model.TextMessageContent;

public class GetUserByIdHandler implements CommandHandler {

    private final MessagingApiClient messagingApiClient;
    private final UserService userService;

    public GetUserByIdHandler(MessagingApiClient messagingApiClient, UserService userService) {
        this.messagingApiClient = messagingApiClient;
        this.userService = userService;
    }

    @Override
    public void handle(MessageEvent event) {
        if (!(event.message() instanceof TextMessageContent messageContent)) {
            return;
        }

        String userMessage = messageContent.text().trim().toLowerCase();

        try {
            // Parse id từ text message
            long userId = extractUserId(userMessage);

            User user = userService.handleGetUserById(userId);
            if (user == null) {
                messagingApiClient.replyMessage(new ReplyMessageRequest(
                        event.replyToken(), List.of(new TextMessage("Không tìm thấy user ID = " + userId)), false));
                return;
            }

            String info = String.format("ID: %d\nTên: %s\nEmail: %s\nPhone: %s",
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPhone() != null ? user.getPhone() : "N/A");

            messagingApiClient.replyMessage(new ReplyMessageRequest(
                    event.replyToken(), List.of(new TextMessage(info)), false));

        } catch (Exception e) {
            messagingApiClient.replyMessage(new ReplyMessageRequest(
                    event.replyToken(), List.of(new TextMessage("Sai cú pháp. Hãy gửi dạng: user id [số]")), false));
        }
    }

    private long extractUserId(String text) {
        // Ví dụ user gửi: "user id 15"
        if (!text.startsWith("user id")) {
            throw new IllegalArgumentException("Invalid format. Hãy gửi dạng: user id [số]");
        }
        String[] parts = text.split(" ");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid format");
        }
        return Long.parseLong(parts[2]);
    }
}
