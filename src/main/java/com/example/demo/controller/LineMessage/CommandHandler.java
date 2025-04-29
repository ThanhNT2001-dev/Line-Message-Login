package com.example.demo.controller.LineMessage;

import org.apache.poi.ss.formula.functions.T;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;

public interface CommandHandler<T extends TextMessageContent> {
    void handle(MessageEvent<T> event);
}
