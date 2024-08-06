### SettleUp ERD


Below is the entity-relationship diagram (ERD) for the Settle_Up Project, illustrating the relationships between users, groups, receipts, transactions, and other entities within the system.

아래는 Settle_Up 프로젝트의 엔티티 관계 다이어그램(ERD)입니다. 사용자, 그룹, 영수증, 거래 등 시스템 내의 다양한 엔티티 간의 관계를 보여줍니다.

![ERD](https://www.planttext.com/api/plantuml/png/x5XBRjim4Dtx54AM1NA1hEB6GU556zUn3q2JRCoMl_9Jm4rwiYvwf5wXGqkoHL6Y11jeNA2x-74-RzxCq7H-__nrQnqopWi-eT8njokc6-iCmkwTKLxVbzZpf2ZUC5BS54mw-FifdAtNynaWtv31MsHgz0CID4NNXaSWDXGvIgOEKCS41H92ryQhdGuX07FUJdvCQcOpAjJbiReyHb47rvQQ7dRClbVIRVbkIRKoRgq9v7gGSBvHYbCamSQ3-xnLKFjpI37OirVcSGaSwhvaQ8GKoRm4C1cN02wsIgRelK0idY9cPnGpWVXSQkyUj6CghIpNmSjZtBSOJBVZU25seukzl49pR9SKKwPT3s5JdYQSAxpx0chCCoB4K6kxV6UWFU1XHm3KQ0VInClXb4DyOHZUsvKb0aoOnSfBDzMQCqgYCzWC3pw8aTGFSQum2k9KMWnVaIE4cpigykBDKmHy4kaOW1yzAaCrUNl9N5hDPmKX5pvu0ycx84YLffDYPYWRf1fKTHMQT78wdAv2ZwQYIjyW7RSoB-HNps1ALWP92ms41lKmTKUBledCtAsMIfBC6BWnpO192ny_4H6j3YLCMHvPqa-Sucet3xuOLjGyxZ-jwZHHogDEOR45-mwQdoNKMRBaIrKbm_sEbJKrIuLvUKkfVAHxh-FjLE5tMug_xDVvU7IcTgSjTfVEC-hWrhN3SfU6YUYg8sOaTx82XzFM--DVduA-Zhp6TEIce5ViSqVXgIzmG-3ik3HU6XpCmXlp_qpybPd8wLrs--Ify5Nexda0KgGhQIiCCEtwvVtoSdsjddj_qjqKLo3eLSurVYIs7HgnXbDjA5VooSBZBXSWIGZlGyWOmx5juo46zXX-YI4cZpwDuzGlffZujltwtYkrCFqMLOZXUoMYaTdlqm8EVPDKKKVUWL7e4NTI5NtqDaNnR-5I2lzf-WC00F__0m00)

관계도 설명
* has : 엔티티 A가 엔티티 B를 포함하거나 소유하고 있음을 나타냅니다.
* belongs to :  엔티티 A가 엔티티 B에 속하거나 종속되어 있음을 나타냅니다.
* pays : 엔티티 A가 엔티티 B에 대해 결제를 수행하거나 지불을 한다는 의미입니다.
* contains : 엔티티 A가 엔티티 B를 포함하고 있음을 나타냅니다. 보통 A는 하나 이상의 B를 가질 수 있습니다.

### SettleUp Class Diagram

Below is the class diagram for the Settle_Up Project, illustrating the relationships and structure of the classes within the system.

아래는 Settle_Up 프로젝트의 클래스 다이어그램입니다. 시스템 내 클래스들의 관계와 구조를 보여줍니다.


![Class Diagram](https://www.planttext.com/api/plantuml/png/r5R1Zfim4BtdApWwX_s37ZORtDHI4h84MgcNbHUih2M34PYgKTilww6Vr5yeE92QiKtJgZrqBsjcdeVttZ1sld_zTjyeMgks48jCqAO9ZeggjWa-BOBkxn3ZjoHAZ_fXdSOntYTw7Rt3u_zGByZ-3Ov9WiEdzHQlucbCFyNuCIKnVehnworU9vDOT4Z8ZhmVa27UHKDwaswxObyM0xdLSwDgcgbDBTiAbughyy1qAyjJm7EzFggQTq-d7fIc9BI2Urem6rcBeL96HKWLItZ1WgnctJ9VgG6lpT7uv5nTYboZxCAhJw3b9VL1yZnec28v6XY2n6OaYI1h5x0dZg0IPyLQ820EmDQZCEGHx2ghfK4Jbfx-f0shlJrwa58mMWQysSbIlOXpp2fPgxJAkxT7fSQCrOn2RRUSvCmlGJEo-jJlzlPgJDxSB-BcYtwX1rJfNuiyybo-8wzSm_oG5TBCiu-AbVdSq7Y-NBkYGT5PrMhrhJx_4oF-hWd_eG6EUjvJidyHF2Jx63mZ-zZXHPPM41UK2p3J8SjuGGKfgrP5bUAoDCUodKoavdJI8tVh4AO3PmGqE63cYJdASi2qCWIbEe1QBOBohKF2DW3DkUDuaNdKZeQ1qPpvA1sTdxlLj1fUmIwsHN4UcsHkFvdNnn_UA-FjrBbLMOslq9NJa7gUie8xBa40Mi99D8RmQkJlcEWjvHhALv_lxk2Px0TwGGQ9QSMVYVaDFh2hy0qm0tBVkTN_SFm1003__mC0)

관계도 설명

일반화 (Inheritance)</br>
표현: 실선 + 빈 삼각형 화살표 </br>
뜻: 서브클래스가 슈퍼클래스를 상속받는 관계입니다. </br>
예시: SubClass → SuperClass
</br>
DemoGroupEntity와 GroupEntity는 AbstractGroupEntity를 상속받습니다. </br>
DemoGroupUserEntity와 GroupUserEntity는 AbstractGroupUserEntity를 상속받습니다. </br>
DemoUserEntity와 UserEntity는 AbstractUserEntity를 상속받습니다.


### Abstract 스키마를 사용하여 다른 엔티티와 관계를 맺는 장점
#### 공통 속성 및 동작 재사용
설명: Abstract 스키마는 공통 속성과 동작을 한 곳에 모아서 정의합니다. 이렇게 하면 동일한 기능을 여러 번 작성할 필요가 없어집니다. 예를 들어, AbstractGroupEntity는 모든 그룹에 공통적인 속성(이름, URL 등)을 정의합니다.
#### 다양한 유형의 확장 가능
설명: 새로운 유형의 사용자나 그룹을 추가할 때, Abstract 스키마를 상속받아 쉽게 확장할 수 있습니다. 예를 들어, DemoGroupEntity와 GroupEntity는 모두 AbstractGroupEntity를 상속받아 각각의 특화된 속성을 추가할 수 있습니다.
#### 유지보수 용이성
설명: 공통 기능을 한 곳에 모아두면, 필요한 변경 사항을 중앙에서 관리할 수 있습니다. 예를 들어, 그룹 엔티티의 공통 속성을 변경해야 할 때, AbstractGroupEntity를 수정하면 이를 상속받는 모든 그룹 엔티티에 적용됩니다.

