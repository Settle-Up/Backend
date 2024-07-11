//package settleup.backend.domain.transaction.service.Impl;
//
//import lombok.AllArgsConstructor;
//import org.jgrapht.Graph;
//import org.jgrapht.graph.DefaultDirectedWeightedGraph;
//import org.jgrapht.graph.DefaultWeightedEdge;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import settleup.backend.domain.group.entity.AbstractGroupEntity;
//import settleup.backend.domain.group.entity.GroupEntity;
//import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionDetailsEntity;
//import settleup.backend.domain.transaction.entity.GroupOptimizedTransactionEntity;
//import settleup.backend.domain.transaction.entity.OptimizedTransactionEntity;
//import settleup.backend.domain.transaction.entity.TransactionalEntity;
//import settleup.backend.domain.transaction.entity.dto.*;
//import settleup.backend.domain.transaction.repository.GroupOptimizedTransactionDetailRepository;
//import settleup.backend.domain.transaction.repository.GroupOptimizedTransactionRepository;
//import settleup.backend.domain.transaction.repository.OptimizedTransactionRepository;
//import settleup.backend.domain.transaction.service.GroupOptimizedService;
//import settleup.backend.domain.transaction.service.TransactionInheritanceService;
//import settleup.backend.domain.user.repository.UserRepository;
//import settleup.backend.global.Helper.Status;
//import settleup.backend.global.Helper.UUID_Helper;
//import settleup.backend.global.exception.CustomException;
//import settleup.backend.global.exception.ErrorCode;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.*;
//
//@Service
//@AllArgsConstructor
//@Transactional
//public class GroupOptimizedServiceImpl implements GroupOptimizedService {
//
//    private final OptimizedTransactionRepository optimizedTransactionRepo;
//    private final GroupOptimizedTransactionRepository groupOptimizedTransactionRepo;
//    private final GroupOptimizedTransactionDetailRepository groupOptimizedDetailRepo;
//    private final UserRepository userRepo;
//    private final UUID_Helper uuidHelper;
//    private final TransactionInheritanceService transactionInheritanceService;
//
//    private static final Logger logger = LoggerFactory.getLogger(GroupOptimizedServiceImpl.class);
//
//    @Override
//    public boolean optimizationInGroup(TransactionP2PResultDto resultDto, List<NetDto> net) {
//        GraphResult graphResult = createGraphFromTransactions(resultDto.getOptimiziationByPeerToPeerList());
//        List<Long> orderedUserIdList = createGraphOrder(net);
//        AbstractGroupEntity group = getGroup(resultDto.getOptimiziationByPeerToPeerList());
//        logger.debug("Graph created. Starting group optimization for group: {}", group.getId());
//
//        boolean transactionCreated = startGroupOptimization(graphResult, orderedUserIdList, group);
//        logger.debug("Group optimization completed for group: {}", group.getId());
//        logger.info("optimizationInGroup OK");
//        return transactionCreated;
//    }
//
//    private AbstractGroupEntity getGroup(List<Long> p2pList) {
//        logger.debug("Retrieving group for p2pList first element: {}", p2pList.get(0));
//        return optimizedTransactionRepo.findGroupByTransactionId(p2pList.get(0));
//    }
//
//    private boolean startGroupOptimization(GraphResult graphResult,
//                                           List<Long> orderedUserIdList,
//                                           AbstractGroupEntity group) {
//        logger.debug("Starting group optimization. User list size: {}", orderedUserIdList.size());
//        groupOptimizedTransactionRepo.updateOptimizationStatusByGroup(group, Status.PREVIOUS);
//        Graph<Long, DefaultWeightedEdge> graph = graphResult.getGraph();
//        Map<DefaultWeightedEdge, Long> edgeTransactionIdMap = graphResult.getEdgeTransactionIdMap();
//        Set<DefaultWeightedEdge> visitedEdges = new HashSet<>();
//        boolean transactionCreated = false;
//
//        for (Long userId : orderedUserIdList) {
//            if (!visitedEdges.containsAll(graph.outgoingEdgesOf(userId))) {
//                for (DefaultWeightedEdge edge : graph.outgoingEdgesOf(userId)) {
//                    if (!visitedEdges.contains(edge)) {
//                        BigDecimal initialWeight = BigDecimal.valueOf(graph.getEdgeWeight(edge));
//                        Long targetNode = graph.getEdgeTarget(edge);
//                        if (dfs(graph, targetNode, visitedEdges, edgeTransactionIdMap, group, userId, initialWeight)) {
//                            transactionCreated = true;
//                        }
//                    }
//                }
//            }
//        }
//        logger.info("startGroupOptimization OK");
//        return transactionCreated;
//    }
//
//    private boolean dfs(Graph<Long, DefaultWeightedEdge> graph,
//                        Long currentNode,
//                        Set<DefaultWeightedEdge> visitedEdges,
//                        Map<DefaultWeightedEdge, Long> edgeTransactionIdMap,
//                        AbstractGroupEntity group,
//                        Long startNode,
//                        BigDecimal lastWeight) {
//        logger.debug("DFS started for node: {}", currentNode);
//        boolean transactionCreated = false;
//        for (DefaultWeightedEdge edge : graph.outgoingEdgesOf(currentNode)) {
//            if (visitedEdges.contains(edge)) continue;
//
//            visitedEdges.add(edge);
//            Long targetNode = graph.getEdgeTarget(edge);
//            BigDecimal currentWeight = BigDecimal.valueOf(graph.getEdgeWeight(edge));
//
//            if (currentWeight.compareTo(lastWeight) == 0 && !targetNode.equals(startNode)) {
//                transactionCreated |= dfs(graph, targetNode, visitedEdges, edgeTransactionIdMap, group, startNode, lastWeight);
//            } else {
//                if (startNode != null && !startNode.equals(currentNode)) {
//                    GroupOptimizedTransactionEntity newTransaction = createOptimizedTransaction(startNode, currentNode, lastWeight, group);
//                    saveTransactionDetails(newTransaction, edgeTransactionIdMap, startNode, currentNode, graph);
//                    transactionCreated = true;
//                }
//                transactionCreated |= dfs(graph, targetNode, new HashSet<>(visitedEdges), edgeTransactionIdMap, group, currentNode, currentWeight);
//            }
//        }
//        return transactionCreated;
//    }
//
//    private GroupOptimizedTransactionEntity createOptimizedTransaction(Long senderId, Long recipientId, BigDecimal amount, AbstractGroupEntity group) {
//        GroupOptimizedTransactionEntity newTransaction = new GroupOptimizedTransactionEntity();
//        newTransaction.setTransactionUUID(uuidHelper.UUIDForGroupOptimizedTransactions());
//        newTransaction.setSenderUser(userRepo.findById(senderId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND)));
//        newTransaction.setRecipientUser(userRepo.findById(recipientId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND)));
//        newTransaction.setGroup(group);
//        newTransaction.setTransactionAmount(amount);
//        newTransaction.setHasBeenSent(false);
//        newTransaction.setHasBeenChecked(false);
//        newTransaction.setRequiredReflection(Status.REQUIRE_REFLECT);
//        newTransaction.setCreatedAt(LocalDateTime.now());
//        newTransaction.setOptimizationStatus(Status.CURRENT);
//
//        Status isUserType = (group.getGroupType() == Status.REGULAR) ? Status.REGULAR : Status.DEMO;
//        newTransaction.setUserType(isUserType);
//        groupOptimizedTransactionRepo.save(newTransaction);
//        logger.debug("Created and saved new optimized transaction: {}, Sender ID: {}, Recipient ID: {}, Amount: {}",
//                newTransaction.getTransactionUUID(), senderId, recipientId, amount);
//
//        logger.info("createOptimizedTransaction OK");
//        return newTransaction;
//    }
//
//    private void saveTransactionDetails(GroupOptimizedTransactionEntity optimizedTransaction, Map<DefaultWeightedEdge, Long> edgeTransactionIdMap, Long startNode, Long endNode, Graph<Long, DefaultWeightedEdge> graph) {
//        DefaultWeightedEdge edge = graph.getEdge(startNode, endNode);
//        while (startNode != endNode) {
//            Long transactionId = edgeTransactionIdMap.get(edge);
//            GroupOptimizedTransactionDetailsEntity details = new GroupOptimizedTransactionDetailsEntity();
//            details.setGroupOptimizedTransaction(optimizedTransaction);
//            details.setTransactionDetailUUID(uuidHelper.UUIDForGroupOptimizedDetail());
//            details.setOptimizedTransaction(optimizedTransactionRepo.findById(transactionId).orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_ID_NOT_FOUND_IN_GROUP)));
//            updateReflection(optimizedTransactionRepo.findById(transactionId).orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_ID_NOT_FOUND_IN_GROUP)));
//            groupOptimizedDetailRepo.save(details);
//            logger.debug("Saved transaction detail: {}, for optimized transaction UUID: {}, based on original transaction ID: {}",
//                    details.getTransactionDetailUUID(), optimizedTransaction.getTransactionUUID(), transactionId);
//            startNode = graph.getEdgeTarget(edge);
//            edge = graph.getEdge(startNode, endNode);
//        }
//
//        logger.info("saveTransactionDetails OK");
//    }
//
//    private void updateReflection(OptimizedTransactionEntity optimizedTransactionEntity) {
//        optimizedTransactionRepo.updateRequiredReflectionByTransactionUUID(optimizedTransactionEntity.getTransactionUUID(), Status.INHERITED);
//        logger.info("updateReflection OK");
//    }
//
//    public GraphResult createGraphFromTransactions(List<Long> p2pList) {
//        Graph<Long, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
//        Map<DefaultWeightedEdge, Long> edgeTransactionIdMap = new HashMap<>();
//
//        for (Long transactionId : p2pList) {
//            Optional<OptimizedTransactionEntity> transactionOpt = optimizedTransactionRepo.findById(transactionId);
//            transactionOpt.ifPresent(transaction -> {
//                Long senderId = transaction.getSenderUser().getId();
//                Long recipientId = transaction.getRecipientUser().getId();
//                BigDecimal amount = transaction.getTransactionAmount();
//
//                graph.addVertex(senderId);
//                graph.addVertex(recipientId);
//
//                DefaultWeightedEdge edge = graph.addEdge(senderId, recipientId);
//                if (edge != null) {
//                    graph.setEdgeWeight(edge, amount.doubleValue());
//                    edgeTransactionIdMap.put(edge, transactionId);
//                }
//            });
//        }
//        logger.info("createGraphFromTransactions OK");
//
//        return new GraphResult(graph, edgeTransactionIdMap);
//    }
//
//    private List<Long> createGraphOrder(List<NetDto> net) {
//        // Log the initial state of the net list
//        logger.debug("Initial net list: {}", net);
//
//        // Sort the net list
//        net.sort(Comparator.comparing(NetDto::getNetAmount));
//
//        // Log the sorted state of the net list
//        logger.debug("Sorted net list: {}", net);
//
//        // Create the ordered user ID list
//        List<Long> orderedUserIdList = new ArrayList<>();
//        for (NetDto dto : net) {
//            orderedUserIdList.add(dto.getUser().getId());
//        }
//
//        // Log the final ordered user ID list
//        logger.debug("Ordered user ID list: {}", orderedUserIdList);
//        logger.info("createGraphOrder OK");
//        return orderedUserIdList;
//    }
//
//    @Override
//    @Transactional
//    public TransactionalEntity processTransaction(TransactionUpdateRequestDto request, AbstractGroupEntity existingGroup) throws CustomException {
//        logger.info("Processing transaction with ID: {}", request.getTransactionId());
//        GroupOptimizedTransactionEntity transactionEntity = groupOptimizedTransactionRepo.findByTransactionUUID(request.getTransactionId())
//                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_ID_NOT_FOUND_IN_GROUP));
//
//        if (!transactionEntity.getGroup().getId().equals(existingGroup.getId())) {
//            throw new CustomException(ErrorCode.TRANSACTION_ID_NOT_FOUND_IN_GROUP);
//        }
//
//        List<GroupOptimizedTransactionDetailsEntity> requireInheritanceList =
//                groupOptimizedDetailRepo.findByGroupOptimizedTransactionId(groupOptimizedTransactionRepo.findByTransactionUUID(transactionEntity.getTransactionUUID()).get().getId());
//        for (GroupOptimizedTransactionDetailsEntity requireInheritanceTransaction : requireInheritanceList) {
//            transactionInheritanceService.clearInheritanceStatusFromGroupToOptimized(requireInheritanceTransaction.getId());
//        }
//
//        return transactionEntity;
//    }
//}
package settleup.backend.domain.transaction.service.Impl;

