package com.example.demo.controller.LineMessage;

import com.linecorp.bot.webhook.model.MessageEvent;

public interface CommandHandler {
    void handle(MessageEvent event);
}
