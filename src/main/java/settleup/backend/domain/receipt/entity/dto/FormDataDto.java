package settleup.backend.domain.receipt.entity.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FormDataDto {
    private String text;
    private MultipartFile image;
}
