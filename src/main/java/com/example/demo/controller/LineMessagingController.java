package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import com.example.demo.controller.LineMessage.CommandHandler;
import com.example.demo.controller.LineMessage.DefaultHandler;
import com.example.demo.controller.LineMessage.GetUserByIdHandler;
import com.example.demo.controller.LineMessage.HappyHandler;
import com.example.demo.controller.LineMessage.ListUsersHandler;
import com.example.demo.controller.LineMessage.StickerHandler;
import com.example.demo.domain.User;
import com.example.demo.service.UserService;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.*;
import com.linecorp.bot.model.PushMessage;
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

    private final MessagingApiClient messagingApiClient;
    private final UserService userService;
    private final Map<String, CommandHandler> commandHandlers;
    private final CommandHandler defaultHandler;
    public LineMessagingController(MessagingApiClient messagingApiClient, UserService userService) {
        this.messagingApiClient = messagingApiClient;
        this.userService = userService;

        this.defaultHandler = new DefaultHandler(messagingApiClient);

        this.commandHandlers = initializeCommandHandlers();
    }

    // @LineMessageHandler: tự động quét các handler có @EventMapping để xử lý sự kiện từ LINE webhook.
    @EventMapping
    public void handleTextMessageEvent(MessageEvent event) {
        if (event.message() instanceof TextMessageContent messageContent) {
            String userMessage = messageContent.text().trim().toLowerCase();
            log.info("Received message: {}", userMessage);

            // Nếu userMessage bắt đầu bằng "user id" thì dùng GetUserByIdHandler
        if (userMessage.startsWith("user id")) {
            new GetUserByIdHandler(messagingApiClient, userService).handle(event);
            return;
        }

            CommandHandler handler = commandHandlers.getOrDefault(userMessage, defaultHandler);
            handler.handle(event);
        }
    }

    private Map<String, CommandHandler> initializeCommandHandlers() {
        Map<String, CommandHandler> handlers = new HashMap<>();
    
        handlers.put("list users", new ListUsersHandler(messagingApiClient, userService));
        handlers.put("sticker", new StickerHandler(messagingApiClient));
        handlers.put("happy", new HappyHandler(messagingApiClient));
    
        return handlers;
    }

    // Luồng hoạt động:
    // Người dùng quét QR Code sau đó gửi tin nhắn qua ứng dụng LINE
    // LINE chuyển tiếp tin nhắn đó tới Webhook URL đã đăng ký của Bot
    // Webhook URL thông qua ngrok gửi request đến server của Spring Boot
    // Server xử lý và gửi phản hồi lại LINE Message API
    // LINE gửi tin nhắn phản hồi tới người dùng

}
