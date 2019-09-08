import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.layout.Layout;


import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// simple test for scattering commits in imaginary tree, where every 3rd node is in a new 'branch' (moved to the right)
public class CommitTreeLayout implements Layout {
    private List<String> branches;
    private Map<String,Integer> sortedBranchMao;
    CommitTreeLayout(List<String> branches){
        this.branches = branches;
        java.util.Collections.sort(branches);
        createSoredBranchMap();
    }

    private void createSoredBranchMap(){
        Map<String,Integer> sortedBranchMap = new TreeMap<String, Integer>();
        int i=0;
        for(String branchName: branches){
            sortedBranchMap.put(branchName,i*50);
            i+=1;
        }
        this.sortedBranchMao = sortedBranchMap;
    }

    @Override
    public void execute(Graph graph) {

        final List<ICell> cells = graph.getModel().getAllCells();
        AtomicInteger startY = new AtomicInteger();
        cells.stream()
                .filter(CommitNode.class::isInstance)
                .map(CommitNode.class::cast)
                .sorted(Comparator.reverseOrder())
                .forEach(commitNode -> {
                    if (commitNode.getBranchName()!=null) {
                        graph.getGraphic(commitNode)
                                .relocate(sortedBranchMao.get(commitNode.getBranchName()), startY.get());
                        startY.addAndGet(50);
                    }
                });
//        Collections.sort(cells);
//        int startY = 50;
//        for (ICell cell : cells) {
//            CommitNode c = (CommitNode) cell;
//            graph.getGraphic(c).relocate(sortedBranchMao.get(c.getBranchName()), startY);
//            startY +=50;
//        }
    }
}