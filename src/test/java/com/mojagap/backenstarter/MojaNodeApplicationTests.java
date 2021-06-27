package com.mojagap.backenstarter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MojaNodeApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(System.getProperty("user.dir"));
        System.out.println(MojaNodeApplicationTests.class.getSimpleName());
        System.out.println(MojaNodeApplicationTests.class.getName());
        System.out.println(MojaNodeApplicationTests.class.getCanonicalName());
    }

}
