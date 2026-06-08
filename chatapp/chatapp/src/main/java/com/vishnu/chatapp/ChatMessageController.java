package com.vishnu.chatapp;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Set;

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

        if (message.getType() == null) {
            return;
        }

        if ("PUBLIC".equals(message.getType())) {
            messagingTemplate.convertAndSend(
                    "/topic/public",
                    message
            );
        } else if ("PRIVATE".equals(message.getType())) {
            if (message.getRecipient() == null
                    || message.getRecipient().isBlank()) {
                return;
            }

            messagingTemplate.convertAndSendToUser(
                    message.getRecipient(),
                    "/queue/private",
                    message
            );

            messagingTemplate.convertAndSendToUser(
                    message.getSender(),
                    "/queue/private",
                    message
            );
        }
    }

    @MessageMapping("/join")
    public void join(ChatMessage message) {

        if (message.getSender() == null
                || message.getSender().isBlank()) {

            return;
        }

        UserStorage.addUser(message.getSender());
        broadcastUsers();
    }

    @MessageMapping("/admin/delete")
    public void deleteUser(AdminAction action) {

        if (!UserStorage.isAdmin(action.getRequester())
                || action.getTargetUser() == null
                || action.getTargetUser().isBlank()) {

            return;
        }

        String targetUser = action.getTargetUser().trim();

        UserStorage.removeUser(targetUser);
        notifyDeletedUser(targetUser);
        broadcastUsers();
    }

    @MessageMapping("/admin/promote")
    public void promoteUser(AdminAction action) {

        if (!UserStorage.isAdmin(action.getRequester())
                || action.getTargetUser() == null
                || action.getTargetUser().isBlank()) {

            return;
        }

        UserStorage.setAdmin(action.getTargetUser());
        broadcastUsers();
    }

    private void notifyDeletedUser(String targetUser) {

        String payload = "{\"action\":\"DELETE\",\"targetUser\":\"" + escapeJson(targetUser) + "\"}";

        messagingTemplate.convertAndSend(
                "/queue/" + targetUser,
                payload
        );
    }

    private void broadcastUsers() {

        String payload = UserStorage.toJson();

        for (String user : Set.copyOf(UserStorage.users)) {

            messagingTemplate.convertAndSend(
                    "/queue/" + user,
                    payload
            );
        }
    }

    private String escapeJson(String value) {

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}