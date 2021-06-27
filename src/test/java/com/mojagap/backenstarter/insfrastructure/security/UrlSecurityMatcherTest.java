package com.mojagap.backenstarter.insfrastructure.security;

import com.mojagap.backenstarter.infrastructure.security.UrlSecurityMatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class UrlSecurityMatcherTest {

    @Test
    void testMatchMethod_ReturnsCorrectStatus() {
        boolean status = UrlSecurityMatcher.matches("/v1/account/*", "/v1/account/1");
        Assertions.assertTrue(status);
        status = UrlSecurityMatcher.matches("/**", "/v1/account/1/Activate/COMPANY");
        Assertions.assertTrue(status);
        status = UrlSecurityMatcher.matches("/v1/user/reject", "/V1/USER/REJECT");
        Assertions.assertTrue(status);
        status = UrlSecurityMatcher.matches("/v1/user/*/company/*", "/v1/user/1/Activate/67");
        Assertions.assertFalse(status);
        status = UrlSecurityMatcher.matches("/v1/user/*/company/*", "/v1/user/1/company/67");
        Assertions.assertTrue(status);
        status = UrlSecurityMatcher.matches("/v1/role/**", "/v1/role");
        Assertions.assertTrue(status);
        status = UrlSecurityMatcher.matches("/v1/role/", "/v1/role");
        Assertions.assertTrue(status);
    }
}
