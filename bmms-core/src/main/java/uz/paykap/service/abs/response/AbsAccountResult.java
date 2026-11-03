package uz.paykap.service.abs.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbsAccountResult {
    private String transId;
    private String branch;
    private String id;
    private String accBal;
    private String currency;
    private String client;
    private String idOrder;
    private String name;
    private String sgn;
    private String bal;
    private String signRegistr;
    private String sIn;
    private String sOut;
    private String dt;
    private String ct;
    private String sInTmp;
    private String sOutTmp;
    private String dtTmp;
    private String ctTmp;
    private Timestamp lDate;
    private Timestamp dateOpen;
    private Timestamp dateClose;
    private String accGroupId;
    private String state;
    private String kodErr;
    private String fileName;
    private String reasonClose;
    private Timestamp dateOpenNibbd;
    private Timestamp dateCloseNibbd;
    private String errorCode;
    private String errorNote;
    private String checkRequestId;
    private String mfo;
    private String inn;


    public AbsAccountResult withTransId(String transId) {
        this.transId = transId;
        return this;
    }

    public AbsAccountResult withBranch(String branch) {
        this.branch = branch;
        return this;
    }

    public AbsAccountResult withId(String id) {
        this.id = id;
        return this;
    }

    public AbsAccountResult withAccBal(String accBal) {
        this.accBal = accBal;
        return this;
    }

    public AbsAccountResult withCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public AbsAccountResult withClient(String client) {
        this.client = client;
        return this;
    }

    public AbsAccountResult withIdOrder(String idOrder) {
        this.idOrder = idOrder;
        return this;
    }

    public AbsAccountResult withName(String name) {
        this.name = name;
        return this;
    }

    public AbsAccountResult withSgn(String sgn) {
        this.sgn = sgn;
        return this;
    }

    public AbsAccountResult withBal(String bal) {
        this.bal = bal;
        return this;
    }

    public AbsAccountResult withSignRegistr(String signRegistr) {
        this.signRegistr = signRegistr;
        return this;
    }

    public AbsAccountResult withSIn(String sIn) {
        this.sIn = sIn;
        return this;
    }

    public AbsAccountResult withSOut(String sOut) {
        this.sOut = sOut;
        return this;
    }

    public AbsAccountResult withDt(String dt) {
        this.dt = dt;
        return this;
    }

    public AbsAccountResult withCt(String ct) {
        this.ct = ct;
        return this;
    }

    public AbsAccountResult withSInTmp(String sInTmp) {
        this.sInTmp = sInTmp;
        return this;
    }

    public AbsAccountResult withSOutTmp(String sOutTmp) {
        this.sOutTmp = sOutTmp;
        return this;
    }

    public AbsAccountResult withDtTmp(String dtTmp) {
        this.dtTmp = dtTmp;
        return this;
    }

    public AbsAccountResult withCtTmp(String ctTmp) {
        this.ctTmp = ctTmp;
        return this;
    }

    public AbsAccountResult withLDate(Timestamp lDate) {
        this.lDate = lDate;
        return this;
    }

    public AbsAccountResult withDateOpen(Timestamp dateOpen) {
        this.dateOpen = dateOpen;
        return this;
    }

    public AbsAccountResult withDateClose(Timestamp dateClose) {
        this.dateClose = dateClose;
        return this;
    }

    public AbsAccountResult withAccGroupId(String accGroupId) {
        this.accGroupId = accGroupId;
        return this;
    }

    public AbsAccountResult withState(String state) {
        this.state = state;
        return this;
    }

    public AbsAccountResult withKodErr(String kodErr) {
        this.kodErr = kodErr;
        return this;
    }

    public AbsAccountResult withFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public AbsAccountResult withReasonClose(String reasonClose) {
        this.reasonClose = reasonClose;
        return this;
    }

    public AbsAccountResult withDateOpenNibbd(Timestamp dateOpenNibbd) {
        this.dateOpenNibbd = dateOpenNibbd;
        return this;
    }

    public AbsAccountResult withDateCloseNibbd(Timestamp dateCloseNibbd) {
        this.dateCloseNibbd = dateCloseNibbd;
        return this;
    }

    public AbsAccountResult withErrorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public AbsAccountResult withErrorNote(String errorNote) {
        this.errorNote = errorNote;
        return this;
    }

    public AbsAccountResult withCheckRequestId(String checkRequestId) {
        this.checkRequestId = checkRequestId;
        return this;
    }
}
