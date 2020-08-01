package live.sidian.local_net_expose_server.persistence.dao;

import live.sidian.local_net_expose_server.persistence.model.ExposeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author sidian
 * @date 2020/7/24 23:12
 */
@Repository
public interface ExposeRecordDao extends JpaRepository<ExposeRecord, Long> {
    List<ExposeRecord> findAllByClientIdAndStatus(Long clientId, Integer status);

    ExposeRecord findByClientIdAndClientPort(Long clientId, Long clientPort);

    ExposeRecord findAllById(Long id);

    List<ExposeRecord> findAllByClientId(Long clientId);
}
