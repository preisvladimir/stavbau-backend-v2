package cz.stavbau.backend.features.registrations.service;


import java.net.InetAddress;

public interface CaptchaService {
    void verify(String captchaToken, InetAddress ip);
}
