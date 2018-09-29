package repo;

// status: indexSha =? headSha

public enum INDEX_HEAD_STATUS {
    NEWFILE, // (in index) and (not in head)
    UNCHANGED, // ((in index) and (in head) and (indexSha == headSha)) or ( (not in index) and (not in head) )
    MODIFIED, // (in index) and (in head) and (indexSha != headSha)
    DELETED // (not in index) and (in head)
}
