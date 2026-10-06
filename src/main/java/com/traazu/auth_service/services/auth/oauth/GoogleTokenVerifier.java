package com.traazu.auth_service.services.auth.oauth;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class GoogleTokenVerifier {

    private final JwtDecoder jwtDecoder;

    public GoogleTokenVerifier(
            @Value("${google.client-id}") String googleClientId) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();

        OAuth2TokenValidator<Jwt> issuerValidator =
                JwtValidators.createDefaultWithIssuer(
                        "https://accounts.google.com"
                );

        // اصلاح شد: aud به صورت List خوانده می‌شود
        OAuth2TokenValidator<Jwt> audienceValidator =
                new JwtClaimValidator<List<String>>(
                        "aud",
                        aud -> aud != null && aud.contains(googleClientId)
                );

        OAuth2TokenValidator<Jwt> validator =
                new DelegatingOAuth2TokenValidator<>(
                        issuerValidator,
                        audienceValidator
                );

        decoder.setJwtValidator(validator);

        this.jwtDecoder = decoder;
    }

    public Jwt verify(String idToken) {

        Jwt jwt = jwtDecoder.decode(idToken);

        String subject = jwt.getSubject();

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException(
                    "Google ID token does not contain a subject"
            );
        }

        Boolean emailVerified =
                jwt.getClaimAsBoolean("email_verified");

        if (!Boolean.TRUE.equals(emailVerified)) {
            throw new IllegalArgumentException(
                    "Google email has not been verified"
            );
        }

        String email = jwt.getClaimAsString("email");

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Google ID token does not contain an email"
            );
        }

        return jwt;
    }

}