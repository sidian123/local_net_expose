package live.sidian.local_net_expose_client.application.impl;

import cn.hutool.core.thread.ThreadUtil;
import live.sidian.local_net_expose_client.application.ExposeClient;
import live.sidian.local_net_expose_client.domain.ExposeRecord;
import live.sidian.local_net_expose_client.instrastructure.api.ServerApi;
import live.sidian.local_net_expose_common.infrastructure.Command;
import live.sidian.local_net_expose_common.infrastructure.TransmitChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author sidian
 * @date 2020/8/1 11:27
 */
@Slf4j
@Service
public class ExposeClientImpl implements ExposeClient {
    /**
     * 与server传输命令的连接
     */
    Socket commandSocket;

    /**
     * server上穿透的端口与穿透记录的映射
     */
    Map<Long, ExposeRecord> exposeRecordMap;

    @Value("${expose.server.port}")
    int serverPort;
    @Value("${expose.server.host}")
    String serverHost;
    @Value("${expose.client.id}")
    long clientId;
    @Resource
    ServerApi serverApi;

    /**
     * 登入server
     */
    @Override
    public void login() throws IOException {
        // 获取客户端信息
        log.info("获取客户端信息");
        exposeRecordMap = serverApi.getExposeRecord(clientId).stream()
                .collect(Collectors.toMap(ExposeRecord::getServerPort, exposeRecord -> exposeRecord));
        // 连接服务端
        log.info(String.format("连接服务器%s:%d", serverHost, serverPort));
        commandSocket = new Socket(serverHost, serverPort);
        DataOutputStream outputStream = new DataOutputStream(commandSocket.getOutputStream());
        outputStream.writeInt(Command.LOGIN);
        outputStream.writeLong(clientId);
        // 等待命令执行
        log.info("等待server命令");
        ThreadUtil.execute(() -> {
            while (true) {
                try {
                    DataInputStream inputStream = new DataInputStream(commandSocket.getInputStream());
                    int command = inputStream.readInt();
                    log.info(String.format("收到命令%d", command));
                    switch (command) {
                        case Command.BUILD_CHANNEL: // 与server, 内网服务构建隧道
                            long exposePort = inputStream.readLong(); // server上暴露的端口
                            buildChannel(exposePort);
                            break;
                        default:
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void buildChannel(long exposePort) throws IOException {
        // 与server建立连接
        Socket serverSocket = new Socket(serverHost, this.serverPort);
        DataOutputStream outputStream = new DataOutputStream(serverSocket.getOutputStream());
        outputStream.writeInt(Command.BUILD_CHANNEL);
        outputStream.writeLong(exposePort);
        // 与内网服务建立连接
        Long clientPort = exposeRecordMap.get(exposePort).getClientPort();
        log.info(String.format("与server, 内网服务%d构建隧道", clientPort));
        Socket localSocket = new Socket("localhost", Math.toIntExact(clientPort));
        // 建立隧道
        TransmitChannel transmitChannel = new TransmitChannel(serverSocket, localSocket);
    }
}
