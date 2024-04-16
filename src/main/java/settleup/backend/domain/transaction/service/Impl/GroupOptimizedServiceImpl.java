package settleup.backend.domain.transaction.service.Impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionDetailsEntity;
import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionEntity;
import settleup.backend.domain.transaction.entity.OptimizedTransactionEntity;

import settleup.backend.domain.transaction.entity.TransactionalEntity;
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
@Transactional
public class GroupOptimizedServiceImpl implements GroupOptimizedService {

    private final OptimizedTransactionRepository optimizedTransactionRepo;
    private final GroupOptimizedTransactionRepository groupOptimizedTransactionRepo;
    private final GroupOptimizedTransactionDetailRepository groupOptimizedDetailRepo;
    private final UserRepository userRepo;
    private final UUID_Helper uuidHelper;
    private final TransactionInheritanceService transactionInheritanceService;

    private static final Logger logger = LoggerFactory.getLogger(GroupOptimizedServiceImpl.class);

    @Override
    public boolean optimizationInGroup(TransactionP2PResultDto resultDto, List<NetDto> net) {
        GraphResult graphResult = createGraphFromTransactions(resultDto.getOptimiziationByPeerToPeerList());
        List<Long> orderedUserIdList = createGraphOrder(net);
        GroupEntity group = getGroup(resultDto.getOptimiziationByPeerToPeerList());
        logger.debug("Graph created. Starting group optimization for group: {}", group.getId());

        boolean transactionCreated = startGroupOptimization(graphResult, orderedUserIdList, group);
        logger.debug("Group optimization completed for group: {}", group.getId());
        return transactionCreated;
    }

    private GroupEntity getGroup(List<Long> p2pList) {
        logger.debug("Retrieving group for p2pList first element: {}", p2pList.get(0));
        return optimizedTransactionRepo.findGroupByTransactionId(p2pList.get(0));
    }


    private boolean startGroupOptimization(GraphResult graphResult,
                                           List<Long> orderedUserIdList,
                                           GroupEntity group) {
        logger.debug("Starting group optimization. User list size: {}", orderedUserIdList.size());
        groupOptimizedTransactionRepo.updateOptimizationStatusByGroup(group, Status.PREVIOUS);
        Graph<Long, DefaultWeightedEdge> graph = graphResult.getGraph();
        Map<DefaultWeightedEdge, Long> edgeTransactionIdMap = graphResult.getEdgeTransactionIdMap();
        Set<DefaultWeightedEdge> visitedEdges = new HashSet<>();
        boolean transactionCreated = false;

        for (Long userId : orderedUserIdList) {
            if (!visitedEdges.containsAll(graph.outgoingEdgesOf(userId))) {
                for (DefaultWeightedEdge edge : graph.outgoingEdgesOf(userId)) {
                    if (!visitedEdges.contains(edge)) {
                        double initialWeight = graph.getEdgeWeight(edge);
                        Long targetNode = graph.getEdgeTarget(edge);
                        if (dfs(graph, targetNode, visitedEdges, edgeTransactionIdMap, group, userId, initialWeight)) {
                            transactionCreated = true;
                        }
                    }
                }
            }
        }
        return transactionCreated;
    }

    private boolean dfs(Graph<Long, DefaultWeightedEdge> graph,
                        Long currentNode,
                        Set<DefaultWeightedEdge> visitedEdges,
                        Map<DefaultWeightedEdge, Long> edgeTransactionIdMap,
                        GroupEntity group,
                        Long startNode,
                        double lastWeight) {
        logger.debug("DFS started for node: {}", currentNode);
        boolean transactionCreated = false;
        for (DefaultWeightedEdge edge : graph.outgoingEdgesOf(currentNode)) {
            if (visitedEdges.contains(edge)) continue;

            visitedEdges.add(edge);
            Long targetNode = graph.getEdgeTarget(edge);
            double currentWeight = graph.getEdgeWeight(edge);


            if (currentWeight == lastWeight && !targetNode.equals(startNode)) {
                transactionCreated |= dfs(graph, targetNode, visitedEdges, edgeTransactionIdMap, group, startNode, lastWeight);
            } else {
                if (startNode != null && startNode != currentNode) {
                    GroupOptimizedTransactionEntity newTransaction = createOptimizedTransaction(startNode, currentNode, lastWeight, group);
                    saveTransactionDetails(newTransaction, edgeTransactionIdMap, startNode, currentNode, graph);
                    transactionCreated = true;
                }
                transactionCreated |= dfs(graph, targetNode, new HashSet<>(visitedEdges), edgeTransactionIdMap, group, currentNode, currentWeight);  // 새로운 visitedEdges 세트와 함께 호출
            }
        }
        return transactionCreated;
    }