import lombok.AllArgsConstructor;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.AbstractGroupEntity;
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
import settleup.backend.global.Helper.Status;
import settleup.backend.global.Helper.UUID_Helper;
import settleup.backend.global.Selector.UserRepoSelector;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
@Transactional
public class GroupOptimizedServiceImpl implements GroupOptimizedService {

    private final OptimizedTransactionRepository optimizedTransactionRepo;
    private final GroupOptimizedTransactionRepository groupOptimizedTransactionRepo;
    private final GroupOptimizedTransactionDetailRepository groupOptimizedDetailRepo;
    private final UserRepoSelector selector;
    private final UUID_Helper uuidHelper;
    private final TransactionInheritanceService transactionInheritanceService;

    private static final Logger logger = LoggerFactory.getLogger(GroupOptimizedServiceImpl.class);

    @Override
    public boolean optimizationInGroup(TransactionP2PResultDto resultDto, List<NetDto> net) {
        GraphResult graphResult = createGraphFromTransactions(resultDto.getOptimiziationByPeerToPeerList());
        List<Long> orderedUserIdList = createGraphOrder(net);
        AbstractGroupEntity group = getGroup(resultDto.getOptimiziationByPeerToPeerList());
        logger.debug("Graph created. Starting group optimization for group: {}", group.getId());

        boolean transactionCreated = startGroupOptimization(graphResult, orderedUserIdList, group);
        logger.debug("Group optimization completed for group: {}", group.getId());
        logger.info("optimizationInGroup OK");
        return transactionCreated;
    }

