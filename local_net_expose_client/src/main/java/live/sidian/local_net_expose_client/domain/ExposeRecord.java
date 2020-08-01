package live.sidian.local_net_expose_client.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @author sidian
 * @date 2020/7/25 17:10
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
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
     * 状态. 0启用, 1禁用
     */
    Integer status;

    /**
     * 应用id
     */
    Long clientId;
}
