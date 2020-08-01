package live.sidian.local_net_expose_common.infrastructure;

/**
 * @author sidian
 * @date 2020/7/31 22:52
 */
public interface Command {
    /**
     * 与server建立连接
     */
    int LOGIN = 0;
    /**
     * 与server或client构建隧道
     */
    int BUILD_CHANNEL = 1;
}
