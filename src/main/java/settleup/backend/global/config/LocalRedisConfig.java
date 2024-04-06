package settleup.backend.global.config;

import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;


@Slf4j
/**
 * profile이 prod가 아닐때만 활성화
 * 배포시 active = prod
 */
@Profile("!prod")
@Configuration
public class LocalRedisConfig {
    @Value("${spring.redis.port}")
    private int redisPort;

    private RedisServer redisServer;

    /**
     * Redis 서버가 이미 실행 중인지 확인하고, 실행 중이라면 사용 가능한 다른 포트를 찾아서 port 변수에 할당하고, 실행 중이 아니라면 redisPort 변수의 값을 사용
     * 현재 시스템이 ARM 아키텍처인지 확인
     * 만약 ARM 아키텍처라면,
     * RedisServer 클래스를 사용하여 Redis 서버를 생성
     * 그렇지 않으면, RedisServer.builder()를 사용하여 Redis 서버를 생성
     * 전 단계에서 생성한 Redis 서버 객체를 실행
     *
     * @throws IOException
     */

    @PostConstruct
    public void redisServer() throws IOException {
        int port = isRedisRunning() ? findAvailablePort() : redisPort;
        if (isArmArchitecture()) {
            System.out.println("ARM Architecture");
            redisServer = new RedisServer(Objects.requireNonNull(getRedisServerExecutable()), port);
        } else {
            redisServer = RedisServer.builder()
                    .port(port)
                    .setting("maxmemory 128M")
                    .build();
        }
        redisServer.start();
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    /**
     * Embedded Redis가 현재 실행중인지 확인
     */
    private boolean isRedisRunning() throws IOException {
        return isRunning(executeGrepProcessCommand(redisPort));
    }

    /**
     * 현재 PC/서버에서 사용가능한 포트 조회
     */
    public int findAvailablePort() throws IOException {

        for (int port = 10000; port <= 65535; port++) {
            Process process = executeGrepProcessCommand(port);
            if (!isRunning(process)) {
                return port;
            }
        }

        throw new IllegalArgumentException("Not Found Available port: 10000 ~ 65535");
    }

    /**
     * 해당 port를 사용중인 프로세스 확인하는 sh 실행
     */
    private Process executeGrepProcessCommand(int port) throws IOException {
        String OS = System.getProperty("os.name").toLowerCase();
        System.out.println("OS: " + OS);
        System.out.println(System.getProperty("os.arch"));
        if (OS.contains("win")) {
            log.info("OS is  " + OS + " " + port);
            String command = String.format("netstat -nao | find \"LISTEN\" | find \"%d\"", port);
            String[] shell = {"cmd.exe", "/y", "/c", command};
            return Runtime.getRuntime().exec(shell);
        }
        String command = String.format("netstat -nat | grep LISTEN|grep %d", port);
        String[] shell = {"/bin/sh", "-c", command};
        return Runtime.getRuntime().exec(shell);
    }

    /**
     * 해당 Process가 현재 실행중인지 확인
     */
    private boolean isRunning(Process process) {
        String line;
        StringBuilder pidInfo = new StringBuilder();

        try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

            while ((line = input.readLine()) != null) {
                pidInfo.append(line);
            }

        } catch (Exception e) {
        }

        return !StringUtils.isEmpty(pidInfo.toString());
    }

    private boolean isArmArchitecture() {
        return System.getProperty("os.arch").contains("aarch64");
    }

    private File getRedisServerExecutable() throws IOException {
        try {
            return new File("src/main/resources/binary/redis/redis-server");
        } catch (Exception e) {
            throw new IOException("Redis Server Executable not found");
        }
    }

}

