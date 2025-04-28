package com.example.demo.controller.LineMessage;

import java.util.List;

import com.example.demo.domain.User;
import com.example.demo.service.UserService;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.*;
import com.linecorp.bot.messaging.model.ReplyMessageRequest;
import com.linecorp.bot.webhook.model.MessageEvent;

public class ListUsersHandler implements CommandHandler{

    private final MessagingApiClient messagingApiClient;
    private final UserService userService;

    public ListUsersHandler(MessagingApiClient messagingApiClient, UserService userService) {
        this.messagingApiClient = messagingApiClient;
        this.userService = userService;
    }
    
    @Override
    public void handle(MessageEvent event) {
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) {
            messagingApiClient.replyMessage(new ReplyMessageRequest(
                    event.replyToken(), List.of(new TextMessage("Không có user nào")), false));
            return;
        }

        StringBuilder message = new StringBuilder("Danh sách Users:\n\n");
        users.stream().limit(5).forEach(user ->
            message.append("- ").append(user.getName()).append(" (").append(user.getEmail()).append(")\n")
        );

        messagingApiClient.replyMessage(new ReplyMessageRequest(
                event.replyToken(), List.of(new TextMessage(message.toString())), false));
    }
    
}
