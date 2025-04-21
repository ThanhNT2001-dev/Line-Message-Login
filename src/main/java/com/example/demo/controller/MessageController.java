package com.example.demo.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.demo.domain.User;
import com.example.demo.service.UserService;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.ReplyMessageRequest;
import com.linecorp.bot.messaging.model.TextMessage;
import com.linecorp.bot.spring.boot.handler.annotation.EventMapping;
import com.linecorp.bot.spring.boot.handler.annotation.LineMessageHandler;
import com.linecorp.bot.webhook.model.Event;
import com.linecorp.bot.webhook.model.MessageEvent;
import com.linecorp.bot.webhook.model.TextMessageContent;

@LineMessageHandler
public class MessageController {
    private final Logger log = LoggerFactory.getLogger(MessageController.class);
    private final MessagingApiClient messagingApiClient;
    private final UserService userService;

    public MessageController(MessagingApiClient messagingApiClient, UserService userService) {
        this.messagingApiClient = messagingApiClient;
        this.userService = userService;
    }

    @EventMapping
    public void handleTextMessageEvent(MessageEvent event) {
        log.info("event: " + event);
        if (event.message() instanceof TextMessageContent) {
            TextMessageContent message = (TextMessageContent) event.message();
            String userMessage = message.text();

            // Xử lý các lệnh đặc biệt
            if (userMessage.trim().equalsIgnoreCase("Kiểm tra danh sách User")) {
                handleGetUsersList(event);
            } else {
                // Phản hồi thông thường
                final String originalMessageText = " Hello Iam Thanh? " + message.text();
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

            // Giới hạn số lượng user hiển thị để không vượt quá giới hạn ký tự của LINE
            int maxUsers = Math.min(5, users.size());

            // Tạo nội dung tin nhắn
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("Danh sách User (").append(users.size()).append(" người dùng):\n\n");

            for (int i = 0; i < maxUsers; i++) {
                User user = users.get(i);
                messageBuilder.append(i + 1).append(". ");
                messageBuilder.append("ID: ").append(user.getId()).append("\n");
                messageBuilder.append("   Tên: ").append(user.getName()).append("\n");
                messageBuilder.append("   Email: ").append(user.getEmail()).append("\n");
                messageBuilder.append("   Điện thoại: ").append(user.getPhone() != null ? user.getPhone() : "N/A")
                        .append("\n\n");
            }

            // Nếu có nhiều user hơn giới hạn hiển thị
            if (users.size() > maxUsers) {
                messageBuilder.append("... và ").append(users.size() - maxUsers).append(" người dùng khác.");
            }

            // Gửi tin nhắn phản hồi
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

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        System.out.println("event: " + event);
    }

    // Luồng hoạt động:
    // Người dùng quét QR Code sau đó gửi tin nhắn qua ứng dụng LINE
    // LINE chuyển tiếp tin nhắn đó tới Webhook URL đã đăng ký của Bot
    // Webhook URL thông qua ngrok gửi request đến server của Spring Boot
    // Server xử lý và gửi phản hồi lại LINE Message API
    // LINE gửi tin nhắn phản hồi tới người dùng

}
