package live.sidian.local_net_expose_server.infrastructure;

/**
 * @author sidian
 * @date 2020/7/31 22:52
 */
public interface Command {
    /**
     * 与server建立连接
     */
    int CONNECT_SERVER = 0;
    /**
     * 与server或client构建隧道
     */
    int BUILD_CHANNEL = 1;
}
