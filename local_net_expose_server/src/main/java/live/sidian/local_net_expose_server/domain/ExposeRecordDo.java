package live.sidian.local_net_expose_server.domain;

import cn.hutool.core.bean.BeanUtil;
import live.sidian.local_net_expose_server.persistence.model.ExposeRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.ServerSocket;

/**
 * @author sidian
 * @date 2020/8/2 17:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExposeRecordDo {
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

    public static ExposeRecordDo of(ExposeRecord exposeRecord) {
        ExposeRecordDo exposeRecordDo = new ExposeRecordDo();
        BeanUtil.copyProperties(exposeRecord, exposeRecordDo);
        return exposeRecordDo;
    }
}
