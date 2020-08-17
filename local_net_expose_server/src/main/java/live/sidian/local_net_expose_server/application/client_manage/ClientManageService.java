package live.sidian.local_net_expose_server.application.client_manage;

import live.sidian.local_net_expose_server.application.client_manage.entity.ServerStatus;

import java.io.IOException;

/**
 * 客户端管理服务
 *
 * @author sidian
 * @date 2020/7/31 20:58
 */
public interface ClientManageService {


    /**
     * 初始化连接客户端的监听器
     */
    void init() throws IOException;

    /**
     * 获取服务端状态信息
     */
    ServerStatus getServerStatus();
}
