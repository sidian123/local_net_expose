package live.sidian.local_net_expose_server.application.client_manage.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.Socket;
import java.util.List;

/**
 * @author sidian
 * @date 2020/8/18 1:14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientVo {
    /**
     * 客户id
     */
    Long id;
    /**
     * 应用名
     */
    String name;

    /**
     * 与客户端建立的流
     */
    Socket socket;

    /**
     * 所有穿透记录
     */
    List<ExposeRecordVo> exposeRecords;
}
