package uz.paykap.service.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the MachineGroup entity.
 */
public class MachineGroupDTO implements Serializable {
    private Long id;

    @NotNull
    private Long parentId;

    @NotNull
    @Size(max = 50)
    private String name;

    @NotNull
    @Size(max = 16)
    private String shortName;

    @NotNull
    @Size(max = 8)
    private String mfo;

    @NotNull
    @Size(max = 16)
    private String inn;

    @NotNull
    @Size(max = 32)
    private String account;

    @Size(max = 128)
    private String contract;

    @NotNull
    private Boolean activeCheck;

    @NotNull
    private Boolean activePay;

    @Size(max = 32)
    private String phoneNumber;

    @Size(max = 64)
    private String encashmentEmployee;

    @Size(max = 64)
    private String appFolder;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getMfo() {
        return mfo;
    }

    public void setMfo(String mfo) {
        this.mfo = mfo;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public Boolean isActiveCheck() {
        return activeCheck;
    }

    public void setActiveCheck(Boolean activeCheck) {
        this.activeCheck = activeCheck;
    }

    public Boolean isActivePay() {
        return activePay;
    }

    public void setActivePay(Boolean activePay) {
        this.activePay = activePay;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEncashmentEmployee() {
        return encashmentEmployee;
    }

    public void setEncashmentEmployee(String encashmentEmployee) {
        this.encashmentEmployee = encashmentEmployee;
    }

    public String getAppFolder() {
        return appFolder;
    }

    public void setAppFolder(String appFolder) {
        this.appFolder = appFolder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MachineGroupDTO machineGroupDTO = (MachineGroupDTO) o;
        if (machineGroupDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), machineGroupDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "MachineGroupDTO{" +
            "id=" + getId() +
            ", parentId=" + getParentId() +
            ", name='" + getName() + "'" +
            ", shortName='" + getShortName() + "'" +
            ", mfo='" + getMfo() + "'" +
            ", inn='" + getInn() + "'" +
            ", account='" + getAccount() + "'" +
            ", contract='" + getContract() + "'" +
            ", activeCheck='" + isActiveCheck() + "'" +
            ", activePay='" + isActivePay() + "'" +
            ", phoneNumber='" + getPhoneNumber() + "'" +
            ", encashmentEmployee='" + getEncashmentEmployee() + "'" +
            ", appFolder='" + getAppFolder() + "'" +
            "}";
    }
}
