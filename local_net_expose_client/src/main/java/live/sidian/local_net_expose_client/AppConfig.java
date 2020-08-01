package live.sidian.local_net_expose_client;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author sidian
 * @date 2020/8/1 19:48
 */
@Data
@Component
public class AppConfig {
    /**
     * 是否显示传输内容
     */
    @Value("${expose.show-content:false}")
    boolean showContent;
}
