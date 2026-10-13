package in.kanchuk.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import in.kanchuk.entity.DeviceToken;
import in.kanchuk.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final DeviceTokenRepository deviceTokenRepository;

    public void sendToUser(String userId, String title, String body) {
        if (userId == null || userId.isBlank()) return;
        if (FirebaseApp.getApps().isEmpty()) return; // Firebase not initialised (no service account)

        List<DeviceToken> tokens = deviceTokenRepository.findByUserId(userId);
        for (DeviceToken dt : tokens) {
            try {
                Message message = Message.builder()
                        .setToken(dt.getToken())
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .build();
                FirebaseMessaging.getInstance().send(message);
            } catch (FirebaseMessagingException e) {
                // Stale or invalid token — remove it so we don't retry
                deviceTokenRepository.delete(dt);
            }
        }
    }
}
