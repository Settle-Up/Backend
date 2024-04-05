# secrets 보안 관리를 위해 private repository 안에서
# GitHub Actions와 AWS CodeDeploy, S3, EC2를 통해 CI/CD 파이프라인을 구축했습니다.
# 아래는 private repository 안의 코드를 재현한 것입니다.

#!/bin/bash
PROJECT_ROOT="/home/ubuntu/app/deploy"
JAR_FILE="$PROJECT_ROOT/backend-0.0.1-SNAPSHOT.jar"
PLAIN_JAR_FILE="$PROJECT_ROOT/backend-0.0.1-SNAPSHOT-plain.jar" 
APP_LOG="$PROJECT_ROOT/application.log"
ERROR_LOG="$PROJECT_ROOT/error.log"
DEPLOY_LOG="$PROJECT_ROOT/deploy.log"
APPLICATION_YML="$PROJECT_ROOT/application.yml"

# 인자 체크
DELETE_FILES_ON_MANUAL_RUN=$1

TIME_NOW=$(date '+%Y-%m-%d %H:%M:%S')

# Java 설치 확인 및 설치
JAVA_VER=$(java -version 2>&1 >/dev/null | grep 'version' | awk '{print $3}')
if [ -z "$JAVA_VER" ]; then
  echo "> Java가 설치되어 있지 않으므로, Corretto 17을 설치합니다." >> "$DEPLOY_LOG"
  sudo apt-get update
  sudo apt-get install -y software-properties-common
  sudo add-apt-repository -y ppa:linuxuprising/java
  sudo apt-get update
  sudo apt-get install -y java-17-openjdk-amd64
  echo "> Corretto 17 설치 완료" >> "$DEPLOY_LOG"
else
  echo "> Java가 이미 설치되어 있습니다: $JAVA_VER" >> "$DEPLOY_LOG"
fi

# 현재 시간을 로그에 추가
echo "====================" >> "$DEPLOY_LOG"
echo "$(date): 배포 시작" >> "$DEPLOY_LOG"
echo "====================" >> "$DEPLOY_LOG"

echo "> build 파일명: $JAR_FILE" >> "$DEPLOY_LOG"

# 현재 실행중인 애플리케이션 pid 확인 및 종료
echo "> 현재 실행중인 애플리케이션 pid 확인" >> "$DEPLOY_LOG"
CURRENT_PID=$(pgrep -f "$JAR_FILE")
if [ -z "$CURRENT_PID" ]; then
  echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다." >> "$DEPLOY_LOG"
else
  echo "> kill -15 $CURRENT_PID" >> "$DEPLOY_LOG"
  kill -15 "$CURRENT_PID"
  sleep 5
  echo "> 기존 애플리케이션 종료 완료" >> "$DEPLOY_LOG"
fi

# JAR 파일 배포 및 실행
echo "> $JAR_FILE 배포 및 실행" >> "$DEPLOY_LOG"
nohup java -jar "$JAR_FILE" --spring.profiles.active=prod > "$APP_LOG" 2> "$ERROR_LOG" &

sleep 15 # Java 애플리케이션의 시작을 기다립니다.

# 애플리케이션 실행 확인 및 조건부 파일 삭제
CURRENT_PID=$(pgrep -f "$JAR_FILE")
if [ -z "$CURRENT_PID" ]; then
    echo "> 애플리케이션 실행 실패." >> "$DEPLOY_LOG"
else
    echo "> 애플리케이션 실행 성공, PID: $CURRENT_PID" >> "$DEPLOY_LOG"
    # 수동 실행 시 파일 삭제
    if [[ "$DELETE_FILES_ON_MANUAL_RUN" == "manual" ]]; then
        if [ -f "$APPLICATION_YML" ]; then
          echo "> 애플리케이션 실행 후 application.yml 파일 삭제" >> "$DEPLOY_LOG"
          rm "$APPLICATION_YML"
          echo "> application.yml 파일 삭제 완료" >> "$DEPLOY_LOG"
        fi
        if [ -f "$PLAIN_JAR_FILE" ]; then
          echo "> 애플리케이션 실행 후 backend-0.0.1-SNAPSHOT-plain.jar 파일 삭제" >> "$DEPLOY_LOG"
          rm "$PLAIN_JAR_FILE"
          echo "> backend-0.0.1-SNAPSHOT-plain.jar 파일 삭제 완료" >> "$DEPLOY_LOG"
        fi
    else
        echo "> 수동 실행이 아니므로 파일 삭제를 건너뜁니다." >> "$DEPLOY_LOG"
    fi
fi

echo "$(date): $JAR_FILE 배포 완료, 실행된 프로세스 아이디: $CURRENT_PID" >> "$DEPLOY_LOG"
echo "hello.world.donghee.deploy.success.congratulations" >> "$DEPLOY_LOG"
echo "====================" >> "$DEPLOY_LOG"
