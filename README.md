# Settle_Up Project

## service-server (backend)

주요 api 들은 시퀀스다이어그램 또는 플로워차트와
엔티티관계 다이어그램(ERD)를 md 파일로 올려두었습니다 
내용이 많아 readme에 한번에 올리기에는 가독성이 떨어질것을 우려해 따로 파일화 했습니다
code를 보시기전에 보시면 도움이 되실 것 같습니다

아래는 자세한 설명을 포함하고 있는 feature명 와 기능, 파일명입니다 
| No. | Feature Name                | 기능                             | 파일명                           |
|-----|-----------------------------|----------------------------------|----------------------------------|
| 1   | KakaoLogin                  | 카카오 로그인                     | SequenceDiagram - KaKaoLogin.md  |
| 2   | OcrBridge                   | 영수증 이미지 분석                | SequenceDiagram - OcrBridge.md   |
| 3   | create-transaction-receipt  | 영수증을 각 개인의 비용으로 변환 | flowchart-expense.md             |
| 4   | ERD                         | 데이터베이스 구조 설계           | ERD.md           |



To enhance readability and organization, we have uploaded the main API diagrams, including sequence diagrams or flowcharts, along with Entity Relationship Diagrams (ERD), as individual markdown (md) files. Considering the extensive content, we opted to separate these files rather than compiling them all into the readme file to avoid clutter and enhance readability. Reviewing these diagrams before diving into the code could provide valuable insights and a clearer understanding of the system's architecture.

Below is a detailed list of features, their functionalities, and the corresponding file names:

| Feature Name               | Functionality                                | File Name                         |
|----------------------------|----------------------------------------------|-----------------------------------|
| KakaoLogin                 | Kakao Login                                  | SequenceDiagram - KaKaoLogin.md   |
| OcrBridge                  | Analysis of Receipt Images                   | SequenceDiagram - OcrBridge.md    |
| create-transaction-receipt | Conversion of Receipts into Individual Costs | flowchart-expense.md              |
| ERD                        | Database Structure Design                    | (Please insert the file name)     |
