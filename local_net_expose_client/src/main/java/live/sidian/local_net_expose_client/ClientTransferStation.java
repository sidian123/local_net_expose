package live.sidian.local_net_expose_client;

import cn.hutool.core.map.MapUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.DataOutputStream;
import java.io.IOException;
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

    @PostConstruct
    public void init() {
        // 获取所有穿透记录
        List<ExposeRecord> exposeRecordList = restTemplate.exchange(
                urlPrefix + "/expose_record/get",
                HttpMethod.GET,
                new HttpEntity<>(MapUtil.builder("clientId", clientId).build()),
                new ParameterizedTypeReference<List<ExposeRecord>>() {
                }
        ).getBody();
        assert exposeRecordList != null;
        // 与服务器建立连接
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
            channel = ForwardChannel.builder().serverSocket(socket).build();
            channelMap.put(exposeRecord.getId(), channel);
        }
    }

}
