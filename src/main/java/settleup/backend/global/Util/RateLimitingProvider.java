package settleup.backend.global.Util;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import settleup.backend.domain.user.entity.DemoUserEntity;
import settleup.backend.domain.user.repository.DemoUserRepository;
import settleup.backend.domain.user.repository.RelatedEntityDeletionService;
import settleup.backend.domain.user.service.Impl.UserDeletionService;
import settleup.backend.global.config.RateLimitingConfig;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class RateLimitingProvider {

    private final DemoUserRepository demoUserRepository;

    private final RateLimitingConfig config;

    private final UserDeletionService userDeletionService;

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingProvider.class);



    public void checkRateLimit(String ip) throws CustomException {
        LocalDateTime currentTime = LocalDateTime.now();
        List<DemoUserEntity> demoUsers = demoUserRepository.findTopByIpOrderByCreatedAtDesc(ip, PageRequest.of(0, 1));
        if (!demoUsers.isEmpty()) {
            DemoUserEntity demoUser = demoUsers.get(0);
            LocalDateTime createdAt = demoUser.getCreatedAt();
            if (createdAt.plusMinutes(config.getTimeLimitMinutes()).isAfter(currentTime)) {
                throw new CustomException(ErrorCode.RATE_LIMIT_EXCEEDED, "Rate limit exceeded for IP: " + ip);
            }
        }
    }
    @Scheduled(cron = "0 0 3 * * *") // 매일 3시에 실행
    public void deleteExpiredDemoUsers() {
        LocalTime now = LocalTime.now();
        LocalTime maintenanceStartTime = LocalTime.of(3, 0);
        LocalTime maintenanceEndTime = LocalTime.of(3, 15);

        if (now.isAfter(maintenanceStartTime) && now.isBefore(maintenanceEndTime)) {
            logger.info("Starting maintenance deletion of expired demo users...");

            LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(config.getTimeLimitMinutes());
            List<Long> expiredUserIds = demoUserRepository.findExpiredNonDummyUserIds(expirationTime);

            if (expiredUserIds.isEmpty()) {
                logger.info("No expired demo users found.");
                return;
            }

            logger.info("Found {} expired demo users. Deleting...", expiredUserIds.size());

            for (Long userId : expiredUserIds) {
                try {
                    userDeletionService.deleteUserWithRelatedEntities(userId);
                    demoUserRepository.deleteById(userId);
                    logger.info("Successfully deleted user with ID: {}", userId);
                } catch (Exception e) {
                    logger.error("Failed to delete user with ID: {}", userId, e);
                }
            }

            // Call to delete expired groups
            userDeletionService.deleteExpiredGroups(expirationTime);

        } else {
            logger.info("Current time is not within the maintenance window. Skipping deletion.");
        }
    }
}