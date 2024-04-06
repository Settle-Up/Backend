package settleup.backend.global.Util;

import jakarta.servlet.FilterChain;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@AllArgsConstructor
public class JwtVerificationFilter extends OncePerRequestFilter {

    private final RedisUtils redisUtils;

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String tokenStr = HeaderUtil.getAccessToken(request);

        if (tokenStr != null && tokenStr.startsWith("Bearer ")) {
            tokenStr = tokenStr.substring(7);
        }

        if (tokenStr != null && redisUtils.hasKeyBlackList(tokenStr)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "The token is invalidated (blacklisted).");
            return;
        }

        filterChain.doFilter(request, response);
    }
}