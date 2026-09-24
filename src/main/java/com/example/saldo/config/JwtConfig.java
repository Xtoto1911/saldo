package com.example.saldo.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Bean
    public SecretKey accessSecretKey(
            @Value("${security.jwt.access-secret}") String secret
    ) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);

        return new SecretKeySpec(
                keyBytes,
                "HmacSHA256"
        );
    }


    @Bean
    public JwtEncoder accessJwtEncoder(
            @Qualifier("accessSecretKey") SecretKey accessSecretKey
    ) {
        return NimbusJwtEncoder
                .withSecretKey(accessSecretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }


    @Bean
    public JwtDecoder accessJwtDecoder(
            @Qualifier("accessSecretKey") SecretKey accessSecretKey
    ) {
        return NimbusJwtDecoder
                .withSecretKey(accessSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }


    @Bean
    public SecretKey refreshSecretKey(
            @Value("${security.jwt.refresh-secret}") String secret
    ) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);

        return new SecretKeySpec(
                keyBytes,
                "HmacSHA256"
        );
    }


    @Bean
    public JwtEncoder refreshJwtEncoder(
            @Qualifier("refreshSecretKey") SecretKey refreshSecretKey
    ) {
        return NimbusJwtEncoder
                .withSecretKey(refreshSecretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }


    @Bean
    public JwtDecoder refreshJwtDecoder(
            @Qualifier("refreshSecretKey") SecretKey refreshSecretKey
    ) {
        return NimbusJwtDecoder
                .withSecretKey(refreshSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

}
