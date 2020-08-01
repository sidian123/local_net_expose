package live.sidian.local_net_expose_server.application.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.thread.ThreadUtil;
import live.sidian.local_net_expose_common.infrastructure.Command;
import live.sidian.local_net_expose_server.application.ClientManageService;
import live.sidian.local_net_expose_server.domain.AppStatus;
import live.sidian.local_net_expose_server.domain.ChannelBuilder;
import live.sidian.local_net_expose_server.domain.ClientDo;
import live.sidian.local_net_expose_server.exception.ClientNotRegister;
import live.sidian.local_net_expose_server.infrastructure.ExposeRecordStatus;
import live.sidian.local_net_expose_server.persistence.dao.ClientDao;
import live.sidian.local_net_expose_server.persistence.dao.ExposeRecordDao;
import live.sidian.local_net_expose_server.persistence.model.Client;
import live.sidian.local_net_expose_server.persistence.model.ExposeRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sidian
 * @date 2020/7/31 21:34
 */
@Slf4j
@Service
public class ClientManageServiceImpl implements ClientManageService {
    /**
     * 所有已连接的客户端
     */
    Map<Long, ClientDo> allClients = new HashMap<>();

    /**
     * 监听的端口
     */
    @Value("${expose.server.client-listen-port}")
    int clientListenPort;

    /**
     * 外网端口与将要建立通道的隧道之间的映射
     */
    Map<Long, ChannelBuilder> willBuildChannels = new HashMap<>();

    @Resource
    ExposeRecordDao exposeRecordDao;
    @Resource
    ClientDao clientDao;

    /**
     * 初始化连接客户端的监听器
     *
     * @throws IOException 监听失败时抛出
     */
    @PostConstruct
    @Override
    public void init() throws IOException {
        // 监听端口
        log.info(String.format("监听端口: %d, 等待客户端连接", clientListenPort));
        ServerSocket serverSocket = new ServerSocket(clientListenPort);
        AppStatus.status = AppStatus.AppStatusConstant.READY;
        // 等待客户端连接, 与客户端沟通
        ThreadUtil.execute(() -> {
            while (true) {
                try {
                    // 等待连接
                    Socket socket = serverSocket.accept();
                    log.info("接收到来自客户端的连接");
                    // 读取客户端命令
                    int command = parseCommand(socket);
                    log.info("收到命令:" + command);
                    // 执行命令
                    switch (command) {
                        case Command.LOGIN: // 登入server
                            log.info("客户端登录");
                            // 认证
                            ClientDo client = authenticate(socket);
                            log.info(String.format("客户端%d登录成功", client.getId()));
                            // 记录
                            allClients.put(client.getId(), client);
                            // 监听client外网端口
                            listen(client);
                            break;
                        case Command.BUILD_CHANNEL: // 与server构建隧道
                            buildChannel(socket);
                            break;
                        default: // 其他情况, 忽略
                            socket.close();
                    }
                } catch (IOException | ClientNotRegister e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void buildChannel(Socket socket) throws IOException {
        // 获取外网暴露端口信息
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        long serverPort = inputStream.readLong();
        log.info(String.format("客户端与Server的%d端口构建隧道", serverPort));
        // 构建
        ChannelBuilder channelBuilder = willBuildChannels.get(serverPort);
        if (channelBuilder == null) {
            socket.close();
            return;
        }
        if (!channelBuilder.isOutSocketOk()) { // 通道失效
            willBuildChannels.remove(serverPort);
            return;
        }
        channelBuilder.setLocalSocket(socket);
        channelBuilder.build();
        willBuildChannels.remove(serverPort);
    }

    private int parseCommand(Socket socket) throws IOException {
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        return inputStream.readInt();
    }

    private void listen(ClientDo client) {
        for (ExposeRecord exposeRecord : client.getExposeRecords()) {
            // 监听
            ThreadUtil.execute(() -> {
                try {
                    log.info(String.format("暴露客户端%d的%d端口到server的%d端上",
                            client.getId(), exposeRecord.getClientPort(), exposeRecord.getServerPort()));
                    listen(exposeRecord, client);
                } catch (IOException e) {
                    log.error("监听客户端失败", e);
                    exposeRecord.setStatus(ExposeRecordStatus.exception);
                }
            });
        }
    }

    private void listen(ExposeRecord exposeRecord, ClientDo client) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(Math.toIntExact(exposeRecord.getServerPort()))) {
            while (true) {
                // 外网访问
                Socket socket = serverSocket.accept();
                log.info(String.format("客户端%d穿透的外网端口%d接收到请求, 即将构建传输隧道, 发送到内网端口%d",
                        client.getId(), exposeRecord.getServerPort(), exposeRecord.getClientPort()));
                // 要求client构建隧道
                DataOutputStream outputStream = new DataOutputStream(client.getSocket().getOutputStream());
                outputStream.writeInt(Command.BUILD_CHANNEL);
                outputStream.writeLong(exposeRecord.getServerPort());
                willBuildChannels.put(exposeRecord.getServerPort(), new ChannelBuilder(socket));
            }
        }
    }


    private ClientDo authenticate(Socket socket) throws IOException, ClientNotRegister {
        // 读取客户端信息
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        long clientId = inputStream.readLong(); // 客户端id
        Client client = clientDao.findById(clientId).orElse(null);
        if (client == null) throw new ClientNotRegister(String.format("客户端%d未注册", clientId));
        // TODO 认证
        // 补全对象
        List<ExposeRecord> exposeRecords = exposeRecordDao.findAllByClientId(clientId);
        ClientDo clientDo = ClientDo.builder()
                .socket(socket)
                .exposeRecords(exposeRecords)
                .build();
        BeanUtil.copyProperties(client, clientDo);
        return clientDo;
    }

}
