package settleup.backend.domain.user.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import settleup.backend.domain.user.common.ApiCallHelper;
import settleup.backend.domain.user.common.UUID_Helper;
import settleup.backend.domain.user.entity.dto.TokenDto;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.global.Util.JwtProvider;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class KaKaoServiceImplTest {

    @InjectMocks
    private KaKaoServiceImpl kaKaoService;
    @Mock
    private UserRepository userRepo;
    @Mock
    private JwtProvider tokenProvider;
    @Mock
    private UUID_Helper uuidHelper;


    @Test
    void registerUser()  {

        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setUserEmail("test@example.com");
        userInfoDto.setUserName("Test User");
        userInfoDto.setUserPhone("010-1234-5678");


        when(uuidHelper.UUIDFromEmail(anyString())).thenReturn("mockUUID");


        when(userRepo.findByUserEmail(anyString())).thenReturn(Optional.empty());


        TokenDto mockTokenDto = new TokenDto();
        mockTokenDto.setAccessToken("mockAccessToken");
        when(tokenProvider.createToken(any(UserInfoDto.class))).thenReturn("mockAccessToken");


        TokenDto result = kaKaoService.registerUser(userInfoDto);


        assertNotNull(result);
        assertEquals("mockAccessToken", result.getAccessToken());
    }
}
