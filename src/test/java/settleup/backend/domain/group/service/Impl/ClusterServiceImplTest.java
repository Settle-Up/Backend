package settleup.backend.domain.group.service.Impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.entity.GroupUserEntity;
import settleup.backend.domain.group.entity.dto.CreateGroupRequestDto;
import settleup.backend.domain.group.entity.dto.CreateGroupResponseDto;
import settleup.backend.domain.group.repository.GroupRepository;
import settleup.backend.domain.group.repository.GroupUserRepository;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.global.Util.UrlProvider;
import settleup.backend.global.common.UUID_Helper;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ClusterServiceImplTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupUserRepository groupUserRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UUID_Helper uuidHelper;

    @Mock
    private UrlProvider urlProvider;

    @InjectMocks
    private ClusterServiceImpl clusterService;
    @BeforeEach
    void setUp() {
        lenient().when(uuidHelper.UUIDForGroup()).thenReturn("uniqueUUID");
        lenient().when(urlProvider.generateUniqueUrl()).thenReturn("uniqueUrl");

        UserEntity mockUserEntity1 = new UserEntity();
        mockUserEntity1.setUserUUID("user1UUID");
        mockUserEntity1.setUserName("Test User 1");
        UserEntity mockUserEntity2 = new UserEntity();
        mockUserEntity2.setUserUUID("user2UUID");
        mockUserEntity2.setUserName("Test User 2");

        lenient().when(userRepository.findByUserUUID("user1UUID")).thenReturn(Optional.of(mockUserEntity1));
        lenient().when(userRepository.findByUserUUID("user2UUID")).thenReturn(Optional.of(mockUserEntity2));

        GroupUserEntity groupUserEntity1 = new GroupUserEntity();
        groupUserEntity1.setUser(mockUserEntity1);
        GroupUserEntity groupUserEntity2 = new GroupUserEntity();
        groupUserEntity2.setUser(mockUserEntity2);

        lenient().when(groupRepository.save(any(GroupEntity.class))).thenAnswer(invocation -> {
            GroupEntity group = invocation.getArgument(0);
            group.setId(1L);
            return group;
        });

        lenient().when(groupUserRepository.findByGroup_Id(1L)).thenReturn(Arrays.asList(groupUserEntity1, groupUserEntity2));
    }


    @Test
    void createGroup_success() throws CustomException {
        CreateGroupRequestDto requestDto = new CreateGroupRequestDto();
        requestDto.setGroupName("Test Group");
        requestDto.setGroupUserList(Arrays.asList("user1UUID", "user2UUID"));

        CreateGroupResponseDto responseDto = clusterService.createGroup(requestDto);

        assertNotNull(responseDto);
        assertEquals("Test Group", responseDto.getGroupName());
        assertEquals("2", responseDto.getGroupMemberCount());
        assertEquals(2, responseDto.getUserList().size());
    }

    @Test
    void createGroup_userNotFound_throwsCustomException() {
        when(userRepository.findByUserUUID("invalidUserUUID")).thenReturn(Optional.empty());

        CreateGroupRequestDto requestDto = new CreateGroupRequestDto(
                "Test Group",
                "1",
                Arrays.asList("invalidUserUUID")
        );


        CustomException exception = assertThrows(CustomException.class, () -> {
            clusterService.createGroup(requestDto);
        });

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

}



