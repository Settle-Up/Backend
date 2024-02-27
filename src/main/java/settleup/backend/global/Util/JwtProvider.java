package settleup.backend.global.Util;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.exception.CustomException;
import settleup.backend.domain.user.exception.ErrorCode;

import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtProvider {
    @Value("${jwt.secret_key}")
    private String secretKey;

    public String createToken(UserInfoDto userInfoDto) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + Duration.ofDays(1).toMillis());

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(createClaims(userInfoDto))
                .setIssuer("SettleUp")
                .setIssuedAt(now)
                .setExpiration(expiration)
                .setSubject("ForSettleUpLogin")
                .signWith(SignatureAlgorithm.HS256, encodeSecretKey())
                .compact();
    }

    private Map<String, Object> createClaims(UserInfoDto userInfoDto) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userUUID", userInfoDto.getUserUUID().toString());
        claims.put("userName", userInfoDto.getUserName());
        return claims;
    }

    private String encodeSecretKey() {
        return Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public Claims parseJwtToken(String token) throws CustomException {
        try {
            return Jwts.parser()
                    .setSigningKey(encodeSecretKey())
                    .parseClaimsJws(removeBearerTokenPrefix(token))
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        } catch (MalformedJwtException e) {
            throw new CustomException(ErrorCode.TOKEN_MALFORMED);
        } catch (JwtException e) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }
    }

    private String removeBearerTokenPrefix(String token) {
        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}


