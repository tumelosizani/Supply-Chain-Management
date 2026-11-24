package dev.dini.scms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "scms.seed.enabled=false")
class ScmsApplicationTests {

    @Test
    void contextLoads() {
    }

}
