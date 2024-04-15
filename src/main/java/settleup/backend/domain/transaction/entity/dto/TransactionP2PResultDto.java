package settleup.backend.domain.transaction.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.transaction.entity.RequiresTransactionEntity;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionP2PResultDto {
    private List<Long> optimiziationByPeerToPeerList = new ArrayList<>();
    private List<List<Long>> nodeList;
    private GroupEntity group;
}
