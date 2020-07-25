package live.sidian.local_net_expose_server.domain;

import cn.hutool.core.thread.ThreadUtil;
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
public class ForwardChannel {
    /**
     * 与客户端建立的socket
     */
    Socket clientSocket;
    /**
     * 在暴露端口上建立的socket
     */
    Socket exposeSocket;

    volatile String status;

    public ForwardChannel() {
    }

    public ForwardChannel(Socket clientSocket, Socket exposeSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.exposeSocket = exposeSocket;
        init();
    }

    public void setClientSocket(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        init();
    }

    public void setExposeSocket(Socket exposeSocket) throws IOException {
        this.exposeSocket = exposeSocket;
        init();
    }

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
        log.info("开始传输数据");
        status = "ok";
        InputStream clientInputStream = clientSocket.getInputStream();
        OutputStream clientOutputStream = clientSocket.getOutputStream();
        InputStream exposeInputStream = exposeSocket.getInputStream();
        OutputStream exposeOutputStream = exposeSocket.getOutputStream();
        // exposeSocket input => clientSocket output
        ThreadUtil.execute(() -> {
            try {
                transfer(exposeInputStream, clientOutputStream);
                closeExposeSocket();
                log.info("expose socket 关闭");
            } catch (IOException e) {
                log.error("转发异常", e);
                status = "error";
                close();
            }
        });
        // clientSocket input => exposeSocket output
        ThreadUtil.execute(() -> {
            try {
                transfer(clientInputStream, exposeOutputStream);
                log.info("client socket 关闭");
            } catch (IOException e) {
                log.error("转发异常", e);
                status = "error";
            } finally {
                // clientSocket关闭流时, 说明clientSocket正常结束了,那么整个通道不可用了
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

    /**
     * 关闭通道, 仅关闭exposeSocket
     */
    public synchronized void closeExposeSocket() {
        try {
            if (exposeSocket != null) exposeSocket.close();
        } catch (IOException e) {
            log.error("关闭expose socket失败", e);
        }
    }

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

























