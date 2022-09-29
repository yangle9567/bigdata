package com.bcld.domain.logic;

import com.bcld.utils.Globals;

public class RowBounds {

    private long limit;

    private long offset;

    private long pageSize;

    private long pageNumber;

    public RowBounds() {
        super();
    }

    public RowBounds(String pageNumber) {
        super();
        this.pageSize = Globals.PAGESIZE;
        this.pageNumber = Long.valueOf(pageNumber);
        this.setLimit(this.pageSize);
        this.setOffset((this.pageNumber - 1) * this.pageSize);
    }

    public RowBounds(String pageNumber, String pageSize) {
        super();
        this.pageSize = Long.valueOf(pageSize);
        this.pageNumber = Long.valueOf(pageNumber);
        this.setLimit(this.pageSize);
        this.setOffset((this.pageNumber - 1) * this.pageSize);
    }

    public RowBounds(long pageNumber) {
        super();
        this.pageSize = Globals.PAGESIZE;
        this.pageNumber = pageNumber;
        this.setLimit(this.pageSize);
        this.setOffset((this.pageNumber - 1) * this.pageSize);
    }

    public RowBounds(long pageNumber, long pageSize) {
        super();
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
        this.setLimit(this.pageSize);
        this.setOffset((this.pageNumber - 1) * pageSize);
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        if (limit < 1) {
            this.limit = 1;
        } else {
            this.limit = limit;
        }
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        if (offset < 0) {
            this.offset = 0;
        } else {
            this.offset = offset;
        }
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public long getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(long pageNumber) {
        this.pageNumber = pageNumber;
    }

}
