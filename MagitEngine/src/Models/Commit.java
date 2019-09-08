package Models;

import generated.MagitSingleCommit;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Commit implements Comparable<Commit>{

    private String commitSha1;

    public String getCommitDate() {
        return commitDate;
    }

    private String commitDate;

    public String getCommitter() {
        return committer;
    }

    private String committer = User.getName();
    private String mainRepoSha1;

    public String getCommitMassage() {
        return commitMassage;
    }

    private String commitMassage;
    private List<String> changedFiles = new ArrayList<>();
    private List<String> commitHistory = new ArrayList<>();
    private String previousCommit;


    public Commit(String commitRepresentation) {
        String[] commitAttr = commitRepresentation.split(",");
        this.mainRepoSha1 = commitAttr[0];
        this.commitMassage = commitAttr[1];
        this.commitDate = commitAttr[2];
        this.committer = commitAttr[3];
        if (commitAttr.length >= 5) {
            commitHistory.add(commitAttr[4]);
            this.previousCommit = "";
        } else if (commitAttr.length >= 6) {
            commitHistory.add(commitAttr[5]);
        }
        this.commitSha1 = DigestUtils.sha1Hex(this.toString());
    }

    Commit(MagitSingleCommit commit, String mainProjectSha1, List<String> commitHistoryList) {
        this.commitMassage = commit.getMessage();
        this.committer = commit.getAuthor();
        this.commitDate = commit.getDateOfCreation();
        this.mainRepoSha1 = mainProjectSha1;
        this.commitHistory = commitHistoryList;
        this.commitSha1 = DigestUtils.sha1Hex(this.toString());
    }

    public Commit(String commitMassage, String mainProjectSha1, String previouseCommit) {

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.mm.yyyy-hh:mm:ss:sss");

        this.commitDate = formatter.format(date);
        this.mainRepoSha1 = mainProjectSha1;
        this.commitMassage = commitMassage;
        this.commitSha1 = DigestUtils.sha1Hex(this.toString());
        this.commitHistory.add(previouseCommit);
    }

    @Override
    public String toString() {

        return this.mainRepoSha1 + "," + this.commitMassage + "," + this.commitDate + "," + this.committer;
    }

    public String toFileString() {
        String previousCommitsCommaSeparated = String.join(",", commitHistory);
        return this.mainRepoSha1 + "," + this.commitMassage + "," + this.commitDate + "," + this.committer + "," +
                previousCommitsCommaSeparated;
    }

    public String commitInfo() {

        return "Commit SHA1: " + this.commitSha1 + " ,Message: " + this.commitMassage + " ,Date: " + this.commitDate + " ,Commiter: " +
                this.committer;
    }

    public void createCommitRepresentation(String path) {
        try {
            File file = new File(path, this.commitSha1);
            ZipOutputStream out = null;
            out = new ZipOutputStream(new FileOutputStream(file));
            ZipEntry e = new ZipEntry(this.commitSha1);

            out.putNextEntry(e);
            out.write(this.toFileString().getBytes());
            out.closeEntry();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPreviousCommit() {
        return previousCommit;
    }

    public String getCommitSha1() {
        return commitSha1;
    }

    public String getMainRepoSha1() {
        return mainRepoSha1;
    }

    public List<String> getChangedFiles() {
        return changedFiles;
    }

    public List<String> getCommitHistory() {
        return commitHistory;
    }

    @Override
    public int compareTo(Commit o) {
        return this.getCommitDate().compareTo(o.getCommitDate());
    }

}