package uz.paykap.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.*;
import uz.paykap.domain.enumeration.DepositState;

/**
 * A DTO for the {@link uz.paykap.domain.Deposit} entity.
 */
@ApiModel(description = "Баланс агента")
public class DepositDTO implements Serializable {

    private Long id;

    private Long version;

    /**
     * Размер овердрафта
     */
    @NotNull
    @ApiModelProperty(value = "Размер овердрафта", required = true)
    private Long overdraftLimit;

    /**
     * Сумма доступного баланса
     */
    @NotNull
    @ApiModelProperty(value = "Сумма доступного баланса", required = true)
    private Long balance;

    /**
     * Валюта баланса
     */
    @NotNull
    @ApiModelProperty(value = "Валюта баланса", required = true)
    private Integer balanceCcy;

    /**
     * Зарезервированный баланса
     */
    @NotNull
    @ApiModelProperty(value = "Зарезервированный баланса", required = true)
    private Long bookedBalance;

    /**
     * Порог уведомления при достижении лимита
     */
    @NotNull
    @ApiModelProperty(value = "Порог уведомления при достижении лимита", required = true)
    private Long limitInform;

    /**
     * Порог остановки оплат
     */
    @NotNull
    @ApiModelProperty(value = "Порог остановки оплат", required = true)
    private Long limitDisable;

    /**
     * Группа контактов для уведомления
     */
    @Size(max = 64)
    @ApiModelProperty(value = "Группа контактов для уведомления")
    private String contactGroup;

    /**
     * Заблокирован
     */
    @NotNull
    @ApiModelProperty(value = "Заблокирован", required = true)
    private Boolean blocked;

    /**
     * Без ограничения
     */
    @NotNull
    @ApiModelProperty(value = "Без ограничения", required = true)
    private Boolean unlimit;

    /**
     * Состояние
     */
    @ApiModelProperty(value = "Состояние")
    private DepositState state;

    private GatewayDTO gateway;

    private AgentDTO agent;

    private VirtualAccountDTO virtualAccount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(Long overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public Integer getBalanceCcy() {
        return balanceCcy;
    }

    public void setBalanceCcy(Integer balanceCcy) {
        this.balanceCcy = balanceCcy;
    }

    public Long getBookedBalance() {
        return bookedBalance;
    }

    public void setBookedBalance(Long bookedBalance) {
        this.bookedBalance = bookedBalance;
    }

    public Long getLimitInform() {
        return limitInform;
    }

    public void setLimitInform(Long limitInform) {
        this.limitInform = limitInform;
    }

    public Long getLimitDisable() {
        return limitDisable;
    }

    public void setLimitDisable(Long limitDisable) {
        this.limitDisable = limitDisable;
    }

    public String getContactGroup() {
        return contactGroup;
    }

    public void setContactGroup(String contactGroup) {
        this.contactGroup = contactGroup;
    }

    public Boolean getBlocked() {
        return blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }

    public Boolean getUnlimit() {
        return unlimit;
    }

    public void setUnlimit(Boolean unlimit) {
        this.unlimit = unlimit;
    }

    public DepositState getState() {
        return state;
    }

    public void setState(DepositState state) {
        this.state = state;
    }

    public GatewayDTO getGateway() {
        return gateway;
    }

    public void setGateway(GatewayDTO gateway) {
        this.gateway = gateway;
    }

    public AgentDTO getAgent() {
        return agent;
    }

    public void setAgent(AgentDTO agent) {
        this.agent = agent;
    }

    public VirtualAccountDTO getVirtualAccount() {
        return virtualAccount;
    }

    public void setVirtualAccount(VirtualAccountDTO virtualAccount) {
        this.virtualAccount = virtualAccount;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DepositDTO)) {
            return false;
        }

        DepositDTO depositDTO = (DepositDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, depositDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DepositDTO{" +
            "id=" + getId() +
            ", overdraftLimit=" + getOverdraftLimit() +
            ", balance=" + getBalance() +
            ", balanceCcy=" + getBalanceCcy() +
            ", bookedBalance=" + getBookedBalance() +
            ", limitInform=" + getLimitInform() +
            ", limitDisable=" + getLimitDisable() +
            ", contactGroup='" + getContactGroup() + "'" +
            ", blocked='" + getBlocked() + "'" +
            ", unlimit='" + getUnlimit() + "'" +
            ", state='" + getState() + "'" +
            ", gateway=" + getGateway() +
            ", agent=" + getAgent() +
            ", virtualAccount=" + getVirtualAccount() +
            "}";
    }
}
