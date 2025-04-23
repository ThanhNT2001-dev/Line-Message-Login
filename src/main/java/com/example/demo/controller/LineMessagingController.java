package com.example.demo.controller;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import com.example.demo.domain.User;
import com.example.demo.service.UserService;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.*;

import com.linecorp.bot.webhook.model.MessageEvent;
import com.linecorp.bot.webhook.model.TextMessageContent;

import lombok.extern.slf4j.Slf4j;

import com.linecorp.bot.spring.boot.handler.annotation.EventMapping;
import com.linecorp.bot.spring.boot.handler.annotation.LineMessageHandler;

@LineMessageHandler
@RestController
@RequestMapping("/api/v1/line-message")
@Slf4j
public class LineMessagingController {

    private final Logger log = LoggerFactory.getLogger(LineMessagingController.class);
    private final MessagingApiClient messagingApiClient;
    private final UserService userService;

    public LineMessagingController(MessagingApiClient messagingApiClient, UserService userService) {
        this.messagingApiClient = messagingApiClient;
        this.userService = userService;
    }

    // @LineMessageHandler: tự động quét các handler có @EventMapping để xử lý sự
    // kiện từ LINE webhook.
    @EventMapping
    public void handleTextMessageEvent(MessageEvent event) {
        log.info("event: {}", event);
        if (event.message() instanceof TextMessageContent messageContent) {
            String userMessage = messageContent.text().trim().toLowerCase();

            switch (userMessage) {
                case "list users":
                    handleGetUsersList(event);
                    break;
                case "users with id = 12":
                    handleGetUserById(event, 12);
                    break;
                default:
                    String originalMessageText = "Bạn đã gửi: " + userMessage;
                    messagingApiClient.replyMessage(new ReplyMessageRequest(
                            event.replyToken(),
                            List.of(new TextMessage(originalMessageText)),
                            false));
            }
        }
    }

    public void handleGetUsersList(MessageEvent event) {
        try {
            List<User> users = this.userService.getAllUsers();

            if (users.isEmpty()) {
                messagingApiClient.replyMessage(new ReplyMessageRequest(
                        event.replyToken(),
                        List.of(new TextMessage("Hiện tại không có người dùng nào trong hệ thống.")),
                        false));
                return;
            }

            int maxUsers = Math.min(5, users.size());
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("Danh sách User (").append(users.size()).append(" người dùng):\n\n");

            for (int i = 0; i < maxUsers; i++) {
                User user = users.get(i);
                messageBuilder.append(i + 1).append(". ")
                        .append("ID: ").append(user.getId()).append("\n")
                        .append("   Tên: ").append(user.getName()).append("\n")
                        .append("   Email: ").append(user.getEmail()).append("\n")
                        .append("   Điện thoại: ").append(user.getPhone() != null ? user.getPhone() : "N/A")
                        .append("\n\n");
            }

            if (users.size() > maxUsers) {
                messageBuilder.append("... và ").append(users.size() - maxUsers).append(" người dùng khác.");
            }

            messagingApiClient.replyMessage(new ReplyMessageRequest(
                    event.replyToken(),
                    List.of(new TextMessage(messageBuilder.toString())),
                    false));
        } catch (Exception e) {
            log.error("Lỗi khi truy vấn danh sách người dùng: ", e);
            messagingApiClient.replyMessage(new ReplyMessageRequest(
                    event.replyToken(),
                    List.of(new TextMessage("Đã xảy ra lỗi khi lấy danh sách người dùng. Vui lòng thử lại sau.")),
                    false));
        }
    }

    public void handleGetUserById(MessageEvent event, long id) {
        try {
            User user = this.userService.handleGetUserById(id);

            if (user == null) {
                messagingApiClient.replyMessage(new ReplyMessageRequest(
                        event.replyToken(),
                        List.of(new TextMessage("Không tìm thấy người dùng với ID = " + id)),
                        false));
                return;
            }

            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("Thông tin người dùng ID: ").append(user.getId()).append("\n")
                    .append("Tên: ").append(user.getName()).append("\n")
                    .append("Email: ").append(user.getEmail()).append("\n")
                    .append("Điện thoại: ").append(user.getPhone() != null ? user.getPhone() : "N/A");

            messagingApiClient.replyMessage(new ReplyMessageRequest(
                    event.replyToken(),
                    List.of(new TextMessage(messageBuilder.toString())),
                    false));
        } catch (Exception e) {
            log.error("Lỗi khi truy vấn người dùng ID = {}", id, e);
            messagingApiClient.replyMessage(new ReplyMessageRequest(
                    event.replyToken(),
                    List.of(new TextMessage("Đã xảy ra lỗi khi lấy thông tin người dùng. Vui lòng thử lại sau.")),
                    false));
        }
    }

}
