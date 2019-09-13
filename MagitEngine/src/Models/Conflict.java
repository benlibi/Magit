package Models;

import Enums.ConflictSections;

import java.util.Map;

public class Conflict {
    String filePath;
    Map<ConflictSections, String> relatedBlobs;

    public Conflict(String filePath, Map<ConflictSections, String> relatedBlobs) {
        this.filePath = filePath;
        this.relatedBlobs = relatedBlobs;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Map<ConflictSections, String> getRelatedBlobs() {
        return relatedBlobs;
    }

    public void setRelatedBlobs(Map<ConflictSections, String> relatedBlobs) {
        this.relatedBlobs = relatedBlobs;
    }
}
