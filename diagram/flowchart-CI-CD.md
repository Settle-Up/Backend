## 자동 배포를 선택하게 된 계기 
개발 과정에서 기획 단계의 완벽해 보이던 프레임워크들이 실제로 구현을 시작하면서 프론트엔드 팀과의 상세한 논의가 필요하게 되었습니다. 이러한 상황에서 기능별 우선순위를 정하고, 프론트엔드와 백엔드 개발의 흐름을 맞추어, 진행 중인 브랜치에 대한 원활한 커뮤니케이션을 위한 전략을 수립했습니다.

생산성을 극대화하기 위해, 한 기능이 완성될 때마다 클라이언트 서버와 서비스 서버 간 API를 확인하고, 실제 작동 방식을 검토한 후 다음 단계로 넘어가기로 결정했습니다. 그러나 실제 서버 배포 없이는 로컬 터널링이나 퍼블릭 터널링 서비스를 이용하는 것 외에는 제한적인 테스트만 가능했습니다. 초반에는 ngork를 이용해서 임시로 인터넷에 공개하여 외부접근을 가능하게 하였지만, 도메인이 가변적이라는 점과 , 제 로컬 서버가 계속 구동되야만 테스트가 가능하였습니다
이러한 한계를 극복하고 프론트엔드 팀이 단위 테스트를 수행할 수 있도록 CI/CD 파이프라인을 구축하기로 결정했습니다.

GitHub Actions를 통한 지속적인 통합 및 배포(CI/CD)를 선택한 이유는 파이프라인 구축에 많은 시간을 할애하기보다는 코드 변경 사항이 있을 때마다 바로 배포에 반영할 수 있는 통합 서비스의 매력 때문이었습니다. 

## 배포를 하면서 고려한 사할
통합 서비스를 사용한다는 것은 보안이 한 곳에서 무너지면 전체가 위험해질 수 있다는 것을 의미합니다. GitHub Actions를 안전하게 도입하기 위해 다음 전략을 사용했습니다:

1. 환경 변수 주입: 배포 시 application.yml 같은 환경 변수를 자동으로 생성하는 스크립트를 통해 코드 노출을 방지합니다.
2. 배포 후 파일 삭제: 배포가 성공적으로 완료된 후, 환경 변수가 주입된 파일을 자동으로 삭제하는 스크립트를 작성했습니다.
3. 환경 변수 주입 방식: GitHub Secrets을 통해 환경 변수를 안전하게 주입합니다.
4. 저장소 보안 설정: GitHub Secrets가 정의된 저장소는 기본적으로 private 설정을 유지합니다.

## CI/CD 배포 workflow
   ![스크린샷 2024-04-05 오후 8 52 34](https://github.com/Settle-Up/settle-up-server/assets/129722492/fbbc111c-cdec-4120-afac-c727071e181d)

1. 트리거 설정: Main, Develop, 기능별 브랜치를 나누어 Main 브랜치에 Push되는 변경사항은 자동으로 GitHub Actions 워크플로우를 실행하도록 설정했습니다.
2. 빌드 환경 설정: Ubuntu 최신 버전의 러너를 사용하여 작업을 실행하고, JDK 17을 설정하여 Gradle을 통해 빌드를 진행합니다. 또한, gradlew에 실행 권한을 추가합니다.
3. 배포 준비: 배포 디렉토리를 생성하고, appspec.yml 파일과 스크립트를 복사한 후 실행 권한을 부여합니다. 다양한 서비스 접속 정보와 키를 GitHub Secrets에서 가져와 application.yml을 생성합니다.
```
      - name: Create deployment directory and copy JAR file
        run: |
          mkdir -p deployment
          cp build/libs/*.jar deployment/
      
      - name: Copy appspec.yml and scripts to deployment directory
        run: |
          cp appspec.yml deployment/
          cp -R scripts/ deployment/scripts/

      - name: Grant execute permission for scripts
        run: chmod +x ./scripts/*.sh
      
      - name: Create application.yml in deployment directory
        run: ./scripts/create-application-yml.sh deployment/
        env:
```
4. AWS S3 업로드: 배포 디렉토리의 내용을 zip 파일로 압축하여 S3로 업로드합니다.
```
        run: aws s3 cp deployment.zip s3://settleupsooandhee/settleupbucket/deployment.zip
      
      - name: Deploy to AWS EC2 via CodeDeploy
        run: |
          aws deploy create-deployment \
            --application-name settleupDeploy \
            --deployment-group-name settleup \
            --revision revisionType=S3,s3Location="{bucket=settleupsooandhee,key=settleupbucket/deployment.zip,bundleType=zip}" \
            --deployment-config-name CodeDeployDefault.OneAtATime
          
```
5. AWS CodeDeploy 배포: S3에 업로드된 zip 파일을 CodeDeploy를 통해 EC2 인스턴스에 배포합니다. CodeDeploy는 appspec.yml 파일을 참조하여 배포 과정을 관리합니다.
```
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
```
6. EC2 인스턴스 작업: SSH를 통해 EC2 인스턴스에 접속하여 배포 스크립트를 실행합니다. 이 스크립트는 애플리케이션을 시작하고 성공적으로 실행되었는지 확인합니다.실행확인 후 보안에 관련된 파일 스크립트를 통한 자동 삭제합니다
```
    - name: Execute script on EC2 instance via SSH
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.HOST }}
          username: ${{ secrets.USERNAME }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          script: |
            cd /home/ubuntu/app/deploy  
            chmod +x ./scripts/start.sh
            ./scripts/start.sh & 
            sleep 15  
            echo "스프링 시작 로그 확인:"
            ps aux | grep backend-0.0.1-SNAPSHOT.jar
            if pgrep -f backend-0.0.1-SNAPSHOT.jar > /dev/null
            then
                echo "hello.deploy.success."
            else
                echo "Deployment failed, application is not running."
                exit 1
            fi
```
```
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
```
7. 배포 로그 관리: 배포 과정에서의 모든 중요 정보는 로그 파일에 기록되어 배포의 성공 여부 및 발생한 문제 등을 추후 확인할 수 있습니다.
