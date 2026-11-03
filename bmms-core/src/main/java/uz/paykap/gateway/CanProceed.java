package uz.paykap.gateway;

public interface CanProceed {
    boolean approve(Long providerId, Long serviceId);
}
