package live.sidian.local_net_expose_server.application;

import live.sidian.local_net_expose_server.infrastructure.ExposeRecordStatus;
import live.sidian.local_net_expose_server.persistence.dao.ExposeRecordDao;
import live.sidian.local_net_expose_server.persistence.model.ExposeRecord;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @author sidian
 * @date 2020/7/25 16:55
 */
@Service
@Transactional
public class ClientService {
    @Resource
    ExposeRecordDao exposeRecordDao;

    public List<ExposeRecord> getExposeRecord(long clientId) {
        return exposeRecordDao.findAllByClientIdAndStatus(clientId, ExposeRecordStatus.enable);
    }
}
