package uz.paykap.service.dto;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ROW")
public class BankCentreId {

    @XmlElement(name = "BIN")
    private String bin;

    @XmlElement(name = "CENTRE_ID")
    private String centreId;

    @XmlElement(name = "BANK_C")
    private String bankC;

    @XmlElement(name = "C0")
    private String co;

    @XmlElement(name = "DESCRIPTION")
    private String description;

    public BankCentreId(String bin, String centreId, String bankC, String co, String description) {
        this.bin = bin;
        this.centreId = centreId;
        this.bankC = bankC;
        this.co = co;
        this.description = description;
    }

    public BankCentreId(String bin, String centreId) {
        this.bin = bin;
        this.centreId = centreId;
    }

    public BankCentreId() {
    }

    public String getBin() {
        return bin;
    }

    public String getCentreId() {
        return centreId;
    }

    public String getBankC() {
        return bankC;
    }

    public String getCo() {
        return co;
    }

    public String getDescription() {
        return description;
    }
}
