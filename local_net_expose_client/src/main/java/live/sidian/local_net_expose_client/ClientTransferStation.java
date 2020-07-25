package live.sidian.local_net_expose_client;

import cn.hutool.core.thread.ThreadUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 中转站
 *
 * @author sidian
 * @date 2020/7/25 17:13
 */
@Slf4j
@Service
public class ClientTransferStation {
    /**
     * socket连接通道的集合. 穿透记录与通道的映射
     */
    Map<Long, ForwardChannel> channelMap = new HashMap<>();

    @Value("${expose.server.url-prefix}")
    String urlPrefix;

    @Value("${expose.client.id}")
    long clientId;

    @Value("${expose.server.host}")
    String host;

    @Value("${expose.server.port}")
    long port;

    @Resource
    RestTemplate restTemplate;
    @Resource
    ObjectMapper objectMapper;

    public void init() {
        // 获取所有穿透记录
        String res = restTemplate.getForObject(urlPrefix + "/expose_record/get?clientId=" + clientId, String.class);
        List<ExposeRecord> exposeRecordList = null;
        try {
            exposeRecordList = objectMapper.readValue(res, new TypeReference<List<ExposeRecord>>() {
            });
        } catch (IOException e) {
            log.error("不可能出现的错误...", e);
        }
        assert exposeRecordList != null;
        // 与服务器建立连接
        log.info("与服务器建立连接");
        for (ExposeRecord exposeRecord : exposeRecordList) {
            try {
                establish(exposeRecord);
            } catch (IOException e) {
                log.error("连接服务器失败", e);
            }
        }
    }

    /**
     * 与server建立连接
     *
     * @param exposeRecord 穿透记录
     */
    private void establish(ExposeRecord exposeRecord) throws IOException {
        // 与服务器建立连接
        Socket socket = new Socket(host, (int) port);
        // 发送连接信息
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.writeLong(clientId);
        dataOutputStream.writeLong(exposeRecord.getClientPort());
        // 记录该连接
        ForwardChannel channel;
        if ((channel = channelMap.get(exposeRecord.getId())) != null) {
            channel.setServerSocket(socket);
        } else {
            channel = new ForwardChannel(socket);
            channelMap.put(exposeRecord.getId(), channel);
        }
        // 等待Server传输数据
        ForwardChannel finalChannel = channel;
        ThreadUtil.execute(() -> {
            try {
                waitForSererData(socket, finalChannel, exposeRecord.getClientPort());
            } catch (IOException e) {
                log.error("数据传输失败", e);
                try {
                    socket.close();
                } catch (IOException ioException) {
                    log.error("server socket 关闭失败");
                }
            }
        });
    }

    private void waitForSererData(Socket socket, ForwardChannel channel, Long clientPort) throws IOException {
        // 检测是否存在数据
        PushbackInputStream inputStream = new PushbackInputStream(socket.getInputStream());
        int b = -1;
        while ((b = inputStream.read()) != -1) { // 有数据
            inputStream.unread(b);
            // 建立通道
            log.info("服务端传输数据");
            channel.setLocalPort(clientPort);
        }
    }

}
