package live.sidian.local_net_expose_server.application;

import live.sidian.local_net_expose_server.domain.ForwardChannel;
import live.sidian.local_net_expose_server.persistence.dao.ExposeRecordDao;
import live.sidian.local_net_expose_server.persistence.model.ExposeRecord;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.Map;

/**
 * @author sidian
 * @date 2020/7/26 12:20
 */
//@Service
@Deprecated
@Transactional
public class CheckService {
    @Resource
    TCPSocketServerImpl tcpSocketServer;
    @Resource
    ExposeRecordDao exposeRecordDao;

    public String checkChannels() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Long, ForwardChannel> entry : tcpSocketServer.getChannelMap().entrySet()) {
            ExposeRecord exposeRecord = exposeRecordDao.findAllById(entry.getKey());
            builder.append(String.format("暴露端口:%d\n内网端口:%d\n%s",
                    exposeRecord.getServerPort(), exposeRecord.getClientPort(), entry.getValue().toString()));
            builder.append("\n\n");
        }
        return builder.toString();
    }
}
