package live.sidian.local_net_expose_server.application;

/**
 * @author sidian
 * @date 2020/7/25 10:46
 */
public interface TCPSocketServer {
    /**
     * 穿透客户端注册的所有穿透
     *
     * @param clientId 客户端ID
     */
    void expose(long clientId);

    /**
     * 关闭客户端所有穿透
     *
     * @param clientId 客户端ID
     */
    void close(long clientId);

    /**
     * 刷新某一穿透记录状态
     *
     * @param exposeRecordId 客户端ID
     */
    void refreshExposeRecord(long exposeRecordId);
}
