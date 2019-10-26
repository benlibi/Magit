package Models;

import Enums.PrStatus;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PR {
    public String getAskUser() {
        return askUser;
    }

    String askUser;
    String ownerUser;
    String repoName;

    public String getPrMsg() {
        return prMsg;
    }

    String prMsg;

    public String getTargetBranch() {
        return targetBranch;
    }

    String targetBranch;

    public String getSourceBranch() {
        return sourceBranch;
    }

    String sourceBranch;
    String prDate;
    PrStatus prStatus;
    String changes;

    int id;

    public static int rolling_id =0;
    public PR(String askUser, String ownerUser, String repoName, String prMsg, String targetBranch, String sourceBranch, String changes){
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-hh:mm");

        this.prDate = formatter.format(date);
        this.askUser = askUser;
        this.ownerUser = ownerUser;
        this.repoName = repoName;
        this.prMsg = prMsg;
        this.targetBranch = targetBranch;
        this.sourceBranch = sourceBranch;
        this.changes = changes;
        prStatus = PrStatus.Open;
        id = rolling_id;
        rolling_id +=1;
    }


    public void setPrStatus(PrStatus prStatus) {
        this.prStatus = prStatus;
    }

    public int getId() {
        return id;
    }

}