    private AbstractGroupEntity getGroup(List<Long> p2pList) {
        logger.debug("Retrieving group for p2pList first element: {}", p2pList.get(0));
        return optimizedTransactionRepo.findGroupByTransactionId(p2pList.get(0));
    }

    private boolean startGroupOptimization(GraphResult graphResult,
                                           List<Long> orderedUserIdList,
                                           AbstractGroupEntity group) {
        logger.debug("Starting group optimization. User list size: {}", orderedUserIdList.size());
        groupOptimizedTransactionRepo.updateOptimizationStatusByGroup(group, Status.PREVIOUS);
        Graph<Long, DefaultWeightedEdge> graph = graphResult.getGraph();
        Map<DefaultWeightedEdge, Long> edgeTransactionIdMap = graphResult.getEdgeTransactionIdMap();
        Set<DefaultWeightedEdge> visitedEdges = new HashSet<>();
        boolean transactionCreated = false;

        for (Long userId : orderedUserIdList) {
            if (graph.containsVertex(userId) && !visitedEdges.containsAll(graph.outgoingEdgesOf(userId))) {
                for (DefaultWeightedEdge edge : graph.outgoingEdgesOf(userId)) {
                    if (!visitedEdges.contains(edge)) {
                        BigDecimal initialWeight = BigDecimal.valueOf(graph.getEdgeWeight(edge));
                        Long targetNode = graph.getEdgeTarget(edge);
                        if (dfs(graph, targetNode, visitedEdges, edgeTransactionIdMap, group, userId, initialWeight)) {
                            transactionCreated = true;
                        }
                    }
                }
            }
        }
        logger.info("startGroupOptimization OK");
        return transactionCreated;
    }

