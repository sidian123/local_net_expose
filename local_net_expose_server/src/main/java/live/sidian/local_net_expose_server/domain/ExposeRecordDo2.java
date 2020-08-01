package live.sidian.local_net_expose_server.domain;

import live.sidian.local_net_expose_server.persistence.model.ExposeRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * 一个穿透记录的运行实例
 *
 * @author sidian
 * @date 2020/7/25 10:21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Deprecated
public class ExposeRecordDo2 {
    ExposeRecord exposeRecord;
    /**
     * server上监听暴露端口的线程
     */
    Thread serverListeningThread;

    /**
     * 处理server port上建立的连接的线程, 一个连接对应一个线程
     */
    @Builder.Default
    List<Thread> channelThreads = new LinkedList<>();

}
