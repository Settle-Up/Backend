# SettleUp Project - **service-server (backend)**

주요기능에 대한 **시퀀스 다이어그램** 또는 **플로우차트** 를 비롯한 주요 다이어그램과 **엔티티 관계 다이어그램(ERD)** 을 </br>***Diagram file*** 로 업로드하였습니다.<br>
내용이 방대한 점을 고려하여, 혼란을 줄이고 가독성을 높이기 위해 파일들을 readme 파일에 모두 포함시키지 않고 분리하기로 결정했습니다. </br></br>
**코드를 살펴보기시기 전에 이 다이어그램들을 검토하시면 시스템 구조에 대한 더 명확한 이해를 제공해 드릴 수 있습니다**.

아래는 기능들의 상세 목록, 그 기능들의 기능성, 그리고 해당 파일 이름들입니다: 
| No. | Feature Name                | 기능                             | 파일명                           |
|-----|-----------------------------|----------------------------------|----------------------------------|
| 1   | KakaoLogin                  | 카카오 로그인                     | SequenceDiagram - KaKaoLogin.md  |
| 2   | OcrBridge                   | 영수증 이미지 분석                | SequenceDiagram - OcrBridge.md   |
| 3   | create-transaction-receipt  | 영수증을 각 개인의 비용으로 변환 | flowchart-expense.md             |
| 4   | ERD                         | 데이터베이스 구조 설계           | ERD.md           |
| 5   | Email-search                | 이메일 검색                   | email-search.md|


We have uploaded the main API diagrams, including sequence diagrams or flowcharts, as well as Entity Relationship Diagrams (ERD), as individual ***diagram files***. Given the extensive nature of the content, we decided to reduce clutter and enhance readability by not including all files in the readme but separating them instead. Reviewing these diagrams before diving into the code will offer a clearer understanding of the system's structure.

Below is a detailed list of features, their functionalities, and the corresponding file names:

| Feature Name               | Functionality                                | File Name                         |
|----------------------------|----------------------------------------------|-----------------------------------|
| KakaoLogin                 | Kakao Login                                  | SequenceDiagram - KaKaoLogin.md   |
| OcrBridge                  | Analysis of Receipt Images                   | SequenceDiagram - OcrBridge.md    |
| create-transaction-receipt | Conversion of Receipts into Individual Costs | flowchart-expense.md              |
| ERD                        | Database Structure Design                    | ERD.md    |
