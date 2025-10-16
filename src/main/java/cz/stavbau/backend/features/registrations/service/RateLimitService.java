package cz.stavbau.backend.features.registrations.service;

import java.net.InetAddress;

public interface RateLimitService {
    void checkStartAllowance(String email, InetAddress ip);
    void onStartCommitted(String email, InetAddress ip);
    void checkResendAllowance(String email, InetAddress ip);
    void onResendCommitted(String email, InetAddress ip);
}
