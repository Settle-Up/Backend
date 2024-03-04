# Settle_Up Project

## service-server (backend)

API 다이어그램, 시퀀스 다이어그램 또는 플로우차트를 비롯한 주요 다이어그램과 엔티티 관계 다이어그램(ERD)을 개별 마크다운(md) 파일로 업로드하여 가독성과 조직성을 향상시켰습니다.
내용이 방대한 점을 고려하여, 혼란을 줄이고 가독성을 높이기 위해 파일들을 readme 파일에 모두 포함시키지 않고 분리하기로 결정했습니다. 
코드를 살펴보기 전에 이 다이어그램들을 검토하면 시스템 구조에 대한 더 명확한 이해를 제공해 드릴 수 있습니다.

아래는 기능들의 상세 목록, 그 기능들의 기능성, 그리고 해당 파일 이름들입니다: 
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
