package live.sidian.local_net_expose_client.application.impl;

import cn.hutool.core.thread.ThreadUtil;
import live.sidian.local_net_expose_client.AppConfig;
import live.sidian.local_net_expose_client.application.ExposeClient;
import live.sidian.local_net_expose_client.domain.ExposeRecord;
import live.sidian.local_net_expose_client.instrastructure.api.ServerApi;
import live.sidian.local_net_expose_common.infrastructure.Command;
import live.sidian.local_net_expose_common.infrastructure.TransmitChannel;
import live.sidian.local_net_expose_common.util.SocketUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
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
    @Resource
    AppConfig appConfig;

    /**
     * 登入server
     */
    @Override
    public void login() {
        ThreadUtil.execute(() -> {
            while (true) {
                try {
                    doLogin();
                } catch (IOException e) {
                    log.error("与server连接出错", e);
                    SocketUtil.close(commandSocket);
                }
                log.info("开始重新登录server");
            }
        });
    }

    private void doLogin() throws IOException {
        // 等待server正常
        waitServerReady();
        // 获取客户端信息
        log.info("获取客户端信息");
        exposeRecordMap = serverApi.getExposeRecord(clientId).stream()
                .collect(Collectors.toMap(ExposeRecord::getServerPort, exposeRecord -> exposeRecord));
        // 连接服务端
        log.info(String.format("连接服务器%s:%d", serverHost, serverPort));
        commandSocket = new Socket(serverHost, serverPort);
        // 发送登录命令
        DataOutputStream outputStream = new DataOutputStream(commandSocket.getOutputStream());
        outputStream.writeInt(Command.LOGIN);
        outputStream.writeLong(clientId);
        // 等待命令执行
        log.info("等待server命令");
        DataInputStream inputStream = new DataInputStream(commandSocket.getInputStream());
        while (true) {
            int command = inputStream.readInt();
            try {
                handleCommand(command, inputStream);
            } catch (SocketException e) {
                log.error("server socket被关闭");
                break;
            } catch (IOException e) {
                log.error("命令执行失败", e);
            }
            log.info("重新读取命令");
        }
    }

    private void handleCommand(int command, DataInputStream inputStream) throws IOException {
        log.info(String.format("收到命令%d", command));
        switch (command) {
            case Command.BUILD_CHANNEL: // 与server, 内网服务构建隧道
                long exposePort = inputStream.readLong(); // server上暴露的端口
                buildChannel(exposePort);
                break;
            default:
                break;
        }
    }

    private void buildChannel(long exposePort) {
        Socket serverSocket = null;
        Socket localSocket = null;
        try {
            // 与server建立连接
            serverSocket = new Socket(serverHost, this.serverPort);
            DataOutputStream outputStream = new DataOutputStream(serverSocket.getOutputStream());
            outputStream.writeInt(Command.BUILD_CHANNEL);
            outputStream.writeLong(exposePort);
            // 与内网服务建立连接
            ExposeRecord exposeRecord = exposeRecordMap.get(exposePort);
            log.info(String.format("在server与内网服务%s:%d之间构建隧道",
                    exposeRecord.getLocalhost(), exposeRecord.getClientPort()));
            localSocket = new Socket(exposeRecord.getLocalhost(), Math.toIntExact(exposeRecord.getClientPort()));
            // 建立隧道
            TransmitChannel transmitChannel = new TransmitChannel(serverSocket, localSocket);
            transmitChannel.setShowContent(appConfig.isShowContent());
        } catch (IOException e) {
            log.error("隧道构建失败", e);
            SocketUtil.close(serverSocket);
            SocketUtil.close(localSocket);
        }
    }

    private void waitServerReady() {
        String status = null;
        do {
            try {
                status = serverApi.getServerStatus();
            } catch (Exception e) {
                log.info("等待Server正常运行, e:" + e.getMessage());
            }
        } while (!Objects.equals(status, "ready"));
    }
}
