package live.sidian.local_net_expose_server.infrastructure;

/**
 * @author sidian
 * @date 2020/7/31 21:18
 */
public interface ExposeRecordStatus {
    /**
     * 启用
     */
    int enable = 0;
    /**
     * 禁用
     */
    int disable = 1;
    /**
     * 异常了
     */
    int exception = 2;
}
