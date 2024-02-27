package settleup.backend.domain.user.service.impl;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.repository.UserRepository;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class SearchServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SearchServiceImpl searchService;

    @Test
    public void testGetUserList() {
        // Given
        String partOfEmail = "seo";
        UserEntity userEntityOne = new UserEntity();
        userEntityOne.setId(1L);
        userEntityOne.setUserUUID("uuid1");
        userEntityOne.setUserName("User One");
        userEntityOne.setUserPhone("1234567890");
        userEntityOne.setUserEmail("seo@example.com");

        UserEntity userEntityTwo = new UserEntity();
        userEntityTwo.setId(2L);
        userEntityTwo.setUserUUID("uuid2");
        userEntityTwo.setUserName("User Two");
        userEntityTwo.setUserPhone("0987654321");
        userEntityTwo.setUserEmail("seo2@example.com");

        List<UserEntity> mockUserEntities = Arrays.asList(userEntityOne, userEntityTwo);

        when(userRepository.findByUserEmailContaining(partOfEmail)).thenReturn(mockUserEntities);

        // When
        List<UserInfoDto> result = searchService.getUserList(partOfEmail);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("seo@example.com", result.get(0).getUserEmail());
        assertEquals("uuid1", result.get(0).getUserUUID());
        assertEquals("seo2@example.com", result.get(1).getUserEmail());
        assertEquals("uuid2", result.get(1).getUserUUID());

        // Verify interaction
        verify(userRepository, times(1)).findByUserEmailContaining(partOfEmail);
    }
}