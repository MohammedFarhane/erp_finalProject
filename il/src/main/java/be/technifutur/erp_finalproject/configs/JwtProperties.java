package be.technifutur.erp_finalproject.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "erp.jwt")
public record JwtProperties(
        String secret,
        long expiration
){
}
