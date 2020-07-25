package live.sidian.local_net_expose_client;

import cn.hutool.core.thread.ThreadUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 转发通道, 两个socket组成一个数据流通道.
 *
 * @author sidian
 * @date 2020/7/25 11:58
 */
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForwardChannel {
    /**
     * 局域网内要被转发的端口
     */
    @Setter
    Long localPort;

    /**
     * 与服务器建立的socket
     */
    @Setter
    Socket serverSocket;

    volatile String status;

    /**
     * 初始化连接
     * TODO 需要改, 局域网内socket关闭了, 不应该结束所有socket, serverSocket可复用. 但serverSocket若关闭了, 那都要关闭.
     */
    public void init() throws IOException {
        if (serverSocket == null || serverSocket.isClosed() || localPort == null) {
            return;
        }
        status = "ok";
        // 与内网端口建立连接
        Socket localSocket = new Socket("127.0.0.1", Math.toIntExact(localPort));
        // 获取流
        InputStream localInputStream = localSocket.getInputStream();
        OutputStream localOutputStream = localSocket.getOutputStream();
        InputStream serverInputStream = serverSocket.getInputStream();
        OutputStream serverOutputStream = serverSocket.getOutputStream();
        // serverSocket input => localSocket output
        ThreadUtil.execute(() -> {
            try {
                transfer(serverInputStream, localOutputStream);
                closeLocalSocket(localSocket);
            } catch (IOException e) {
                log.error("转发失败", e);
                status = "error";
                close();
            }
        });
        // localSocket input => serverSocket output
        ThreadUtil.execute(() -> {
            try {
                transfer(localInputStream, serverOutputStream);
                closeLocalSocket(localSocket);
            } catch (IOException e) {
                log.error("转发失败", e);
                status = "error";
                close();
            }
        });
    }

    private void transfer(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] bytes = new byte[1024];
        int len;
        do {
            // 读
            len = inputStream.read(bytes);
            // 写
            outputStream.write(bytes);
        } while (len != -1);
    }

    public synchronized void closeLocalSocket(Socket localSocket) {
        try {
            localSocket.close();
        } catch (IOException e) {
            log.error("关闭local socket失败", e);
        } finally {
            localPort = null;
        }
    }

    public synchronized void close() {
        localPort = null;
        try {
            serverSocket.close();
        } catch (IOException e) {
            log.error("关闭server socket失败", e);
        }
    }
}

























