package uz.paykap.service.bookkeeping.dto;

import java.io.Serializable;

public class AbsEntryResult implements Serializable {
    private String absId;

    public String getAbsId() {
        return absId;
    }

    public void setAbsId(String absId) {
        this.absId = absId;
    }

    public AbsEntryResult() {
    }

    public AbsEntryResult(String absId) {
        this.absId = absId;
    }

    @Override
    public String toString() {
        return "AbsEntryResult{" +
            "absId='" + absId + '\'' +
            '}';
    }
}
