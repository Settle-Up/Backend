# SettleUp Project - **service-server (backend)**

## 💡 개발 기간 및 인원
개발기간 : 2024 .2.19 ~ 2024.4. 28 (70일간) </br>
개발인원 : </br>
**Backend  : 서동희**  </br>
**Frontend : 박수빈**  </br> 

## 💡 기술 스택

| 분야      | 기술스택                                                                                                   |
|------------|----------------------------------------------------------------------------------------------------------|
| 언어       | ![Java](https://img.shields.io/badge/Java-EE4266?style=flat-square&logo=Java&logoColor=white)             |
| 프레임워크 | ![Spring](https://img.shields.io/badge/Spring-337357?style=flat-square&logo=Spring&logoColor=white)       |
| 데이터베이스 | ![MySQL](https://img.shields.io/badge/MySQL-59B4C3?style=flat-square&logo=MySQL&logoColor=white)        |
| ORM        | ![JPA](https://img.shields.io/badge/JPA-000000?style=flat-square&logo=Jpa&logoColor=white)                |
| 메모리     | ![Redis](https://img.shields.io/badge/Redis-EE4266?style=flat-square&logo=Redis&logoColor=white)          |
| 배포       | ![Shell](https://img.shields.io/badge/Shell-A5DD9B?style=flat-square&logo=Shell&logoColor=white) ![GithubAction](https://img.shields.io/badge/GithubAction-EE4266?style=flat-square&logo=Github&logoColor=white) ![AWS](https://img.shields.io/badge/AWS-FFD23F?style=flat-square&logo=AWS&logoColor=white) ![CodeDeploy](https://img.shields.io/badge/CodeDeploy-FFD23F?style=flat-square&logo=AWS&logoColor=white) ![S3](https://img.shields.io/badge/S3-A5DD9B?style=flat-square&logo=S3&logoColor=white)   


## 💡 서비스 소개 </br>
저희 서비스는 그룹 내 공유 정산서 기능을 제공합니다. 특히 장기간에 걸쳐 자주 발생하는 지출이 있는 경우(룸메이트, 장기 여행자, 정기 모임 등)에 초점을 맞추고 있습니다. 이런 상황에서는 각 거래 후 바로 정산하지 않으면, 시간이 지나면서 누가 누구에게 얼마나 채무가 있는지 파악하기 어려워집니다. 저희 시스템은 이를 해결하기 위해 개발되었습니다.</br>

SettleUp 서비스를 통해 사용자는 영수증 사진을 간편하게 등록할 수 있습니다. 이렇게 쌓인 비용은 최적화 자료에 따라 언제든지 유저가 원하는 시기에 개인적으로 송금하고, 그 결과는 그룹 전체에 반영됩니다. 장기간 비용이 발생하는 모임에서, 모든 거래를 추적할 필요 없이 저희 시스템은 그룹 내 채무 관계를 알고리즘으로 재조정하여 최소한의 거래 횟수를 요구합니다.</br>

저희 사이트는 장기간 발생한 비용으로 인해 복잡해진 채무 관계를 단순화하여, 사용자가 받아야 할 금액과 지불해야 할 금액을 명확하게 제시합니다. 이 기능이 저희 서비스의 핵심입니다.</br>

💡**기능 추가 설명**</br>
실제 영수증을 기반으로 거래를 추적하여 투명한 최적화를 제공합니다. 사용자는 영수증 사진을 업로드하기만 하면, 그룹 내 특정 인원을 아이템별로 선택해 각 금액을 할당하고, 서버에서 최적화를 통해 얽힌 채무 관계를 재조정할 수 있습니다.

일부 송금이 완료되면 보낸 유저는 그 금액을 체크하고, 받는 유저는 페이지에 접속했을 때 누구에게 얼마를 받았는지 확인할 수 있습니다.

또한, 그룹 내 비용 발생의 시작점이 되는 영수증은 날짜별로 유저가 추적할 수 있도록 설계했습니다.

![스크린샷 2024-05-02 오후 4 30 54](https://github.com/Settle-Up/settle-up-server/assets/129722492/cd36737b-bb88-44b2-9f8b-56d3ed0d0250)


## 💡구현 기능

아래는 구현기능과 더불어 기획단계 때 개발에 주안점을 담았던 것들에 대한 상세한 설명과 플로워 그램이 포함되어 있습니다 
내용이 방대한 점을 고려하여, 혼란을 줄이고 가독성을 높이기 위해 파일들을 readme 파일에 모두 포함시키지 않고 분리하기로 결정했습니다. </br></br>
**코드를 살펴보기시기 전에 아래 파일들을 검토하시면 시스템에 대한 더 명확한 이해를 제공해 드릴 수 있습니다**.

아래는 개발한 기능들의 상세 목록, 그 기능들의 기능성, 그리고 설명이 필요한 경우 readme 파일에 추가한 파일 이름들입니다: 

| **번호** | **기능& readme파일명**                              | **분류**           |
|-----------|----------------------------------------|--------------------|
| 1         | 소셜로그인.md                         | 보안관련 api          |
| 2         | 로그아웃.md                           | 보안관련 api        |
| 3         | 그룹리스트 불러오기               | 그룹관련 api          |
| 4         | 이메일 검색.md                        | 그룹관련 api         |
| 5         | 그룹생성                         | 그룹관련 api        |
| 6         | 그룹정보 불러오기                 | 그룹관련 api     |
| 7         | 그룹 탈퇴.md                          | 그룹관련 api         |
| 8         | 멤버추가.md                           | 그룹관련 api          |
| 9         | 그룹내 나의 최적화된 자본리스트1.md   | 상세페이지관련 api    |
| 10        | 그룹내 영수증 리스트2.md             | 상세페이지관련 api  |
| 11        | 알림 설정                             | 상세페이지관련 api    |
| 12        | 지난 영수증 보여주기.md               | 상세페이지관련 api    |
| 13        | 영수증 사진관련하여 외부 api 호출.md | 비용관련 api         |
| 14        | 새로운 영수증 입력(최적화).md        | 비용관련 api       |
| 15        | 유저가 송금한 리스트 데이터베이스에 반영.md | 비용관련 api         |
| 16        | 타인이 나에게 송금한 리스트 불러오기.md  | 비용관련 api    |
| 17        | 기본세팅입력                       | 유저관련 api         |
| 18        | 기본세팅불러오기                | 유저관련  api        |
| 19        | ci/cd 도입기 .md                   | 배포관련 doc   |
| 20        | 로드발란서 healthcheck                   | 배포관련  api  |
| 21        | 추후에 개선하고 싶은 부분              | 추후서비스 doc|

  
## 💡 데이터 베이스 스키마 구조 


## 💡 회고

본 프로젝트는 백엔드 한명 , 프론트 한명이서 개발한 프로그램이였습니다 
즉 각자의 파트에서 처음부터 끝까지 웹사이트를 기획하고 프로그래밍으로 실현해서 결과를 확인 할수 있어서 개발에 대한 시야를 넓힐 수 있는 좋은 기회였습니다 

하지만 , 그만큼 혼자서 모든 기능을 구현하는 과정이 쉽지는 않았던거 같습니다 
서버의 모든 것을 혼자 개발 하다보니 , 이 프로젝트의 서버 자체가 현재 백엔드를 개발하는 나의 실력을 정확하게 나타내는 지표라는 생각을 했습니다</br> 
잘한 부분도 , 부족한 부분도 온전히 나로 인한 결과라는 생각에 
하나의 기능을 구현하는데 있어서도 내가 고려하지 못한 변수는 없는지 , 더 좋은 구조로 재 사용하도록 설계할 수 없는지 스스로의 코드를 항상 의심하고 다시 확인하는 작업의 연속이였습니다 

이 프로젝트로 덕분에 많은 개발 능력이 향상되고 그중에서도 제가 가장 많은 부분 시간을 할애하고 성장했던 부분은 5가지 인것 같습니다 

**1. api 목적성에 따른 실행흐름의 제어</br>
2. 재사용성</br>
3. 인터페이스 기반 프로그래밍</br>
4. 배포의 자동화와 배포</br>
5. 체계적인 작업과 스스로 묻고 답하기</br>**

-------------------------------------------

**1. api 목적성에 따른 실행흐름의 제어**

a)
실물영수증을 사진으로 받아 데이터화 하는데는 마이크로소프트의 azure 서비스를 외부 api 로 사용하였습니다 
이 과정에서 외부api을 호출을 바로 응답을 받으면 성공여부는 success이지만 , 안에 데이터는 아직 파싱중이라는 메세지를 받고 , 호출 후 3초에서 4초후에 완전한 응답을 받을 수 있었습니다 
실행흐름을 제어하지 않으면 , 클라이언트단으로 빈 데이터를 보내는 일이 발생하였습니다 

이에 자바의 schedule 을 활용하여 호출을 하고 5초동안 비동기적으로 API 호출의 상태를 주기적으로 확인할 수 있도록 대응하였습니다

b)
저희 프로그램의 영수증 입력 api 는 실물 영수증을 서버에 전달하고 서버에서 그 영수증을 기반으로 최적화를 진행하는 과정으로 유저에게 최소화된 송금데이터를 제공할 수 있도록 데이터를 쌓는 역활입니다
api의 궁극적인 목적은 영수증을 입력받아 알고리즘을 통해 "최적화"하는 것이지만 , 유저의 입장에서는 자신이 하는 행위인 "영수증 등록"이 영수증 등록만 데이터 베이스에 잘 입력된다면 
추후의 최적화 과정에서의 오류나 , exception 에 대해 서버에서 클라이언트로 반환받는 것은 적절하지 않다고 생각하였습니다
 
이에 영수증 입력 api 자체는
| 단계 | 설명 |
|------|------|
| a    | 영수증의 각 금액을 채무로 할당 |
| b    | 그룹 내 두 명의 유저 사이에 형성될 수 있는 관계를 노드화하여, 둘 사이의 거래를 하나로 통합하는 1차 최적화 |
| c    | 그룹 내 각 개인이 받거나 주어야 할 총액의 순액 구하기 |
| d    | DFS를 통해 앞뒤 가중치가 같은 에지를 하나로 생성하는 2차 최적화 |
| e    | 양방향 에지가 생성될 경우, 하나의 에지로 통일하는 3차 최적화 |

총 과정중 a 만 성공을 하면 클라이언트 단으로 바로 성공 메세지를 보내고 그 추후의 과정들은 sentry 라는 외부 api 를 통해 서버측에 에러나 exception 이 발생하면 정해진 서버의 알림창으로 메세지를 받도록 설계하였습니다 
또한 a 의 과정이 완료되고 ,a 의 데이터가 다 입력이 된 후 에 그 데이터를 기반으로 추후 알고리즘이 작동할 수 있어서 a 의 과정이 완료되고 컨트롤러 단에서 클라이언트에게 성공메세지를 반환하는 순간 이벤트를 발행하여 
추후 과정들이 이벤트 리스너를 통해 진행되도록 실행흐름을 제어했습니다 

또한 3차 최적화는 2차 최적화가 일어나지 않는다면 (채무금액이 같은 노드가 연속된것이 없다면) 양방향으로 에지가 형성될 가능성이 없기 때문에 3차 최적화는 2차 최적화가 진행된다는 전제하에 호출이 될 수 있도록 흐름을 제어 했습니다 

프로젝트를 진행하면서 동기적 프로그래밍이 클라이언트부터 데이터 베이스 저장까지의 기능을 수행함에있어서  동작 순서 보장의 한계가있다는 것을 깨달았습니다 

저는 이에 이벤트기반 프로그래밍으로 제가 원하는 시점에 함수를 호출할 수 있도록 개선하였습니다 

-----------------------------------------
**2. 재사용성**
  
 dto 를 설계 함에 있어서 포괄적으로 사용할 수 있는 dto를 작성하고 , 각 상황에 맞게 @jsonInclude를 통해 데이터가 포함되어 있지 않다면 클라이언트 에게 보여지는 데이터를 구분하였습니다 
 무엇보다 재사용성에 대해 가장 신경을 많이 쓴 부분은 errorHandler , exception 부분이였습니다 
 - ErrorCode </br>
 열거형을 통해서 표준화된 에러 코드를 사용할 수 있도록 하였으며 유지 보수가 용이하도록 새로 추가되는 에러 코드를 쉽게 확장할 수 있도록 설계하였습니다</br>
 - Custom Exception </br>
 특정 에러코드를 기반으로 예외를 이르키며 해당 코드의 의미와 함께 상세 메세지를 함께 제공할 수 있도록 설계하였습니다 </br>
 - Http status code mapping </br>
 각 에러 코드에 적절한 Http 상태 코드를 매핑하여 클라이언트가 api 응답을 통해 명확한 상태를 파악할 수 있도록 하였습니다</br>
 - Global Error handler </br>
 각 예외 대해 일관된 응답을 생성하고 CustomException, SSLException 등 예외별로 다른 로직을 적용합니다</br>
 - 응답 포맷 통일 </br>
 응답마다 매번 다른 dto를 생성하는것은 인스턴스의 낭비라고 생각하고 처음 기획할 때 부터 응답은 아래와 같은 형식의 객체로 통일하여 클라이언트에게 응답에대한 혼란을 줄이고자 했습니다 </br>
```java
@Data
@NoArgsConstructor
public class ResponseDto<T> {
    private boolean success;
    private String message;
    private T data;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String errorCode;


    public ResponseDto(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public ResponseDto(boolean success, String message, T data, String errorCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
    }
}

```
 
 -----------------------------------
**3. 인터페이스기반 프로그래밍**
  

  비용의 최적화를 설계하면서 1차, 2차 , 3차 최적화를 담는 엔티티를 하나의 공통된 엔티티를 공통으로 상속하게 하므로써 중복되는 코드에 대해 최소화하였습니다

  --------------------------------------------------

**4. 배포의 자동화와 보완**
   
   githubAction 과 AWS의 기능 중 하나인  EC2, S3 , Codedeploy 를 통해 지속적 통합 배포를 실현했습니다 
   이를 이행하면서 가장 시간을 할애를 많이 했던 부분은 리눅스기반의 shell을 작성하여 배포과정을 자동화 하는 것이였습니다 , 
   처음 작성해보는 shell으로 github에 올라가면 안되는 민감한 정보(.env) 가 업로드 되지 않고 인스턴스 안에서는 주입받아야하는 부분이 가장 난제였던거 같습니다
   저는 이에 githubAction의 Secert을 활용하였고 , application.yml 같은 민감한 정보를 담은 파일은 인스턴스에서 파일을 실행할때 인스턴스 안에서 생성을 하고 , 성공적으로 실행된것이 확인이 되면 
   인스턴스안에 남아있는 민감정보를 자동으로 지워주는 일련의 과정을 shell로 작성하였습니다 

   배포과정을 설계하고 , 코드를 작성하면서 많은 블로그를 찾아보았는데 여러 블로그에서 인스턴스 자체에서 환경변수(export) 를 해서 민감정보를 그대로 노출하는 방식을 사용하여 진행한것을 볼수 있었습니다 
   저는 환경변수를 그대로 노출하는 경우는 시스템의 다른 사용자가 환경변수를 통해 쉽게 접근할 수 있고 , 환경변수에 저장된 민감정보가 로그에 기록될수 있어서 상당히 보안적으로 위험하다고 생각했습니다
   
   배포의 자동화를 도입하기 전에는 생각해보지 못했던 여러 보안적 측면에서 고려할 수 있었습니다

   --------------------------------
**5.체계적인 작업과 스스로 묻고 답하기**

  백엔드 서버 , 프론트엔드 각 한명씩 진행하다보니 처음에는 걱정되는 부분이 크게 3가지가 있었습니다 
  
1.한 명이 프로젝트에서 이탈할 경우, 프로젝트 진행이 지연되거나 어려워질 수 있다.</br>
2.개인이 서버를 개발하는 경우, 조직 규칙보다 개인적인 선호도에 따라 개발될 위험이 있다.</br>
3.문제 해결 방안은 팀원과 공유할 수 있지만, 최종적으로 해결하고 이행하는 것은 나의 몫이다.</br>

  이 3가지를 해결하기 위해 아래와 같은 방식으로 진행하였습니다 

  
  1.한 명이 프로젝트에서 이탈할 경우, 프로젝트 진행이 지연되거나 어려워질 수 있다=>
  
프로젝트 기획 및 초기 개발 단계에서는 의논할 사항이 많을 것이라 예상하여 매일 오프라인 미팅을 진행했습니다. 프로젝트의 청사진이 어느 정도 잡힌 후에는 각자 작업하기 좋은 환경에서 오전 11시와 오후 9시에 온라인 미팅을 통해 진행 상황을 공유했습니다.
팀원과 개발 분야는 서로 다르지만 매일 오늘의 컨디션이 어떤지, 블로커 상황에서 어떤 방법을 도입했는지에 대해 공유하고 프로젝트가 끝날때 까지 호흡을 맞출 수 있었습니다
  
  2.개인이 서버를 개발하는 경우, 조직 규칙보다 개인적인 선호도에 따라 개발될 위험이 있다.=>
     
프로젝트 기획이 끝난 후 노션을 통해 공용 페이지를 만들고, 프론트엔드와 백엔드에서 공유할 사항(와이어프레임, ERD, API 명세)을 분리하여 관리했습니다. API를 기반으로 우선순위를 정하고, 각 API에 필요한 기간을 나누어 작업했습니다.

또한, DDD와 MVC 모델을 활용하여 일관된 아키텍처를 설계했습니다. MVC 계층 구조로 도메인, 서비스, 프레젠테이션 계층 간 의존성을 통제했고, DDD 모델로 일관된 구조를 유지했습니다. 깃허브 브랜치 전략은 main(배포), develop(개발), hotfix(긴급 수정), feature(기능)로 구분해 혼자 서버를 개발하면서도 팀 작업처럼 일관성을 유지했습니다.

 3.문제 해결 방안은 팀원과 공유할 수 있지만, 최종적으로 해결하고 이행하는 것은 나의 몫이다=>

  프로젝트를 진행하면서 가장 힘들었던것은 FE , BE 각 한명씩 프로젝트를 진행하다보니 각 파트에 대한 모르는 부분이나 명확하지 않은 부분을 전부 혼자 해결해야하는 것이였습니다 
  항상코드를 작성하면서 "이 방법이 최선인가?"에 대해 많이 고민하고 다른 사람들은 어떤 식으로 하는지 찾아봤던것 같습니다 

  api를 작성하면서 해당 기능에대해 너무 방대하고 막막하게 느껴질때면 api 를 작성하기 전에 javadoc 을 이용해서 
```
/**
해야하는기능:
받는데이터:
반환해야할 값:
기능세분화 1.:
기능세분화 2. :
/**
```
이런식으로 적어 두고 스스로에게 묻고 답을 찾아가는 방식으로 진행하였습니다 

본프로젝트를 진행하면서 전반적인 부분을 모두 담당개발을 진행한 결과 전반적인 백엔드 서버에 대해 이해도가 깊어지고 개발하는 방식에대한 체계성이 생겼습니다

      
## ⚙️ 협업툴

<div>
<img src="https://img.shields.io/badge/Discode-4A154B?style=flat&logo=Slack&logoColor=white"/>
<img src="https://img.shields.io/badge/Notion-000000?style=flat&logo=Notion&logoColor=white"/>
</div>