    private boolean dfs(Graph<Long, DefaultWeightedEdge> graph,
                        Long currentNode,
                        Set<DefaultWeightedEdge> visitedEdges,
                        Map<DefaultWeightedEdge, Long> edgeTransactionIdMap,
                        AbstractGroupEntity group,
                        Long startNode,
                        BigDecimal lastWeight) {
        if (!graph.containsVertex(currentNode)) {
            logger.error("DFS attempted on non-existent node: {}", currentNode);
            return false;
        }

        logger.debug("DFS started for node: {}", currentNode);
        boolean transactionCreated = false;
        for (DefaultWeightedEdge edge : graph.outgoingEdgesOf(currentNode)) {
            if (visitedEdges.contains(edge)) continue;

            visitedEdges.add(edge);
            Long targetNode = graph.getEdgeTarget(edge);
            BigDecimal currentWeight = BigDecimal.valueOf(graph.getEdgeWeight(edge));

            if (currentWeight.compareTo(lastWeight) == 0 && !targetNode.equals(startNode)) {
                transactionCreated |= dfs(graph, targetNode, visitedEdges, edgeTransactionIdMap, group, startNode, lastWeight);
            } else {
                if (startNode != null && !startNode.equals(currentNode)) {
                    GroupOptimizedTransactionEntity newTransaction = createOptimizedTransaction(startNode, currentNode, lastWeight, group);
                    saveTransactionDetails(newTransaction, edgeTransactionIdMap, startNode, currentNode, graph);
                    transactionCreated = true;
                }
                transactionCreated |= dfs(graph, targetNode, new HashSet<>(visitedEdges), edgeTransactionIdMap, group, currentNode, currentWeight);
            }
        }
        return transactionCreated;
    }

