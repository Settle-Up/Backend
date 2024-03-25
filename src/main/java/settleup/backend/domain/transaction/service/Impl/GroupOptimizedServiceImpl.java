package settleup.backend.domain.transaction.service.Impl;

import lombok.AllArgsConstructor;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionDetailsEntity;
import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.OptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.dto.GraphResult;
import settleup.backend.domain.transaction.entity.dto.NetDto;
import settleup.backend.domain.transaction.entity.dto.TransactionP2PResultDto;
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
    private static final Logger logger = LoggerFactory.getLogger(GroupOptimizedServiceImpl.class);

    @Override
    public void optimizationInGroup(TransactionP2PResultDto resultDto, List<NetDto> net) {
        logger.debug("Starting optimizationInGroup with p2pList: {}, netList size: {}",resultDto.getP2pList(), net.size());
        GraphResult graphResult = createGraphFromTransactions(resultDto.getP2pList());
        List<Long> orderedUserIdList = createGraphOrder(net);
        GroupEntity group = getGroup(resultDto.getP2pList());
        logger.debug("Graph created. Starting group optimization for group: {}", group.getId());
        startGroupOptimization(graphResult, orderedUserIdList, group);
        logger.debug("Group optimization completed for group: {}", group.getId());

    }

    private GroupEntity getGroup(List<Long> p2pList) {
        logger.debug("Retrieving group for p2pList first element: {}", p2pList.get(0));
        return transactionRepo.findGroupByTransactionId(p2pList.get(0));
    }


    private void startGroupOptimization(GraphResult graphResult,
                                        List<Long> orderedUserIdList,
                                        GroupEntity group) {
        logger.debug("Starting group optimization. User list size: {}", orderedUserIdList.size());
        groupOptimizedTransactionRepo.updateIsUsedStatusByGroup(group,Status.USED);
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
        logger.debug("DFS started for node: {}", currentNode);
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
        newTransaction.setTransactionUUID(uuidHelper.UUIDForGroupOptimizedTransactions());
        newTransaction.setSenderUser(userRepo.findById(senderId).get());
        newTransaction.setRecipientUser(userRepo.findById(recipientId).get());
        newTransaction.setGroup(group);
        newTransaction.setTransactionAmount(amount);
        newTransaction.setIsCleared(Status.PENDING);
        newTransaction.setCreatedAt(LocalDateTime.now());
        newTransaction.setIsUsed(Status.NOT_USED);
        groupOptimizedTransactionRepo.save(newTransaction);
        logger.debug("Created and saved new optimized transaction: {}, Sender ID: {}, Recipient ID: {}, Amount: {}",
                newTransaction.getTransactionUUID(), senderId, recipientId, amount);

        return newTransaction;
    }

    // 거래 세부 정보를 저장
    private void saveTransactionDetails(GroupOptimizedTransactionEntity optimizedTransaction, Long transactionId) {
        GroupOptimizedTransactionDetailsEntity details = new GroupOptimizedTransactionDetailsEntity();
        details.setGroupOptimizedTransaction(optimizedTransaction);
        details.setTransactionDetailUUID(uuidHelper.UUIDForGroupOptimizedDetail());
        details.setOptimizedTransaction(transactionRepo.findById(transactionId).get());
        logger.debug("Saved transaction detail: {}, for optimized transaction UUID: {}, based on original transaction ID: {}",
                details.getTransactionDetailUUID(), optimizedTransaction.getTransactionUUID(), transactionId);

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

    private List<Long> createGraphOrder(List<NetDto> net) {
        List<NetDto> sortedNetList = new ArrayList<>(net);

        // Bubble sort for ascending order by netAmount
        boolean sorted = false;
        while (!sorted) {
            sorted = true;
            for (int i = 0; i < sortedNetList.size() - 1; i++) {
                NetDto current = sortedNetList.get(i);
                NetDto next = sortedNetList.get(i + 1);
                // Change the condition for ascending order
                if (current.getNetAmount() > next.getNetAmount() ||
                        (current.getNetAmount() == next.getNetAmount() && current.getUser().getId() > next.getUser().getId())) {
                    // swap
                    sortedNetList.set(i, next);
                    sortedNetList.set(i + 1, current);
                    sorted = false;
                }
            }
        }

        // Extracting userIds from the sorted list
        List<Long> orderedUserIdList = new ArrayList<>();
        for (NetDto dto : sortedNetList) {
            orderedUserIdList.add(dto.getUser().getId());
        }

        return orderedUserIdList;
    }
}
