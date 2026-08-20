package com.chatwithme.backend.message;

import com.chatwithme.backend.message.dto.MessageResponse;
import com.chatwithme.backend.message.dto.SendMessageRequest;
import com.chatwithme.backend.push.FcmPushService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final FcmPushService fcmPushService;

    public MessageService(MessageRepository messageRepository, FcmPushService fcmPushService) {
        this.messageRepository = messageRepository;
        this.fcmPushService = fcmPushService;
    }

    public MessageResponse sendMessage(SendMessageRequest request) {
        Message message = new Message(request.deviceId(), request.senderName(), request.text(), Instant.now());
        message = messageRepository.save(message);

        fcmPushService.broadcastMessage(
                message.getId(),
                message.getDeviceId(),
                message.getSenderName(),
                message.getText(),
                message.getCreatedAt()
        );

        return toResponse(message);
    }

    public void deleteAllMessages() {
        messageRepository.deleteAll();
    }

    public List<MessageResponse> getMessages(Instant since, int limit) {
        List<Message> messages;
        if (since != null) {
            messages = messageRepository.findByCreatedAtAfterOrderByCreatedAtAsc(since, PageRequest.of(0, limit));
        } else {
            messages = messageRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
            Collections.reverse(messages);
        }
        return messages.stream().map(this::toResponse).toList();
    }

    private MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getDeviceId(),
                message.getSenderName(),
                message.getText(),
                message.getCreatedAt()
        );
    }
}
