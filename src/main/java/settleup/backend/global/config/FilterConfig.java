package settleup.backend.global.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import settleup.backend.global.Util.ServiceMaintenanceFilter;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<ServiceMaintenanceFilter> serviceMaintenanceFilter() {
        FilterRegistrationBean<ServiceMaintenanceFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ServiceMaintenanceFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}