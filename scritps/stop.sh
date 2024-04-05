# secrets 보안 관리를 위해 private repository 안에서
# GitHub Actions와 AWS CodeDeploy, S3, EC2를 통해 CI/CD 파이프라인을 구축했습니다.
# 아래는 private repository 안의 코드를 재현한 것입니다.

#!/bin/bash
DEPLOY_PATH="/home/ubuntu/app/deploy"
JAR_NAME="backend-0.0.1-SNAPSHOT.jar"

DEPLOY_LOG="$DEPLOY_PATH/deploy.log"

echo "> 현재 실행중인 애플리케이션 PID 확인" >> "$DEPLOY_LOG"
CURRENT_PID=$(pgrep -fl "$JAR_NAME" | grep java | awk '{print $1}')

if [ -z "$CURRENT_PID" ]; then
  echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다." >> "$DEPLOY_LOG"
else
  echo "> kill -15 $CURRENT_PID" >> "$DEPLOY_LOG"
  kill -15 "$CURRENT_PID"
  sleep 5
  echo "> 기존 애플리케이션 종료 완료" >> "$DEPLOY_LOG"
fi
