# secrets 보안 관리를 위해 private repository 안에서
# GitHub Actions와 AWS CodeDeploy, S3, EC2를 통해 CI/CD 파이프라인을 구축했습니다.
# 아래는 private repository 안의 코드를 재현한 것입니다.

#!/bin/bash

DEPLOYMENT_DIR=$1

# application.yml 파일 생성
cat << EOF > ${DEPLOYMENT_DIR}/application.yml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

  cache:
    type: redis
  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT}
    password: ${REDIS_PASSWORD}

  servlet:
    multipart:
      max-file-size: 3MB
      max-request-size: 5MB

  jpa:
    hibernate:
      ddl-auto: update
    database-platform: org.hibernate.dialect.MySQL8Dialect

logging:
  level:
    root: DEBUG
    org.hibernate.SQL: DEBUG
    p6spy: DEBUG

oauth:
  kakao:
    client_id: ${OAUTH_KAKAO_CLIENT_ID}
    redirect_uri: ${OAUTH_KAKAO_REDIRECT_URI}
    client_authentication_method: POST
    grant_type: ${OAUTH_KAKAO_GRANT_TYPE}
    client_secret: ${OAUTH_KAKAO_CLIENT_SECRET}
    scope: ${OAUTH_KAKAO_SCOPE}
    client_name: Kakao
    kakao_auth_uri: https://kauth.kakao.com/oauth/authorize
    kakao_token_uri: ${OAUTH_KAKAO_KAKAO_TOKEN_URI}
    user_info_accept: ${OAUTH_KAKAO_USER_INFO_ACCEPT}

azure:
  post_url: ${AZURE_POST_URL}
  get_url: ${AZURE_GET_URL}
  api_key: ${AZURE_API_KEY}

jwt:
  secret_key: ${JWT_SECRET_KEY}

encryption:
  secret-key: ${ENCRYPTION_SECRET_KEY}

sentry:
  dsn: ${SENTRY_DSN}
  traces-sample-rate: 1.0
  logging:
    minimum-event-level: info
    minimum-breadcrumb-level: info
EOF
