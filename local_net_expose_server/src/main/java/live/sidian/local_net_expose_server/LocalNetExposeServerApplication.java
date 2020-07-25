package live.sidian.local_net_expose_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableJpaRepositories
@SpringBootApplication
public class LocalNetExposeServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(LocalNetExposeServerApplication.class, args);
    }
}
