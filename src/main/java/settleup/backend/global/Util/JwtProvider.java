package settleup.backend.global.Util;

import io.jsonwebtoken.*;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.global.config.CryptographyConfig;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@AllArgsConstructor
public class JwtProvider {

    private final CryptographyConfig cryptographyConfig;
    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    public String createRegularUserToken(UserInfoDto userInfoDto) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + Duration.ofDays(1).toMillis());

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(createClaims(userInfoDto))
                .setIssuer("SettleUp")
                .setIssuedAt(now)
                .setExpiration(expiration)
                .setSubject("FormalLogin")
                .signWith(SignatureAlgorithm.HS256, encodeSecretKey())
                .compact();
    }

    public String createDemoUserToken(UserInfoDto userInfoDto) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + Duration.ofMinutes(60).toMillis());

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(createClaims(userInfoDto))
                .setIssuer("SettleUp")
                .setIssuedAt(now)
                .setExpiration(expiration)
                .setSubject("DemoLogin")
                .signWith(SignatureAlgorithm.HS256, encodeSecretKey())
                .compact();
    }

    private Map<String, Object> createClaims(UserInfoDto userInfoDto) throws CustomException {
        Map<String, Object> claims = new HashMap<>();
        Boolean userType = userInfoDto.getIsRegularUserOrDemoUser();
        if (userType == null) {
            throw new CustomException(ErrorCode.USER_TYPE_CANNOT_BE_NULL);
        }
        String encryptedUserIdAsUUID = ServerCryptUtil.encrypt(userInfoDto.getUserId().toString(), cryptographyConfig.getEncryptionSecretKey());
        String encryptedUserName = ServerCryptUtil.encrypt(userInfoDto.getUserName().toString(), cryptographyConfig.getEncryptionSecretKey());
        claims.put("server-specified-key-01", encryptedUserIdAsUUID);
        claims.put("server-specified-key-02", encryptedUserName);
        if(userType){
            claims.put("userType",true);
        } else if (!userType) {
            claims.put("userType",false);

        }
        return claims;
    }

    private String encodeSecretKey() {
        return Base64.getEncoder().encodeToString(cryptographyConfig.getJwtSecretKey().getBytes());
    }

    public Claims parseJwtTokenGenerationInfo(String token) throws CustomException {
        try {
            logger.info("Parsing token: {}", token);
            String cleanToken = removeBearerTokenPrefix(token);
            logger.info("Clean token: {}", cleanToken);
            return Jwts.parser()
                    .setSigningKey(encodeSecretKey())
                    .parseClaimsJws(cleanToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        } catch (MalformedJwtException e) {
            throw new CustomException(ErrorCode.TOKEN_MALFORMED);
        } catch (JwtException e) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }
    }

    public  String removeBearerTokenPrefix(String token) {
        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}


