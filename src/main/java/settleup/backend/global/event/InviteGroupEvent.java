package settleup.backend.global.event;

import org.springframework.context.ApplicationEvent;
import settleup.backend.domain.group.entity.dto.CreateGroupResponseDto;

public class InviteGroupEvent extends ApplicationEvent {
    private CreateGroupResponseDto groupInfo;

    public InviteGroupEvent(Object source, CreateGroupResponseDto groupInfo) {
        super(source);
        this.groupInfo = groupInfo;
    }

    public CreateGroupResponseDto getGroupInfo() {
        return groupInfo;
    }
}
