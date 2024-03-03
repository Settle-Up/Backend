package settleup.backend.domain.receipt.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OcrResponseDto {
    private String operationLocation;
    private String envoyUpstreamServiceTime;
    private String apimRequestId;
    private String strictTransportSecurity;
    private String contentTypeOptions;
    private String msRegion;
    private String date;
}
