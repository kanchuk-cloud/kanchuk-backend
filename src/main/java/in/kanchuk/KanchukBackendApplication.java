package in.kanchuk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KanchukBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(KanchukBackendApplication.class, args);
    }
}
