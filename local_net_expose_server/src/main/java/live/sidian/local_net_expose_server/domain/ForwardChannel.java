package live.sidian.local_net_expose_server.domain;

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
 * 当其中一个socket关闭时, 另一个也被关闭
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
     * 与客户端建立的socket
     */
    @Setter
    Socket clientSocket;
    /**
     * 在暴露端口上建立的socket
     */
    @Setter
    Socket exposeSocket;

    /**
     * exposeSocket input => clientSocket output
     */
    Thread e2cThread;

    /**
     * clientSocket input => exposeSocket output
     */
    Thread c2eThread;

    /**
     * 初始化连接
     */
    public void init() throws IOException {
        // 参数校验
        if ((clientSocket == null || clientSocket.isClosed()) ||
                (exposeSocket == null || exposeSocket.isClosed())) {
            return;
        }
        // 开始初始化
        InputStream clientInputStream = clientSocket.getInputStream();
        OutputStream clientOutputStream = clientSocket.getOutputStream();
        InputStream exposeInputStream = exposeSocket.getInputStream();
        OutputStream exposeOutputStream = exposeSocket.getOutputStream();
        // exposeSocket input => clientSocket output
        e2cThread = new Thread(() -> transfer(exposeInputStream, clientOutputStream));
        // clientSocket input => exposeSocket output
        c2eThread = new Thread(() -> transfer(clientInputStream, exposeOutputStream));
    }

    private void transfer(InputStream inputStream, OutputStream outputStream) {
        try {
            byte[] bytes = new byte[1024];
            int len;
            do {
                // 读
                len = inputStream.read(bytes);
                // 写
                outputStream.write(bytes);
            } while (len != -1);
        } catch (IOException e) {
            log.error("转发异常", e);
            close();
        }
    }

    /**
     * 关闭通道, 即关闭两端socket连接
     */
    public synchronized void close() {
        try {
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            log.error("关闭client socket失败", e);
        }
        try {
            if (exposeSocket != null) exposeSocket.close();
        } catch (IOException e) {
            log.error("关闭expose socket失败", e);
        }
    }
}

























