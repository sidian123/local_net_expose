package live.sidian.local_net_expose_client.instrastructure.api;

import live.sidian.local_net_expose_client.ExposeRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author sidian
 * @date 2020/8/1 11:31
 */
@FeignClient(name = "server", url = "${expose.server.url-prefix}")
@ResponseBody
public interface ServerApi {
    @GetMapping("/server/status")
    String getServerStatus();

    @GetMapping("/expose_record/get")
    List<ExposeRecord> getExposeRecord(long clientId);
}
