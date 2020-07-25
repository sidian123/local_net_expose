package live.sidian.local_net_expose_client.util;

import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author sidian
 * @date 2020/7/25 17:59
 */
@Component
public class SpringUtil implements ApplicationContextAware {
    static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtil.applicationContext = applicationContext;
    }

    public static void closeApplication(int exit) {
        SpringApplication.exit(applicationContext, () -> exit);
    }

    public static void closeApplication() {
        closeApplication(0);
    }
}
