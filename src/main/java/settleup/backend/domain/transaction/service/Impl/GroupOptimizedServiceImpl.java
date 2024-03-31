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

import settleup.backend.domain.transaction.entity.dto.*;
import settleup.backend.domain.transaction.repository.GroupOptimizedTransactionDetailRepository;
import settleup.backend.domain.transaction.repository.GroupOptimizedTransactionRepository;
import settleup.backend.domain.transaction.repository.OptimizedTransactionRepository;
import settleup.backend.domain.transaction.service.GroupOptimizedService;
import settleup.backend.domain.transaction.service.TransactionInheritanceService;

import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.global.common.Status;
import settleup.backend.global.common.UUID_Helper;

import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class GroupOptimizedServiceImpl implements GroupOptimizedService {

    private final OptimizedTransactionRepository optimizedTransactionRepo;
    private final GroupOptimizedTransactionRepository groupOptimizedTransactionRepo;
    private final GroupOptimizedTransactionDetailRepository groupOptimizedDetailRepo;
    private final UserRepository userRepo;
    private final UUID_Helper uuidHelper;
    private final TransactionInheritanceService transactionInheritanceService;


    // p2pList 41,42,43 p2pList 방금 만들어진 1차 최적화 id 값 ,
    // 2차 최적화는 왜 ? status 를 고려하지 않는가 ?
    // -> 방금만들어진 영수증을 기반 하는거라 status 무의미 하다고 생각 (새로 영수증이 만들어지는 시기 = 1차 최적화 = 2차 최적화 )
    // 3차는 1차 2차 기반 status 고려 할 필요 없는가 .? => 같은 시기에 이루어지기 때문에 필요 없다고 생각이 된다
    // 1차에서 status 고려하면 충분하다고 생각
    // 2차 최적화만 따로 진행된다면 status를 고려해야함 , 단 1차와 2차가 연쇄적으로 이루어지기 때문에 2차의 대상이 되는 1차를 불러올때 고려할 필요없다
    // 무조건적으로 status 는 pending 이기 때문에
    private static final Logger logger = LoggerFactory.getLogger(GroupOptimizedServiceImpl.class);

    @Override
    public void optimizationInGroup(TransactionP2PResultDto resultDto, List<NetDto> net) {
        logger.debug("Starting optimizationInGroup with p2pList: {}, netList size: {}", resultDto.getP2pList(), net.size());
        GraphResult graphResult = createGraphFromTransactions(resultDto.getP2pList());
        List<Long> orderedUserIdList = createGraphOrder(net);
        GroupEntity group = getGroup(resultDto.getP2pList());
        logger.debug("Graph created. Starting group optimization for group: {}", group.getId());
        startGroupOptimization(graphResult, orderedUserIdList, group);
        logger.debug("Group optimization completed for group: {}", group.getId());

    }

    private GroupEntity getGroup(List<Long> p2pList) {
        logger.debug("Retrieving group for p2pList first element: {}", p2pList.get(0));
        return optimizedTransactionRepo.findGroupByTransactionId(p2pList.get(0));
    }


    private void startGroupOptimization(GraphResult graphResult,
                                        List<Long> orderedUserIdList,
                                        GroupEntity group) {
        logger.debug("Starting group optimization. User list size: {}", orderedUserIdList.size());
        groupOptimizedTransactionRepo.updateIsUsedStatusByGroup(group, Status.USED);
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
        // dfs 로직 변경
        // 에지를 끝까지 돌고 가중치가 같은 에지를 기록 해서 첫 번째랑 마지막을 이어야함
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
        newTransaction.setIsSenderStatus(Status.PENDING);
        newTransaction.setIsRecipientStatus(Status.PENDING);
        newTransaction.setIsInheritanceStatus(Status.PENDING);
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
        details.setOptimizedTransaction(optimizedTransactionRepo.findById(transactionId).get());
        logger.debug("Saved transaction detail: {}, for optimized transaction UUID: {}, based on original transaction ID: {}",
                details.getTransactionDetailUUID(), optimizedTransaction.getTransactionUUID(), transactionId);

        groupOptimizedDetailRepo.save(details);
    }


    public GraphResult createGraphFromTransactions(List<Long> p2pList) {
        Graph<Long, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Map<DefaultWeightedEdge, Long> edgeTransactionIdMap = new HashMap<>();

        for (Long transactionId : p2pList) {
            Optional<OptimizedTransactionEntity> transactionOpt = optimizedTransactionRepo.findById(transactionId);
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
        // 정렬 조건 정의
        Collections.sort(net, (o1, o2) -> {
            // 음수 금액을 고려한 비교
            if (o1.getNetAmount() < 0 && o2.getNetAmount() < 0) {
                // 음수일 때는 큰 금액(절대값이 작은)이 앞으로 오도록, 즉 o2에서 o1을 뺀 결과를 반환
                return Float.compare(o2.getNetAmount(), o1.getNetAmount());
            } else if (Float.compare(o1.getNetAmount(), o2.getNetAmount()) == 0) {
                // 금액이 같을 때는 userId로 정렬
                return Long.compare(o1.getUser().getId(), o2.getUser().getId());
            }
            // 금액이 양수일 때는 작은 금액이 앞으로 오도록, 즉 o1에서 o2를 뺀 결과를 반환
            return Float.compare(o1.getNetAmount(), o2.getNetAmount());
        });

        // Extracting userIds from the sorted list
        List<Long> orderedUserIdList = new ArrayList<>();
        for (NetDto dto : net) {
            orderedUserIdList.add(dto.getUser().getId());
        }

        return orderedUserIdList;
    }

    @Override
    public String processTransaction(String transactionId, TransactionUpdateRequestDto request, GroupEntity existingGroup) throws CustomException {
        GroupOptimizedTransactionEntity transactionEntity = groupOptimizedTransactionRepo.findByTransactionUUID(transactionId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_ID_NOT_FOUND_IN_GROUP));

        if (!transactionEntity.getGroup().getId().equals(existingGroup.getId())) {
            throw new CustomException(ErrorCode.TRANSACTION_ID_NOT_FOUND_IN_GROUP);
        }

        Status statusToUpdate = Status.valueOf(request.getApprovalStatus());

        if ("sender".equals(request.getApprovalUser())) {
            groupOptimizedTransactionRepo.updateIsSenderStatusByUUID(transactionId, statusToUpdate);

        } else {
            groupOptimizedTransactionRepo.updateIsRecipientStatusByUUID(transactionId, statusToUpdate);

        }

        Optional<GroupOptimizedTransactionEntity> bothSideClearTransaction = groupOptimizedTransactionRepo.findByTransactionUUID(transactionId);
        if (bothSideClearTransaction.isPresent()) {
            GroupOptimizedTransactionEntity transaction = bothSideClearTransaction.get();
            if (transaction.getIsSenderStatus() == Status.CLEAR && transaction.getIsRecipientStatus() == Status.CLEAR) {
                List<GroupOptimizedTransactionDetailsEntity> secondInheritanceTargetList =
                        groupOptimizedDetailRepo.findByGroupOptimizedTransactionId(transaction.getId());
                for (GroupOptimizedTransactionDetailsEntity secondInheritanceTarget : secondInheritanceTargetList) {
                    transactionInheritanceService.clearInheritanceStatusForOptimizedToRequired(secondInheritanceTarget.getOptimizedTransaction().getId());
                }
            }
        }

        return transactionEntity.getTransactionUUID();
    }
    }
