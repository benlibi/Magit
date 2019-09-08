package Models;

import org.apache.commons.collections4.list.SetUniqueList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExtendedCommit extends Commit {

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    String branchName;

    public Set<ExtendedCommit> getCommitChildes() {
        return commitChildes;
    }

    private Set<ExtendedCommit> commitChildes = new HashSet<>();

    public ExtendedCommit(String commitRepresentation){
        super(commitRepresentation);
    }



    public void addToCommitListChildList(ExtendedCommit commit) {
        commitChildes.add(commit);
    }

    private ExtendedCommit compareCommits(ExtendedCommit commit1, ExtendedCommit commit2){
        if(commit1.compareTo(commit2)>0){
            return commit2;
        }else {
            return commit1;
        }
    }

    public ExtendedCommit getOlderCommit(){
        ExtendedCommit olderCommit = null;
        for(ExtendedCommit commit: commitChildes){
            if(olderCommit==null){
                olderCommit = commit;
            }
            else{
                olderCommit = compareCommits(commit, olderCommit);
            }
        }
        return olderCommit;
    }
}
