package uz.paykap.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;

import org.javers.core.metamodel.annotation.DiffIgnore;
import uz.paykap.domain.enumeration.DepositState;

/**
 * Баланс агента
 */
@Entity
@Table(name = "deposit")
@DiffIgnore
public class Deposit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Version
    private Long version;

    /**
     * Размер овердрафта
     */
    @NotNull
    @Column(name = "overdraft_limit", nullable = false)
    private Long overdraftLimit;

    /**
     * Сумма доступного баланса
     */
    @NotNull
    @Column(name = "balance", nullable = false)
    private Long balance;

    /**
     * Валюта баланса
     */
    @NotNull
    @Column(name = "balance_ccy", nullable = false)
    private Integer balanceCcy;

    /**
     * Зарезервированный баланса
     */
    @NotNull
    @Column(name = "booked_balance", nullable = false)
    private Long bookedBalance;

    /**
     * Порог уведомления при достижении лимита
     */
    @NotNull
    @Column(name = "limit_inform", nullable = false)
    private Long limitInform;

    /**
     * Порог остановки оплат
     */
    @NotNull
    @Column(name = "limit_disable", nullable = false)
    private Long limitDisable;

    /**
     * Группа контактов для уведомления
     */
    @Size(max = 64)
    @Column(name = "contact_group", length = 64)
    private String contactGroup;

    /**
     * Заблокирован
     */
    @NotNull
    @Column(name = "blocked", nullable = false)
    private Boolean blocked;

    /**
     * Без ограничения
     */
    @NotNull
    @Column(name = "unlimit", nullable = false)
    private Boolean unlimit;

    /**
     * Состояние
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private DepositState state;

    /**
     * Связанный Шлюз
     */
    @JsonIgnoreProperties(value = { "deposit" }, allowSetters = true)
    @OneToOne
    @JoinColumn(unique = true)
    private Gateway gateway;

    /**
     * Связанный Агент
     */
    @JsonIgnoreProperties(value = { "titles", "deposit" }, allowSetters = true)
    @OneToOne
    @JoinColumn(unique = true)
    private Agent agent;

    /**
     * Связанный виртуальный счет
     */
    @JsonIgnoreProperties(value = { "deposit" }, allowSetters = true)
    @OneToOne
    @JoinColumn(unique = true)
    private VirtualAccount virtualAccount;

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Deposit id(Long id) {
        this.id = id;
        return this;
    }

    public Long getOverdraftLimit() {
        return this.overdraftLimit;
    }

    public Deposit overdraftLimit(Long overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
        return this;
    }

    public void setOverdraftLimit(Long overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    public Long getBalance() {
        return this.balance;
    }

    public Deposit balance(Long balance) {
        this.balance = balance;
        return this;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public Integer getBalanceCcy() {
        return this.balanceCcy;
    }

    public Deposit balanceCcy(Integer balanceCcy) {
        this.balanceCcy = balanceCcy;
        return this;
    }

    public void setBalanceCcy(Integer balanceCcy) {
        this.balanceCcy = balanceCcy;
    }

    public Long getBookedBalance() {
        return this.bookedBalance;
    }

    public Deposit bookedBalance(Long bookedBalance) {
        this.bookedBalance = bookedBalance;
        return this;
    }

    public void setBookedBalance(Long bookedBalance) {
        this.bookedBalance = bookedBalance;
    }

    public Long getLimitInform() {
        return this.limitInform;
    }

    public Deposit limitInform(Long limitInform) {
        this.limitInform = limitInform;
        return this;
    }

    public void setLimitInform(Long limitInform) {
        this.limitInform = limitInform;
    }

    public Long getLimitDisable() {
        return this.limitDisable;
    }

    public Deposit limitDisable(Long limitDisable) {
        this.limitDisable = limitDisable;
        return this;
    }

    public void setLimitDisable(Long limitDisable) {
        this.limitDisable = limitDisable;
    }

    public String getContactGroup() {
        return this.contactGroup;
    }

    public Deposit contactGroup(String contactGroup) {
        this.contactGroup = contactGroup;
        return this;
    }

    public void setContactGroup(String contactGroup) {
        this.contactGroup = contactGroup;
    }

    public Boolean getBlocked() {
        return this.blocked;
    }

    public Boolean isBlocked() {
        return this.blocked;
    }

    public Deposit blocked(Boolean blocked) {
        this.blocked = blocked;
        return this;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }

    public Boolean getUnlimit() {
        return this.unlimit;
    }

    public Boolean isUnlimit() {
        return this.unlimit;
    }

    public Deposit unlimit(Boolean unlimit) {
        this.unlimit = unlimit;
        return this;
    }

    public void setUnlimit(Boolean unlimit) {
        this.unlimit = unlimit;
    }

    public DepositState getState() {
        return this.state;
    }

    public Deposit state(DepositState state) {
        this.state = state;
        return this;
    }

    public void setState(DepositState state) {
        this.state = state;
    }

    public Gateway getGateway() {
        return this.gateway;
    }

    public Deposit gateway(Gateway gateway) {
        this.setGateway(gateway);
        return this;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    public Agent getAgent() {
        return this.agent;
    }

    public Deposit agent(Agent agent) {
        this.setAgent(agent);
        return this;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public VirtualAccount getVirtualAccount() {
        return this.virtualAccount;
    }

    public Deposit virtualAccount(VirtualAccount virtualAccount) {
        this.setVirtualAccount(virtualAccount);
        return this;
    }

    public void setVirtualAccount(VirtualAccount virtualAccount) {
        this.virtualAccount = virtualAccount;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Deposit)) {
            return false;
        }
        return id != null && id.equals(((Deposit) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Deposit{" +
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
            "}";
    }
}
