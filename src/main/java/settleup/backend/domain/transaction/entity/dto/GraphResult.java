package settleup.backend.domain.transaction.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GraphResult {
    private Graph<Long, DefaultWeightedEdge> graph;
    private Map<DefaultWeightedEdge, Long> edgeTransactionIdMap;
}
