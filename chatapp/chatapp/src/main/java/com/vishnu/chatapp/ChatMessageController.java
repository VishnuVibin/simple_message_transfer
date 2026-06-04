package com.vishnu.chatapp;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatMessageController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessageController(
            SimpMessagingTemplate messagingTemplate) {

        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/send")
    public void sendMessage(
            ChatMessage message) {

        if (message.getType() != null
                && message.getType().equals("PRIVATE")) {

            if (message.getRecipient() != null
                    && !message.getRecipient().isBlank()) {

                messagingTemplate.convertAndSend(
                        "/topic/private-" + message.getRecipient(),
                        message
                );
            }

            messagingTemplate.convertAndSend(
                    "/topic/private-" + message.getSender(),
                    message
            );

            return;
        }

        messagingTemplate.convertAndSend(
                "/topic/messages",
                message
        );
    }

    @MessageMapping("/join")
    public void join(ChatMessage message) {

        if (message.getSender() == null
                || message.getSender().isBlank()) {

            return;
        }

        UserStorage.users.add(
                message.getSender()
        );

        messagingTemplate.convertAndSend(
                "/topic/users",
                String.join(
                        ",",
                        UserStorage.users
                )
        );
    }
}