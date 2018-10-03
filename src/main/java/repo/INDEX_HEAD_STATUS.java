package repo;

// status: indexSha =? headSha

import repo.objects.Blob;

import java.util.Map;

public enum INDEX_HEAD_STATUS {
    NEWFILE, // (in index) and (not in head)
    UNCHANGED, // ((in index) and (in head) and (indexSha == headSha)) or ( (not in index) and (not in head) )
    MODIFIED, // (in index) and (in head) and (indexSha != headSha)
    DELETED; // (not in index) and (in head)

    public static INDEX_HEAD_STATUS getFileStatus(String relativeFileName, Map<String, Blob> headFiles, Map<String, Blob> indexedFiles) throws Exception {
        INDEX_HEAD_STATUS indexHeadStatus;

        if (indexedFiles.containsKey(relativeFileName) && // (in index) and (not in head)
                !headFiles.containsKey(relativeFileName)) {
            indexHeadStatus = INDEX_HEAD_STATUS.NEWFILE;

        } else if (!indexedFiles.containsKey(relativeFileName) && // (not in index) and (not in head)
                !headFiles.containsKey(relativeFileName)) {
            indexHeadStatus = INDEX_HEAD_STATUS.UNCHANGED;

        } else if (indexedFiles.containsKey(relativeFileName) && // (in index) and (in head) and (indexSha == headSha)
                headFiles.containsKey(relativeFileName) &&
                indexedFiles.get(relativeFileName).sha.equals(headFiles.get(relativeFileName).sha)) {
            indexHeadStatus = INDEX_HEAD_STATUS.UNCHANGED;

        } else if (indexedFiles.containsKey(relativeFileName) && // (in index) and (in head) and (indexSha != headSha)
                headFiles.containsKey(relativeFileName) &&
                !indexedFiles.get(relativeFileName).sha.equals(headFiles.get(relativeFileName).sha)) {
            indexHeadStatus = INDEX_HEAD_STATUS.MODIFIED;

        } else if (!indexedFiles.containsKey(relativeFileName) && // (not in index) and (in head)
                headFiles.containsKey(relativeFileName)) {
            indexHeadStatus = INDEX_HEAD_STATUS.DELETED;

        } else {
            throw new Exception("impossible INDEX_HEAD_STATUS");
        }

        return indexHeadStatus;
    }
}
