import com.fxgraph.cells.AbstractCell;
import com.fxgraph.graph.ICell;
import com.fxgraph.graph.IEdge;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import com.fxgraph.graph.Graph;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CommitNode extends AbstractCell implements Comparable< CommitNode > {
    public String getTimestamp() {
        return timestamp;
    }

    public Date getTimestampDate() throws ParseException {
        Date commitDate = new SimpleDateFormat("dd.MM.yyyy-hh:mm:ss:SSS").parse(this.timestamp);
        return commitDate;
    }

    private String timestamp;
    private String committer;
    private String message;
    public String sha1;
    private String rootPath;

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    private String branchName;
    private Controller commitNodeController;

    public CommitNode(String sha1, String timestamp, String committer, String message, String branchName, String rootRepoPath) {
        this.timestamp = timestamp;
        this.committer = committer;
        this.message = message;
        this.branchName = branchName;
        this.sha1=sha1;
        this.rootPath=rootRepoPath;
    }

    @Override
    public Region getGraphic(Graph graph) {

        try {

            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("commitNode.fxml");
            fxmlLoader.setLocation(url);
            GridPane root = fxmlLoader.load(url.openStream());

            commitNodeController = fxmlLoader.getController();
            commitNodeController.setCommitSha1(sha1);
            commitNodeController.setBranchName(branchName);
            commitNodeController.setCommitMessage(message);
            commitNodeController.setCommitter(committer);
            commitNodeController.setCommitTimeStamp(timestamp);
            commitNodeController.setCircleId(sha1,rootPath);


            return root;
        } catch (IOException e) {
            return new Label("Error when tried to create graphic node !");
        }
    }

    @Override
    public DoubleBinding getXAnchor(Graph graph, IEdge edge) {
        final Region graphic = graph.getGraphic(this);
        return graphic.layoutXProperty().add(commitNodeController.getCircleRadius());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommitNode that = (CommitNode) o;

        return timestamp != null ? timestamp.equals(that.timestamp) : that.timestamp == null;
    }

    @Override
    public int hashCode() {
        return timestamp != null ? timestamp.hashCode() : 0;
    }

    @Override
    public int compareTo(CommitNode o) {
        try {
            return getTimestampDate().compareTo(o.getTimestampDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
