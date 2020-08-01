package live.sidian.local_net_expose_client;

import com.fasterxml.jackson.databind.ObjectMapper;
import live.sidian.local_net_expose_client.application.ExposeClient;
import live.sidian.local_net_expose_client.instrastructure.api.ServerApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Objects;

@Slf4j
@EnableFeignClients
@SpringBootApplication
public class LocalNetExposeClientApplication implements CommandLineRunner {
    @Resource
    ServerApi serverApi;
    @Resource
    ExposeClient client;


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
        client.login();
        // 没主线程啥事了, 睡觉
        while (true) {
            Thread.sleep(5000);
        }
    }

    private void waitServerStart() throws InterruptedException {
        String status = null;
        do {
            try {
                status = serverApi.getServerStatus();
            } catch (Exception e) {
                log.info("等待Server正常运行, e:" + e.getMessage());
            }
        } while (!Objects.equals(status, "ready"));
    }
}