    private GroupOptimizedTransactionEntity createOptimizedTransaction(Long senderId, Long recipientId, BigDecimal amount, AbstractGroupEntity group) {
        GroupOptimizedTransactionEntity newTransaction = new GroupOptimizedTransactionEntity();
        newTransaction.setTransactionUUID(uuidHelper.UUIDForGroupOptimizedTransactions());
        Boolean isUserType = null;
        if(group.getGroupType() ==Status.REGULAR){
            isUserType =true;
        }else {
            isUserType =false;
        }

        newTransaction.setSenderUser(selector.getUserRepository(isUserType).findById(senderId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND)));
        newTransaction.setRecipientUser(selector.getUserRepository(isUserType).findById(recipientId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND)));
        newTransaction.setGroup(group);
        newTransaction.setTransactionAmount(amount);
        newTransaction.setHasBeenSent(false);
        newTransaction.setHasBeenChecked(false);
        newTransaction.setRequiredReflection(Status.REQUIRE_REFLECT);
        newTransaction.setCreatedAt(LocalDateTime.now());
        newTransaction.setOptimizationStatus(Status.CURRENT);

        Status isUserTypeB = (group.getGroupType() == Status.REGULAR) ? Status.REGULAR : Status.DEMO;
        newTransaction.setUserType(isUserTypeB);
        groupOptimizedTransactionRepo.save(newTransaction);
        logger.debug("Created and saved new optimized transaction: {}, Sender ID: {}, Recipient ID: {}, Amount: {}",
                newTransaction.getTransactionUUID(), senderId, recipientId, amount);

        logger.info("createOptimizedTransaction OK");
        return newTransaction;
    }

    private void saveTransactionDetails(GroupOptimizedTransactionEntity optimizedTransaction, Map<DefaultWeightedEdge, Long> edgeTransactionIdMap, Long startNode, Long endNode, Graph<Long, DefaultWeightedEdge> graph) {
        DefaultWeightedEdge edge = graph.getEdge(startNode, endNode);
        while (startNode != endNode) {
            Long transactionId = edgeTransactionIdMap.get(edge);
            GroupOptimizedTransactionDetailsEntity details = new GroupOptimizedTransactionDetailsEntity();
            details.setGroupOptimizedTransaction(optimizedTransaction);
            details.setTransactionDetailUUID(uuidHelper.UUIDForGroupOptimizedDetail());
            details.setOptimizedTransaction(optimizedTransactionRepo.findById(transactionId).orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_ID_NOT_FOUND_IN_GROUP)));
            updateReflection(optimizedTransactionRepo.findById(transactionId).orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_ID_NOT_FOUND_IN_GROUP)));
            groupOptimizedDetailRepo.save(details);
            logger.debug("Saved transaction detail: {}, for optimized transaction UUID: {}, based on original transaction ID: {}",
                    details.getTransactionDetailUUID(), optimizedTransaction.getTransactionUUID(), transactionId);
            startNode = graph.getEdgeTarget(edge);
            edge = graph.getEdge(startNode, endNode);
        }

        logger.info("saveTransactionDetails OK");
    }

    private void updateReflection(OptimizedTransactionEntity optimizedTransactionEntity) {
        optimizedTransactionRepo.updateRequiredReflectionByTransactionUUID(optimizedTransactionEntity.getTransactionUUID(), Status.INHERITED);
        logger.info("updateReflection OK");
    }

    public GraphResult createGraphFromTransactions(List<Long> p2pList) {
        Graph<Long, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Map<DefaultWeightedEdge, Long> edgeTransactionIdMap = new HashMap<>();

        for (Long transactionId : p2pList) {
            Optional<OptimizedTransactionEntity> transactionOpt = optimizedTransactionRepo.findById(transactionId);
            transactionOpt.ifPresent(transaction -> {
                Long senderId = transaction.getSenderUser().getId();
                Long recipientId = transaction.getRecipientUser().getId();
                BigDecimal amount = transaction.getTransactionAmount();

                graph.addVertex(senderId);
                graph.addVertex(recipientId);

                DefaultWeightedEdge edge = graph.addEdge(senderId, recipientId);
                if (edge != null) {
                    graph.setEdgeWeight(edge, amount.doubleValue());
                    edgeTransactionIdMap.put(edge, transactionId);
                }
            });
        }
        logger.info("createGraphFromTransactions OK");

        return new GraphResult(graph, edgeTransactionIdMap);
    }

    private List<Long> createGraphOrder(List<NetDto> net) {
        // Log the initial state of the net list
        logger.debug("Initial net list: {}", net);

        // Sort the net list
        net.sort(Comparator.comparing(NetDto::getNetAmount));

        // Log the sorted state of the net list
        logger.debug("Sorted net list: {}", net);

        // Create the ordered user ID list
        List<Long> orderedUserIdList = new ArrayList<>();
        for (NetDto dto : net) {
            orderedUserIdList.add(dto.getUser().getId());
        }

        // Log the final ordered user ID list
        logger.debug("Ordered user ID list: {}", orderedUserIdList);
        logger.info("createGraphOrder OK");
        return orderedUserIdList;
    }

    @Override
    @Transactional
    public TransactionalEntity processTransaction(TransactionUpdateRequestDto request, AbstractGroupEntity existingGroup) throws CustomException {
        logger.info("Processing transaction with ID: {}", request.getTransactionId());
        GroupOptimizedTransactionEntity transactionEntity = groupOptimizedTransactionRepo.findByTransactionUUID(request.getTransactionId())
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_ID_NOT_FOUND_IN_GROUP));

        if (!transactionEntity.getGroup().getId().equals(existingGroup.getId())) {
            throw new CustomException(ErrorCode.TRANSACTION_ID_NOT_FOUND_IN_GROUP);
        }

        List<GroupOptimizedTransactionDetailsEntity> requireInheritanceList =
                groupOptimizedDetailRepo.findByGroupOptimizedTransactionId(groupOptimizedTransactionRepo.findByTransactionUUID(transactionEntity.getTransactionUUID()).get().getId());
        for (GroupOptimizedTransactionDetailsEntity requireInheritanceTransaction : requireInheritanceList) {
            transactionInheritanceService.clearInheritanceStatusFromGroupToOptimized(requireInheritanceTransaction.getId());
        }

        return transactionEntity;
    }
}
