package live.sidian.local_net_expose_server.application.client_manage.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.ServerSocket;

/**
 * @author sidian
 * @date 2020/8/18 1:13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExposeRecordVo {
    Long id;

    /**
     * server上监听端口
     */
    Long serverPort;

    ServerSocket serverSocket;

    /**
     * client上暴露端口
     */
    Long clientPort;

    /**
     * 局域网内的主机
     */
    String localhost;

    /**
     * 状态. 0启用, 1禁用
     */
    Integer status;

    /**
     * 应用id
     */
    Long clientId;

    /**
     * 请求数
     */
    Long requestNum;
}
