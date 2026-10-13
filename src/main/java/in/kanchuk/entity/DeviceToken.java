package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "device_tokens")
@Getter
@Setter
public class DeviceToken extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String token;

    @Column(length = 20)
    private String platform;
}
