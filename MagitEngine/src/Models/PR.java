package Models;

import Enums.PrStatus;

public class PR {
    String askUser;
    String ownerUser;
    String repoName;
    String prMsg;
    String targetBranch;
    String sourceBranch;
    PrStatus prStatus;

    public PR(String askUser, String ownerUser, String repoName, String prMsg, String targetBranch, String sourceBranch){
        this.askUser = askUser;
        this.ownerUser = ownerUser;
        this.repoName = repoName;
        this.prMsg = prMsg;
        this.targetBranch = targetBranch;
        this.sourceBranch = sourceBranch;
        prStatus = PrStatus.Open;
    }
}
