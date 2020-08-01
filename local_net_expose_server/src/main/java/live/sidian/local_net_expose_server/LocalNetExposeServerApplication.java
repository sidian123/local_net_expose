package live.sidian.local_net_expose_server;

import live.sidian.local_net_expose_common.util.SpringUtil;
import live.sidian.local_net_expose_server.application.ClientManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@EnableAsync
@EnableJpaRepositories
@SpringBootApplication
public class LocalNetExposeServerApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(LocalNetExposeServerApplication.class, args);
    }

    @Resource
    ClientManageService clientManageService;

    @Override
    public void run(String... args) {
        try {
            clientManageService.init();
        } catch (IOException e) {
            System.out.println("端口监听失败");
            SpringUtil.closeApplication();
        }
    }
}
