package uz.paykap.gateway.impl;

import org.springframework.stereotype.Service;
import uz.paykap.gateway.CanProceed;

@Service
public class AlwaysCanProceed implements CanProceed {
    public boolean approve(Long providerId, Long serviceId) {
        return true;
    }
}
