package cz.stavbau.backend.features.registrations.service.impl;

import cz.stavbau.backend.features.registrations.service.CaptchaService;

import java.net.InetAddress;

public class CaptchaServiceMock implements CaptchaService {
    @Override
    public void verify(String captchaToken, InetAddress ip) {
        // Local/stage mock: vždy OK. Audit a skutečnou verifikaci přidáme v recaptcha implementaci.
    }
}
