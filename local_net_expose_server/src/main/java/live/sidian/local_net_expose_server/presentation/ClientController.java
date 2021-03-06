package live.sidian.local_net_expose_server.presentation;

import live.sidian.local_net_expose_server.application.ClientService;
import live.sidian.local_net_expose_server.domain.AppStatus;
import live.sidian.local_net_expose_server.persistence.model.ExposeRecord;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 提供给客户端使用的接口
 *
 * @author sidian
 * @date 2020/7/25 16:51
 */
@RestController
@RequestMapping("/client")
public class ClientController {
    @Resource
    ClientService clientService;

    @GetMapping("/expose_record/get")
    public List<ExposeRecord> getExposeRecord(long clientId) {
        return clientService.getExposeRecord(clientId);
    }

    @GetMapping("/server/status")
    public String getServerStatus() {
        return AppStatus.status;
    }
}
