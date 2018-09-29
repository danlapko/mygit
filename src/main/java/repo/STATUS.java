package repo;

public class STATUS {
    public WORKDIR_INDEX_STATUS workdirIndexStatus;
    public INDEX_HEAD_STATUS indexHeadStatus;

    public STATUS() {
    }

    public STATUS(WORKDIR_INDEX_STATUS workdirIndexStatus, INDEX_HEAD_STATUS indexHeadStatus) {
        this.workdirIndexStatus = workdirIndexStatus;
        this.indexHeadStatus = indexHeadStatus;
    }
}
