package settleup.backend.global.Selector;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import settleup.backend.domain.group.entity.*;
import settleup.backend.domain.group.entity.dto.GroupMonthlyReportDto;
import settleup.backend.domain.group.repository.*;
import settleup.backend.domain.user.entity.*;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.repository.*;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserRepoSelector {

    private final UserRepository userRepo;
    private final DemoUserRepository demoUserRepo;
    private final GroupRepository groupRepo;
    private final DemoGroupRepository demoGroupRepo;
    private final GroupUserRepository groupUserRepo;
    private final DemoGroupUserRepository demoGroupUserRepo;

    public UserBaseRepository<? extends AbstractUserEntity> getUserRepository(Boolean isRegularUser) {
        return isRegularUser ? userRepo : demoUserRepo;
    }

    // 일반 사용자와 데모 사용자를 위한 그룹 리포지토리 반환
    public GroupBaseRepository<? extends AbstractGroupEntity> getGroupRepository(Boolean isRegularUser) {
        return isRegularUser ? groupRepo : demoGroupRepo;
    }

    // 일반 사용자와 데모 사용자를 위한 그룹 유저 리포지토리 반환
    public GroupUserBaseRepository<? extends AbstractGroupUserEntity> getGroupUserRepository(Boolean isRegularUser) {
        return isRegularUser ? groupUserRepo : demoGroupUserRepo;
    }

    public AbstractGroupUserEntity findGroupUser(Boolean isRegularUser, Long userId) {
        GroupUserBaseRepository<? extends AbstractGroupUserEntity> repository = getGroupUserRepository(isRegularUser);
        return repository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public AbstractGroupEntity getGroupEntity(Boolean isRegularUser) {
        return isRegularUser ? new GroupEntity() : new DemoGroupEntity();
    }

    public AbstractGroupUserEntity getGroupUserEntity(Boolean isRegularUser) {
        return isRegularUser ? new GroupUserEntity() : new DemoGroupUserEntity();
    }

    public AbstractUserEntity getUserEntity(Boolean isRegularUser) {
        return isRegularUser ? new UserEntity() : new DemoUserEntity();
    }
}

