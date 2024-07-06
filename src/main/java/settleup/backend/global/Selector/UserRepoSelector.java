package settleup.backend.global.Selector;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import settleup.backend.domain.group.entity.*;
import settleup.backend.domain.group.repository.*;
import settleup.backend.domain.user.entity.*;
import settleup.backend.domain.user.repository.*;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

@Service
@AllArgsConstructor
public class UserRepoSelector {

    private final UserRepository userRepo;
    private final DemoUserRepository demoUserRepo;
    private final GroupRepository groupRepo;
    private final DemoGroupRepository demoGroupRepo;
    private final GroupUserRepository groupUserRepo;
    private final DemoGroupUserRepository demoGroupUserRepo;

    public UserBaseRepository<? extends UserTypeEntity> getUserRepository(Boolean isRegularUser) {
        return isRegularUser ? userRepo : demoUserRepo;
    }

    // 일반 사용자와 데모 사용자를 위한 그룹 리포지토리 반환
    public GroupBaseRepository<? extends GroupTypeEntity> getGroupRepository(Boolean isRegularUser) {
        return isRegularUser ? groupRepo : demoGroupRepo;
    }

    // 일반 사용자와 데모 사용자를 위한 그룹 유저 리포지토리 반환
    public GroupUserBaseRepository<? extends GroupUserTypeEntity> getGroupUserRepository(Boolean isRegularUser) {
        return isRegularUser ? groupUserRepo : demoGroupUserRepo;
    }
//    public GroupUserBaseRepository<GroupUserTypeEntity> getGroupUserRepository(Boolean isRegularUser) {
//        return (GroupUserBaseRepository<GroupUserTypeEntity>) (isRegularUser ? groupUserRepo : demoGroupUserRepo);
//    }
    public GroupUserTypeEntity findGroupUser(Boolean isRegularUser, Long userId) {
        GroupUserBaseRepository<? extends GroupUserTypeEntity> repository = getGroupUserRepository(isRegularUser);
        return repository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public GroupTypeEntity getGroupEntity(Boolean isRegularUser) {
        return isRegularUser ? new GroupEntity() : new DemoGroupEntity();
    }

    public GroupUserTypeEntity getGroupUserEntity(Boolean isRegularUser) {
        return isRegularUser ? new GroupUserEntity() : new DemoGroupUserEntity();
    }

    public UserTypeEntity getUserEntity(Boolean isRegularUser) {
        return isRegularUser ? new UserEntity() : new DemoUserEntity();
    }
}
