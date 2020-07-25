package live.sidian.local_net_expose_client;

import lombok.Data;

/**
 * @author sidian
 * @date 2020/7/25 17:10
 */
@Data
public class ExposeRecord {
    Long id;
    /**
     * server上暴露端口
     */
    Long serverPort;
    /**
     * client上暴露端口
     */
    Long clientPort;

    /**
     * 应用id
     */
    Long clientId;
}
