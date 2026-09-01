package com.fulfillx.orders.messaging;

import com.fulfillx.orders.domain.OutboxEvent;
import com.fulfillx.orders.persistence.OutboxEventRepository;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private final OutboxEventRepository outbox;
    private final KafkaTemplate<String, String> kafka;

    public OutboxPublisher(OutboxEventRepository outbox, KafkaTemplate<String, String> kafka) {
        this.outbox = outbox;
        this.kafka = kafka;
    }

    @Scheduled(fixedDelayString = "${fulfillx.outbox.delay-ms:1000}")
    @Transactional
    public void publishPending() {
        for (OutboxEvent event : outbox.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc()) {
            try {
                kafka.send(event.getTopic(), event.getAggregateId(), event.getPayload())
                        .get(Duration.ofSeconds(5).toMillis(), TimeUnit.MILLISECONDS);
                event.markPublished();
                log.info("Published outbox event {} for aggregate {}", event.getId(), event.getAggregateId());
            } catch (Exception exception) {
                log.warn("Outbox event {} remains pending: {}", event.getId(), exception.getMessage());
                break;
            }
        }
    }
}

