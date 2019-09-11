package Models;

import Enums.ConflictSections;

import java.util.Map;

public class Conflict {
    String filePath;
    String fileSha1;
    Map<ConflictSections, String> relatedBlobs;

    public Conflict(String filePath, String fileSha1, Map<ConflictSections, String> relatedBlobs) {
        this.filePath = filePath;
        this.fileSha1 = fileSha1;
        this.relatedBlobs = relatedBlobs;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileSha1() {
        return fileSha1;
    }

    public void setFileSha1(String fileSha1) {
        this.fileSha1 = fileSha1;
    }

    public Map<ConflictSections, String> getRelatedBlobs() {
        return relatedBlobs;
    }

    public void setRelatedBlobs(Map<ConflictSections, String> relatedBlobs) {
        this.relatedBlobs = relatedBlobs;
    }
}
