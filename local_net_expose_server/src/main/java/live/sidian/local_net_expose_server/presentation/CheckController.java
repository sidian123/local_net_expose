package live.sidian.local_net_expose_server.presentation;

import live.sidian.local_net_expose_server.application.CheckService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author sidian
 * @date 2020/7/26 12:12
 */
@RestController
@RequestMapping("/check")
public class CheckController {
    @Resource
    CheckService checkService;

    @GetMapping("/channels")
    public String checkChannels() {
        return checkService.checkChannels();
    }

}
