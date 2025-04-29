package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import com.example.demo.controller.LineMessage.*;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import org.springframework.web.bind.annotation.*;

import com.example.demo.service.UserService;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.MessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;

import lombok.extern.slf4j.Slf4j;

@LineMessageHandler
@RestController
@RequestMapping("/api/v1/line-message")
@Slf4j
public class LineMessagingController {

    private final LineMessagingClient lineMessagingClient;
    private final UserService userService;
    private final Map<String, CommandHandler<TextMessageContent>> commandHandlers;
    private final CommandHandler<TextMessageContent> defaultHandler;

    public LineMessagingController(LineMessagingClient lineMessagingClient, UserService userService) {
        this.lineMessagingClient = lineMessagingClient;
        this.userService = userService;

        this.defaultHandler = new DefaultHandler(lineMessagingClient);

        this.commandHandlers = initializeCommandHandlers();
    }

    // @LineMessageHandler: tự động quét các handler có @EventMapping để xử lý sự
    // kiện từ LINE webhook.
    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
        TextMessageContent messageContent = event.getMessage();
        String userMessage = messageContent.getText().trim().toLowerCase();
        log.info("Received message: {}", userMessage);

        // Nếu userMessage bắt đầu bằng "user id" thì dùng GetUserByIdHandler
        if (userMessage.startsWith("user id")) {
            new GetUserByIdHandler(lineMessagingClient, userService).handle(event);
            return;
        }

        CommandHandler<TextMessageContent> handler = commandHandlers.getOrDefault(userMessage, defaultHandler);
        handler.handle(event);
    }

    private Map<String, CommandHandler<TextMessageContent>> initializeCommandHandlers() {
        Map<String, CommandHandler<TextMessageContent>> handlers = new HashMap<>();

        handlers.put("sticker", new StickerHandler(lineMessagingClient));
        handlers.put("happy", new HappyHandler(lineMessagingClient));

        return handlers;
    }

    // Luồng hoạt động:
    // Người dùng quét QR Code sau đó gửi tin nhắn qua ứng dụng LINE
    // LINE chuyển tiếp tin nhắn đó tới Webhook URL đã đăng ký của Bot
    // Webhook URL thông qua ngrok gửi request đến server của Spring Boot
    // Server xử lý và gửi phản hồi lại LINE Message API
    // LINE gửi tin nhắn phản hồi tới người dùng

}
