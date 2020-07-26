package live.sidian.local_net_expose_client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Objects;

@Slf4j
@SpringBootApplication
public class LocalNetExposeClientApplication implements CommandLineRunner {
    @Resource
    ClientTransferStation clientTransferStation;

    @Value("${expose.server.url-prefix}")
    String urlPrefix;


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
        // 等待server启动
        waitServerStart();
        // 初始化
        clientTransferStation.init();
        // 没主线程啥事了, 睡觉
        while (true) {
            Thread.sleep(5000);
        }
    }

    private void waitServerStart() throws InterruptedException {
        String status = null;
        do {
            try {
                status = restTemplate().getForObject(urlPrefix + "/server/status", String.class);
            } catch (Exception e) {
                log.info("等待Server正常运行, e:" + e.getMessage());
            }
            Thread.sleep(1000);
        } while (!Objects.equals(status, "ready"));
    }
}
