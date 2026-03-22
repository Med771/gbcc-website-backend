package backend.website.gbcc.logic.auth;

import backend.website.gbcc.logic.auth.dto.AuthLoginRequestDto;
import backend.website.gbcc.logic.auth.dto.AuthSessionDto;

public interface AuthService {
    AuthSessionDto login(AuthLoginRequestDto requestDto);

    AuthSessionDto refresh(String refreshToken);

    void logout(String refreshToken);
}
