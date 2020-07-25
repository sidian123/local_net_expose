package live.sidian.local_net_expose_client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@SpringBootApplication
public class LocalNetExposeClientApplication implements CommandLineRunner {
    @Resource
    ClientTransferStation clientTransferStation;

    public static void main(String[] args) {
        SpringApplication.run(LocalNetExposeClientApplication.class, args);
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }


    @Override
    public void run(String... args) throws Exception {
        // 初始化
        clientTransferStation.init();
        // 没主线程啥事了, 睡觉
        while (true) {
            Thread.sleep(5000);
        }
    }
}
