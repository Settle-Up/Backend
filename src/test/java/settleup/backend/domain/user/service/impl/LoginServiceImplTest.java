package settleup.backend.domain.user.service.impl;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.exception.CustomException;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.global.Util.JwtProvider;

import java.util.Optional;

public class LoginServiceImplTest {

    @Mock
    private JwtProvider provider;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private LoginServiceImpl loginService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void validTokenOrNot_ValidToken_ReturnsUserInfoDto() throws CustomException {

        String token = "valid.token";
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("ForSettleUpLogin");
        when(claims.get("userUUID", String.class)).thenReturn("userUUID123");
        when(claims.get("userName", String.class)).thenReturn("John Doe");

        when(provider.parseJwtToken(token)).thenReturn(claims);

        UserEntity userEntity = new UserEntity();
        when(userRepo.findByUserUUID("userUUID123")).thenReturn(Optional.of(userEntity));


        UserInfoDto result = loginService.validTokenOrNot(token);


        assertNotNull(result);
        assertEquals("userUUID123", result.getUserUUID());
        assertEquals("John Doe", result.getUserName());
    }

    @Test
    public void validTokenOrNot_WrongSubject_ThrowsCustomException() {

        String token = "invalid.subject.token";
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("WrongSubject");

        when(provider.parseJwtToken(token)).thenReturn(claims);


        assertThrows(CustomException.class, () -> loginService.validTokenOrNot(token));
    }

    @Test
    public void validTokenOrNot_UserNotFound_ThrowsCustomException() {

        String token = "user.not.found.token";
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("ForSettleUpLogin");
        when(claims.get("userUUID", String.class)).thenReturn("nonExistentUserUUID");

        when(provider.parseJwtToken(token)).thenReturn(claims);
        when(userRepo.findByUserUUID("nonExistentUserUUID")).thenReturn(Optional.empty());


        assertThrows(CustomException.class, () -> loginService.validTokenOrNot(token));
    }
}