    private GroupOptimizedTransactionEntity createOptimizedTransaction(Long senderId, Long recipientId, double amount, GroupEntity group) {
        GroupOptimizedTransactionEntity newTransaction = new GroupOptimizedTransactionEntity();
        newTransaction.setTransactionUUID(uuidHelper.UUIDForGroupOptimizedTransactions());
        newTransaction.setSenderUser(userRepo.findById(senderId).get());
        newTransaction.setRecipientUser(userRepo.findById(recipientId).get());
        newTransaction.setGroup(group);
        newTransaction.setTransactionAmount(amount);
        newTransaction.setHasBeenSent(false);
        newTransaction.setHasBeenChecked(false);
        newTransaction.setRequiredReflection(Status.REQUIRE_REFLECT);
        newTransaction.setCreatedAt(LocalDateTime.now());
        newTransaction.setOptimizationStatus(Status.CURRENT);
        groupOptimizedTransactionRepo.save(newTransaction);
        logger.debug("Created and saved new optimized transaction: {}, Sender ID: {}, Recipient ID: {}, Amount: {}",
                newTransaction.getTransactionUUID(), senderId, recipientId, amount);

        return newTransaction;
    }


    private void saveTransactionDetails(GroupOptimizedTransactionEntity optimizedTransaction, Map<DefaultWeightedEdge, Long> edgeTransactionIdMap, Long startNode, Long endNode, Graph<Long, DefaultWeightedEdge> graph) {
        DefaultWeightedEdge edge = graph.getEdge(startNode, endNode);
        while (startNode != endNode) {
            Long transactionId = edgeTransactionIdMap.get(edge);
            GroupOptimizedTransactionDetailsEntity details = new GroupOptimizedTransactionDetailsEntity();
            details.setGroupOptimizedTransaction(optimizedTransaction);
            details.setTransactionDetailUUID(uuidHelper.UUIDForGroupOptimizedDetail());
            details.setOptimizedTransaction(optimizedTransactionRepo.findById(transactionId).orElseThrow());
            updateReflection(optimizedTransactionRepo.findById(transactionId).orElseThrow());
            groupOptimizedDetailRepo.save(details);
            logger.debug("Saved transaction detail: {}, for optimized transaction UUID: {}, based on original transaction ID: {}",
                    details.getTransactionDetailUUID(), optimizedTransaction.getTransactionUUID(), transactionId);
            startNode = graph.getEdgeTarget(edge);
            edge = graph.getEdge(startNode, endNode);
        }
    }

    private void updateReflection(OptimizedTransactionEntity optimizedTransactionEntity) {
        optimizedTransactionRepo.updateRequiredReflectionByTransactionUUID(optimizedTransactionEntity.getTransactionUUID(), Status.INHERITED);
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
                    edgeTransactionIdMap.put(edge, transactionId);
                }
            });
        }

        return new GraphResult(graph, edgeTransactionIdMap);
    }


    private List<Long> createGraphOrder(List<NetDto> net) {
        Collections.sort(net, (o1, o2) -> {

            if (o1.getNetAmount() < 0 && o2.getNetAmount() < 0) {

                return Float.compare(o2.getNetAmount(), o1.getNetAmount());
            } else if (Float.compare(o1.getNetAmount(), o2.getNetAmount()) == 0) {

                return Long.compare(o1.getUser().getId(), o2.getUser().getId());
            }

            return Float.compare(o1.getNetAmount(), o2.getNetAmount());
        });


        List<Long> orderedUserIdList = new ArrayList<>();
        for (NetDto dto : net) {
            orderedUserIdList.add(dto.getUser().getId());
        }

        return orderedUserIdList;
    }

    @Override
    @Transactional
    public TransactionalEntity processTransaction(TransactionUpdateRequestDto request, GroupEntity existingGroup) throws CustomException {
        logger.info("Processing transaction with ID: {}", request.getTransactionId());
        GroupOptimizedTransactionEntity transactionEntity = groupOptimizedTransactionRepo.findByTransactionUUID(request.getTransactionId())
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_ID_NOT_FOUND_IN_GROUP));

        if (!transactionEntity.getGroup().getId().equals(existingGroup.getId())) {
            throw new CustomException(ErrorCode.TRANSACTION_ID_NOT_FOUND_IN_GROUP);
        }

        List<GroupOptimizedTransactionDetailsEntity> requireInheritanceList=
                groupOptimizedDetailRepo.findByGroupOptimizedTransactionId(groupOptimizedTransactionRepo.findByTransactionUUID(transactionEntity.getTransactionUUID()).get().getId());
        for(GroupOptimizedTransactionDetailsEntity requireInheritanceTransaction:requireInheritanceList){
            transactionInheritanceService.clearInheritanceStatusFromGroupToOptimized(requireInheritanceTransaction.getId());
        }

        return transactionEntity;
    }
}
