package settleup.backend.domain.transaction.service.Impl;

import lombok.AllArgsConstructor;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.springframework.stereotype.Service;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionDetailsEntity;
import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.OptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.dto.GraphResult;
import settleup.backend.domain.transaction.entity.dto.NetDto;
import settleup.backend.domain.transaction.repository.GroupOptimizedTransactionDetailRepository;
import settleup.backend.domain.transaction.repository.GroupOptimizedTransactionRepository;
import settleup.backend.domain.transaction.repository.OptimizedTransactionRepository;
import settleup.backend.domain.transaction.service.GroupOptimizedService;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.global.common.Status;
import settleup.backend.global.common.UUID_Helper;

import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class GroupOptimizedServiceImpl implements GroupOptimizedService {

    private final OptimizedTransactionRepository transactionRepo;
    private final GroupOptimizedTransactionRepository groupOptimizedTransactionRepo;
    private final GroupOptimizedTransactionDetailRepository groupOptimizedDetailRepo;
    private final UserRepository userRepo;
    private final UUID_Helper uuidHelper;

    // p2pList 41,42,43
    // 1. 그래프 만들기 2. DFS 를 돌을 순서 정하기 3.

    @Override
    public void optimizationInGroup(List<Long> p2pList, List<NetDto> net) {
        GraphResult graphResult = createGraphFromTransactions(p2pList);
        List<Long> orderedUserIdList = createGraphOrder(net);
        GroupEntity group = getGroup(p2pList);
        startGroupOptimization(graphResult, orderedUserIdList, group);

    }

    private GroupEntity getGroup(List<Long> p2pList) {
        return transactionRepo.findGroupByTransactionId(p2pList.get(0));
    }

    // 1. orderedUserIdList 를 순서대로  graphResult 의 그래프의 노드의 깊이 탐색을 시작
    // 2. 한번 방문한 에지는 또 방문하지 않도록 방문배열을 활용
    // 3.  DFS 를 돌면서 A->(500)->B->(500)->C 처럼 앞 뒤의 가중치가 같은 노드가 발견되면
    //     A->(500)->C 라는 새로운 노드 만들어서 새로운 sender 와 recipent , amount 를 groupOptimizedTransactionRepo.save
    //    (3). 번에서 만들어진 새로운 노드의 GroupTransaction pk 값 다음 4번 함수로 전해주기
    // 4. 새로운 노드를 만들기 위해 사용되었던 두 노드의 정보를 graph 에서 transaction_id 를 꺼내고 (3번) 에서 받은 GroupTransaction_id 와 , 각자 transaction_id 저장
    //      groupOptimizedDetailRepo.save
    private void startGroupOptimization(GraphResult graphResult,
                                        List<Long> orderedUserIdList,
                                        GroupEntity group) {
        Graph<Long, DefaultWeightedEdge> graph = graphResult.getGraph();
        Map<DefaultWeightedEdge, Long> edgeTransactionIdMap = graphResult.getEdgeTransactionIdMap();
        Set<DefaultWeightedEdge> visitedEdges = new HashSet<>();

        // orderedUserIdList에 따라 각 사용자 ID로부터 DFS 시작
        for (Long userId : orderedUserIdList) {
            //  DFS 탐색 시작
            if (!visitedEdges.contains(userId)) {
                dfs(graph, userId, visitedEdges, edgeTransactionIdMap, group);
            }
        }
    }

    private void dfs(Graph<Long, DefaultWeightedEdge> graph,
                     Long currentNode,
                     Set<DefaultWeightedEdge> visitedEdges,
                     Map<DefaultWeightedEdge, Long> edgeTransactionIdMap,
                     GroupEntity group) {
        // 현재 노드에서 출발하는 모든 에지에 대해서 반복
        for (DefaultWeightedEdge edge : graph.outgoingEdgesOf(currentNode)) {
            if (visitedEdges.contains(edge)) continue; // 이미 방문한 에지는 패스

            Long targetNode = graph.getEdgeTarget(edge);
            double weight = graph.getEdgeWeight(edge);

            // 방문 에지를 방문 처리
            visitedEdges.add(edge);

            // 다음 노드에서 출발하는 에지를 탐색, 조건에 맞는 경로를 찾음
            for (DefaultWeightedEdge nextEdge : graph.outgoingEdgesOf(targetNode)) {
                if (visitedEdges.contains(nextEdge)) continue; // 이미 방문한 에지는 패스

                double nextWeight = graph.getEdgeWeight(nextEdge);
                Long nextTarget = graph.getEdgeTarget(nextEdge);

                // 앞뒤 에지의 가중치가 같은 경우, 새로운 최적화된 트랜잭션을 생성
                if (weight == nextWeight) {
                    GroupOptimizedTransactionEntity newTransaction =
                            createOptimizedTransaction(currentNode, nextTarget, weight, group);
                    saveTransactionDetails(newTransaction, edgeTransactionIdMap.get(edge));
                    saveTransactionDetails(newTransaction, edgeTransactionIdMap.get(nextEdge));

                    // 다음 타겟 노드에서 DFS 계속 진행
                    dfs(graph, nextTarget, visitedEdges, edgeTransactionIdMap, group);
                }
            }
        }
    }


    // 최적화 거래를 생성하고 저장
    private GroupOptimizedTransactionEntity createOptimizedTransaction(Long senderId, Long recipientId, double amount, GroupEntity group) {
        GroupOptimizedTransactionEntity newTransaction = new GroupOptimizedTransactionEntity();
        newTransaction.setGroupOptimizedTransactionUUID(uuidHelper.UUIDForGroupOptimizedTransactions());
        newTransaction.setSenderUser(userRepo.findById(senderId).get());
        newTransaction.setRecipientUser(userRepo.findById(recipientId).get());
        newTransaction.setGroup(group);
        newTransaction.setOptimizedAmount(amount);
        newTransaction.setIsCleared(Status.PENDING);
        newTransaction.setCreatedAt(LocalDateTime.now());
        groupOptimizedTransactionRepo.save(newTransaction);
        return newTransaction;
    }

    // 거래 세부 정보를 저장
    private void saveTransactionDetails(GroupOptimizedTransactionEntity optimizedTransaction, Long transactionId) {
        GroupOptimizedTransactionDetailsEntity details = new GroupOptimizedTransactionDetailsEntity();
        details.setGroupOptimizedTransaction(optimizedTransaction);
        details.setGroupOptimizedTransactionDetailUUID(uuidHelper.UUIDForGroupOptimizedDetail());
        details.setOptimizedTransaction(transactionRepo.findById(transactionId).get());
        groupOptimizedDetailRepo.save(details);
    }


    public GraphResult createGraphFromTransactions(List<Long> p2pList) {
        Graph<Long, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Map<DefaultWeightedEdge, Long> edgeTransactionIdMap = new HashMap<>();

        for (Long transactionId : p2pList) {
            Optional<OptimizedTransactionEntity> transactionOpt = transactionRepo.findById(transactionId);
            transactionOpt.ifPresent(transaction -> {
                Long senderId = transaction.getSenderUser().getId();
                Long recipientId = transaction.getRecipientUser().getId();
                double amount = transaction.getTransactionAmount();

                graph.addVertex(senderId);
                graph.addVertex(recipientId);

                DefaultWeightedEdge edge = graph.addEdge(senderId, recipientId);
                if (edge != null) {
                    graph.setEdgeWeight(edge, amount);
                    // 에지와 transactionId를 매핑
                    edgeTransactionIdMap.put(edge, transactionId);
                }
            });
        }

        return new GraphResult(graph, edgeTransactionIdMap);
    }


    private List createGraphOrder(List<NetDto> net) {
        List<NetDto> sortedNetList = new ArrayList<>(net);

        // Bubble sort
        boolean sorted = false;
        while (!sorted) {
            sorted = true;
            for (int i = 0; i < sortedNetList.size() - 1; i++) {
                NetDto current = sortedNetList.get(i);
                NetDto next = sortedNetList.get(i + 1);
                if (current.getNetAmount() < next.getNetAmount() ||
                        (current.getNetAmount() == next.getNetAmount() && current.getUser().getId() > next.getUser().getId())) {
                    // swap
                    sortedNetList.set(i, next);
                    sortedNetList.set(i + 1, current);
                    sorted = false;
                }
            }
        }

        // 정렬된 리스트에서 userId만 추출
        List<Long> orderedUserIdList = new ArrayList<>();
        for (NetDto dto : sortedNetList) {
            orderedUserIdList.add(dto.getUser().getId());
        }

        return orderedUserIdList;
    }

}
