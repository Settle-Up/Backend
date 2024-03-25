package settleup.backend.domain.group.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OptimizedDetailUUIDsDto {
    private List<String> searchUUIDForGroupOptimized;
    private List<String> searchUUIDForOptimized;
}
