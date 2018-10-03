package repo;

// status: contentSha =? indexSha

import repo.objects.Blob;

import java.util.Map;

public enum WORKDIR_INDEX_STATUS {
    UNTRACKED, // (in workdir) and (not in index)
    UNCHANGED, // (in workdir) and (in index) and (contentSha == indexSha)
    MODIFIED, // (in workdir) and (in index) and (contentSha != indexSha)
    DELETED;// (not in workdir) and (in index)

    public static WORKDIR_INDEX_STATUS getFileStatus(String relativeFileName, Map<String, String> workdirFiles, Map<String, Blob> indexedFiles) throws Exception {
        WORKDIR_INDEX_STATUS workdirIndexStatus = null;

        if (workdirFiles.containsKey(relativeFileName) && // (in workdir) and (not in index)
                !indexedFiles.containsKey(relativeFileName)) {
            workdirIndexStatus = WORKDIR_INDEX_STATUS.UNTRACKED;

        } else if (workdirFiles.containsKey(relativeFileName) &&  // (in workdir) and (in index) and (contentSha == indexSha)
                indexedFiles.containsKey(relativeFileName) &&
                workdirFiles.get(relativeFileName).equals(indexedFiles.get(relativeFileName).sha)) {
            workdirIndexStatus = WORKDIR_INDEX_STATUS.UNCHANGED;

        } else if (workdirFiles.containsKey(relativeFileName) &&  // (in workdir) and (in index) and (contentSha != indexSha)
                indexedFiles.containsKey(relativeFileName) &&
                !workdirFiles.get(relativeFileName).equals(indexedFiles.get(relativeFileName).sha)) {
            workdirIndexStatus = WORKDIR_INDEX_STATUS.MODIFIED;

        } else if (!workdirFiles.containsKey(relativeFileName) && // (not in workdir) and (in index)
                indexedFiles.containsKey(relativeFileName)) {
            workdirIndexStatus = WORKDIR_INDEX_STATUS.DELETED;

        } else {
            throw new Exception("impossible WORKDIR_INDEX_STATUS");
        }
        return workdirIndexStatus;
    }
}
