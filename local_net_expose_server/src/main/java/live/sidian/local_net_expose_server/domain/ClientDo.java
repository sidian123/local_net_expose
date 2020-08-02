package live.sidian.local_net_expose_server.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.Socket;
import java.util.List;

/**
 * 客户端
 *
 * @author sidian
 * @date 2020/7/31 21:02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDo {
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
    List<ExposeRecordDo> exposeRecords;
}
