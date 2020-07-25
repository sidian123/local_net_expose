package live.sidian.local_net_expose_server.persistence.dao;

import live.sidian.local_net_expose_server.persistence.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author sidian
 * @date 2020/7/24 23:11
 */
@Repository
public interface ClientDao extends JpaRepository<Client, Long> {

}
