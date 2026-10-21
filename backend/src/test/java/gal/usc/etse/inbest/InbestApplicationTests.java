package gal.usc.etse.inbest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@Import(PostgresTestConfiguration.class)
class InbestApplicationTests {

    @Test
    void contextLoads() {
    }

}
