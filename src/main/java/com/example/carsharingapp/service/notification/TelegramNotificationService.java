package com.example.carsharingapp.service.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TelegramNotificationService implements NotificationService{
    @Value("${TELEGRAM_BOT_TOKEN}")   private String botToken;
    @Value("${TELEGRAM_CHAT_ID}")    private String chatId;
    private final RestTemplate rest = new RestTemplate();
    @Override
    public void sendNotification(String message) {
        String uri = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        rest.postForObject(uri, Map.of(
                "chat_id", chatId,
                "text",    message
        ), String.class);
    }
}
