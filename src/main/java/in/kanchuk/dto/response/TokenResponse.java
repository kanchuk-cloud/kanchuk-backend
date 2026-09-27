package in.kanchuk.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponse {
    private String token;
    private String role;
    private String email;
    private String name;
    private String phone;
    private String userId;

    public TokenResponse(String token, String role, String email, String name) {
        this(token, role, email, name, null, null);
    }

    public TokenResponse(String token, String role, String email, String name, String phone) {
        this(token, role, email, name, phone, null);
    }
}
