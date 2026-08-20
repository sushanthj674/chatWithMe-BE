package com.chatwithme.backend.message;

import com.chatwithme.backend.message.dto.MessageResponse;
import com.chatwithme.backend.message.dto.SendMessageRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse send(@Valid @RequestBody SendMessageRequest request) {
        return messageService.sendMessage(request);
    }

    @GetMapping
    public List<MessageResponse> list(
            @RequestParam(required = false) String since,
            @RequestParam(defaultValue = "50") int limit
    ) {
        Instant sinceInstant = parseSince(since);
        return messageService.getMessages(sinceInstant, limit);
    }

    private Instant parseSince(String since) {
        if (since == null || since.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(since);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid 'since' timestamp, expected ISO-8601: " + since);
        }
    }
}
