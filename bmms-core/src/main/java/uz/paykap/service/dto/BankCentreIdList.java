package uz.paykap.service.dto;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ROWDATA")
public class BankCentreIdList {

    @XmlElementRef(name = "row")
    private List<BankCentreId> bankCentreIds;

    public BankCentreIdList(List<BankCentreId> bankCentreIds) {
        this.bankCentreIds = bankCentreIds;
    }

    public BankCentreIdList() {
    }

    public List<BankCentreId> getBankCentreIds() {
        return bankCentreIds;
    }
}
