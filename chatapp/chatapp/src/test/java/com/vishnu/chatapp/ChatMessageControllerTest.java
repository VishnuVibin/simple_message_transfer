package com.vishnu.chatapp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ChatMessageControllerTest {

    @AfterEach
    void cleanup() {
        UserStorage.users.clear();
        UserStorage.admin = null;
    }

    @Test
    void deleteUserShouldNotifyDeletedUserAndRefreshOthers() {
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        ChatMessageController controller = new ChatMessageController(messagingTemplate);

        UserStorage.addUser("admin");
        UserStorage.addUser("alice");
        UserStorage.setAdmin("admin");

        AdminAction action = new AdminAction();
        action.setRequester("admin");
        action.setTargetUser("alice");

        controller.deleteUser(action);

        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        verify(messagingTemplate, atLeastOnce()).convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());

        assertTrue(destinationCaptor.getAllValues().contains("/queue/alice"));
        assertTrue(payloadCaptor.getAllValues().stream().anyMatch(payload -> payload.contains("\"action\":\"DELETE\"")));
    }
}
