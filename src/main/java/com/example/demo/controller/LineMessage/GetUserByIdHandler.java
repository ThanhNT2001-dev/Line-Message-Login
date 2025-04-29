package com.example.demo.controller.LineMessage;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.example.demo.domain.User;
import com.example.demo.service.UserService;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;

import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.Separator;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.unit.FlexAlign;
import com.linecorp.bot.model.message.flex.unit.FlexFontSize;
import com.linecorp.bot.model.message.flex.unit.FlexLayout;
import com.linecorp.bot.model.message.flex.unit.FlexMarginSize;


public class GetUserByIdHandler implements CommandHandler<TextMessageContent> {

    private final LineMessagingClient lineMessagingClient;
    private final UserService userService;

    public GetUserByIdHandler(LineMessagingClient lineMessagingClient, UserService userService) {
        this.lineMessagingClient = lineMessagingClient;
        this.userService = userService;
    }

    @Override
    public void handle(MessageEvent<TextMessageContent> event) {
        if (!(event.getMessage() instanceof TextMessageContent messageContent)) {
            return;
        }

        String userMessage = messageContent.getText().trim().toLowerCase();

        try {
            // Parse id từ text message
            long userId = extractUserId(userMessage);

            User user = userService.handleGetUserById(userId);
            if (user == null) {
                replyText(event.getReplyToken(), "Không tìm thấy user ID = " + userId);
                return;
            }

            // Tạo Flex Message
            Bubble userProfileFlex = createUserProfileFlex(user);

            FlexMessage flexMessage = new FlexMessage("Thông tin người dùng", userProfileFlex);

            // Gửi Flex Message
            replyFlex(event.getReplyToken(), flexMessage);

        } catch (Exception e) {
            replyText(event.getReplyToken(), "Sai cú pháp. Hãy gửi dạng: user id [số]");
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

    // 🧩 Hàm tạo Flex Message Profile
    private Bubble createUserProfileFlex(User user) {
        return Bubble.builder()
                .body(Box.builder()
                        .layout(FlexLayout.VERTICAL)
                        .contents(List.of(
                                Text.builder()
                                        .text("THÔNG TIN NGƯỜI DÙNG")
                                        .weight(Text.TextWeight.BOLD)
                                        .size(FlexFontSize.XL)
                                        .align(FlexAlign.CENTER)
                                        .build(),
                                Separator.builder().margin(FlexMarginSize.MD).build(),
                                Text.builder()
                                        .text("Tên: " + user.getName())
                                        .size(FlexFontSize.Md)
                                        .margin(FlexMarginSize.MD)
                                        .wrap(true)
                                        .build(),
                                Text.builder()
                                        .text("Email: " + (user.getEmail() != null ? user.getEmail() : "N/A"))
                                        .size(FlexFontSize.SM)
                                        .margin(FlexMarginSize.SM)
                                        .wrap(true)
                                        .build(),
                                Text.builder()
                                        .text("Điện thoại: " + (user.getPhone() != null ? user.getPhone() : "N/A"))
                                        .size(FlexFontSize.SM)
                                        .margin(FlexMarginSize.SM)
                                        .wrap(true)
                                        .build()))
                        .build())
                .build();
    }

    private void replyText(String replyToken, String message) {
        try {
            lineMessagingClient.replyMessage(
                    new ReplyMessage(replyToken, Collections.singletonList(new TextMessage(message)))
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void replyFlex(String replyToken, FlexMessage flexMessage) {
        try {
            lineMessagingClient.replyMessage(
                    new ReplyMessage(replyToken, Collections.singletonList(flexMessage))
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
