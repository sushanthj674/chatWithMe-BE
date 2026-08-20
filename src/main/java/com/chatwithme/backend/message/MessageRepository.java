package com.chatwithme.backend.message;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByCreatedAtAfterOrderByCreatedAtAsc(Instant since, Pageable pageable);

    List<Message> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
