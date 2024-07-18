package settleup.backend.global.Util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.GenericFilterBean;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalTime;

@Slf4j
public class ServiceMaintenanceFilter extends GenericFilterBean {

    private final LocalTime maintenanceStartTime = LocalTime.of(3, 0);
    private final LocalTime maintenanceEndTime = LocalTime.of(3, 25);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        LocalTime now = LocalTime.now();

        if (now.isAfter(maintenanceStartTime) && now.isBefore(maintenanceEndTime)) {
            setMaintenanceResponse(httpServletResponse);
            return;
        }

        chain.doFilter(request, response);
    }

    private void setMaintenanceResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        writer.write("{");
        writer.write("\"success\": false,");
        writer.write("\"message\": \"Service is under maintenance from 03:00 to 03:15\",");
        writer.write("\"data\": null,");
        writer.write("\"errorCode\": \"" + ErrorCode.SERVICE_UNDER_MAINTENANCE.getCode() + "\"");
        writer.write("}");
        writer.flush();
    }
}
