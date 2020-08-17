package live.sidian.local_net_expose_server.application.client_manage.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author sidian
 * @date 2020/8/18 1:07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerStatus {
    @Builder.Default
    String name = "内网穿透服务端";
    @Builder.Default
    String version = "v1.0";
    List<ClientVo> clientInfos;
}
