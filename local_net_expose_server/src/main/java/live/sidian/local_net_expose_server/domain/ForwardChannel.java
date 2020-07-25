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
 * 转发通道
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
        if (clientSocket == null || exposeSocket == null) {
            return;
        }
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

    public synchronized void close() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            log.error("关闭client socket失败", e);
        }
        try {
            exposeSocket.close();
        } catch (IOException e) {
            log.error("关闭expose socket失败", e);
        }
    }
}

























