package live.sidian.local_net_expose_server.application;

import live.sidian.local_net_expose_server.domain.ForwardChannel;
import live.sidian.local_net_expose_server.persistence.dao.ExposeRecordDao;
import live.sidian.local_net_expose_server.persistence.model.ExposeRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sidian
 * @date 2020/7/24 22:36
 */
@Slf4j
@Service
public class TCPSocketServerImpl implements TCPSocketServer {
    /**
     * 与暴露端口相关的线程. 穿透记录id与监听线程的映射
     */
    Map<Long, Thread> exposeListenThreadMap = new HashMap<>();

    /**
     * 监听内网连接的线程
     */
    Thread clientListenThread;

    /**
     * socket连接通道的集合. 穿透记录与通道的映射
     */
    Map<Long, ForwardChannel> channelMap = new HashMap<>();


    @Lazy
    @Resource
    TCPSocketServerImpl self;

    @Resource
    ExposeRecordDao exposeRecordDao;
    @Value("${expose.server.listen-client-port}")
    int clientListenPort;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        // 监听内网客户端连接
        clientListenThread = new Thread(this::listenClientConnection);
    }

    /**
     * 监听内网客户端连接
     */
    private void listenClientConnection() {
        try {
            // 监听
            ServerSocket serverSocket = new ServerSocket(clientListenPort);
            // 处理连接
            while (true) {
                // 等待连接
                Socket socket = serverSocket.accept();
                // 记录
                if (!recordClientSocket(socket)) { // 记录失败
                    socket.close();
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 记录socket到channel中
     *
     * @param socket 套接字
     * @return 是否成功记录
     */
    private boolean recordClientSocket(Socket socket) throws IOException {
        // 唯一标识socket
        int port = socket.getPort(); // 远程端口
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        long clientId = dataInputStream.readLong(); // 客户端id
        // 找到对应穿透记录
        ExposeRecord exposeRecord = exposeRecordDao.findByClientIdAndClientPort(clientId, (long) port);
        if (exposeRecord == null) { // 数据库无配置, 即不允许连接
            return false;
        }
        // 记录
        ForwardChannel channel;
        if ((channel = channelMap.get(exposeRecord.getId())) != null) { // 已存在通道
            channel.setClientSocket(socket);
        } else { // 不存在通道
            channel = ForwardChannel.builder().clientSocket(socket).build();
        }
        // 建立输入输出流联系
        channel.init();
        return true;
    }

    /**
     * 穿透客户端注册的所有穿透
     *
     * @param clientId 客户端ID
     */
    @Override
    public void expose(long clientId) {
        // 获取客户端所有可用穿透配置
        List<ExposeRecord> exposeRecordList = exposeRecordDao.findAllByClientIdAndStatus(
                clientId, ExposeRecord.ExposeRecordStatus.enable);
        // 判断是否已注册过了
        boolean present = exposeRecordList.stream()
                .anyMatch(exposeRecord -> exposeListenThreadMap.get(exposeRecord.getId()) != null);
        if (present) { // 有注册过
            return;
        }
        // 初始化暴露端口的监听线程
        for (ExposeRecord exposeRecord : exposeRecordList) {
            // 监听端口
            exposeListenThreadMap.put(exposeRecord.getId(), new Thread(() -> listen(exposeRecord)));
        }
    }

    /**
     * 监听穿透配置要暴露的接口
     *
     * @param exposeRecord 穿透配置
     */
    void listen(ExposeRecord exposeRecord) {
        try {
            // 监听端口
            ServerSocket serverSocket = new ServerSocket(Math.toIntExact(exposeRecord.getServerPort()));
            // 处理请求
            while (true) {
                // 等待连接
                Socket socket = serverSocket.accept();
                // 记录
                ForwardChannel channel;
                if ((channel = channelMap.get(exposeRecord.getId())) != null) { // 已存在通道
                    channel.setExposeSocket(socket);
                } else { // 不存在通道
                    channel = ForwardChannel.builder().exposeSocket(socket).build();
                }
                // 建立输入输出流联系
                channel.init();
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }


    /**
     * 刷新某一穿透记录状态
     *
     * @param exposeRecordId 客户端ID
     */
    @Override
    public void refreshExposeRecord(long exposeRecordId) {

    }

    /**
     * 关闭客户端所有穿透
     *
     * @param clientId 客户端ID
     */
    @Override
    public void close(long clientId) {

    }


}

















