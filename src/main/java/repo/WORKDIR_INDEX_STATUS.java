package repo;

// status: contentSha =? indexSha

public enum WORKDIR_INDEX_STATUS {
    UNTRACKED, // (in workdir) and (not in index)
    UNCHANGED, // (in workdir) and (in index) and (contentSha == indexSha)
    MODIFIED, // (in workdir) and (in index) and (contentSha != indexSha)
    DELETED // (not in workdir) and (in index)
}
