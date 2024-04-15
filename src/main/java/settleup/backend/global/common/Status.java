package settleup.backend.global.common;

/**
 *  상태관리 경우의 수 정리
 *
 *  requireTransaction 은 상속 개념 에서 상속클리어
 *  INHERITED_CLEAR => 상위 최적화에서 hasBeenSent 발생 , Or 합계 0
 *
 *  REQUIRE_OPTIMIZED => 기본값
 *
 *  -------------------------------------------
 *  1.INHERITED_CLEAR, => 상위 최적화에서 hasBeenSent 발생 , Or 합계 0
 *  2.INHERITED ,  => 상위 최적화가 만들어지는데 쓰임 (겹쳐짐)
 *  3.REQUIRE_REFLECT, => 상위 최적화가 만들어지는에 안쓰임
 *  4.REQUIRE_OPTIMIZED => only RequireTransaction 을 위한 default
 *
 *
 *  ** 영수증이 등록 될 때
 *    (USED/ NOT_USED) 영수증이 등록 될 때 마다
 *    A . requireTransaction 등록  RequiredReflection => REQUIRE_REFLECT
 *    B . (A 의 <>Inherited_clear ) 리스트들을 optimizedTransaction 진행 => 생성된 newTransaction => Not_Used , Require_Reflect +( hasBeenSent,hasBeenChecked =>false)
 *    C . groupOptimized 진행 => 1 . 생성된 newTransaction => Not_Used , Require_Reflect + ( hasBeenSent,hasBeenChecked =>false)
 *                              2 . groupOptimizedDetail 에 흡수된 1차 최적화 B => inherited 바꾸기
 *    D . B 와 C 를 기반으로 ultimate 진행 => 1. 생성된 newTransaction => Not_Used , Require_Reflect + ( hasBeenSent,hasBeenChecked => false)
 *                                       2.  ultimateOptimizedDetail 에 흡수된 2차 최적화 , 1차 최적화  => inherited 로 바꾸기
 *
 *  ** sender가 돈을 보냈을 때
 *  인자 값 : transactionId , sender id
 *  => 해당 transaction 의 hasBeenSent => true 로 바꿈
 *
 *  ** recepient가 확인을 했을 때
 *  인자 값 : transactionId , receipent Id
 *  exception transaction 이 hasBeenSent 가 true 상태여야함
 *  => 해당 transaction 의 hasBeenChecked 가 true 여야함
 *
 *  ** 돈을 보냈다는 알림 (endpoint :/notifications/transactions/received)
 *  조회 대상 : user 가 receipent 일때 + hasBeenChecked 가 false 인 경우를 불러옴
 *
 *  ** details retrieved
 *     A => neededTransaction :
 *                             transactionalEntity를 통해서 RequireReflection 이 Require_Reflect ,hasBeenSent => false 인 경우만 불러온다
 *                             RequireReflection 이 Require_Reflection 이면서 hasBeenSent 인경우 => 일주일 내 라면 last transactionList
 *
 */

public enum Status {
 PREVIOUS, CURRENT ,OWE, OWED, INHERITED_CLEAR, INHERITED , REQUIRE_REFLECT, REQUIRE_OPTIMIZED;
}
