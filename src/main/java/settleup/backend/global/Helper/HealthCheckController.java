package settleup.backend.global.Helper;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class HealthCheckController {
    @GetMapping("/auth/check/connection/browser")
    public String getBrowserConnection() {
        return "forward:/hello.html";
    }
}

