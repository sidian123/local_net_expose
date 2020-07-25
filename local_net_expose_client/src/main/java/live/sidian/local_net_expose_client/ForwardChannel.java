package live.sidian.local_net_expose_client;

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
     * 局域网内要被转发的端口
     */
    Long localPort;

    /**
     * 与服务器建立的socket
     */
    Socket serverSocket;

    public ForwardChannel() {
    }

    public ForwardChannel(Socket serverSocket) throws IOException {
        this.serverSocket = serverSocket;
        init();
    }

    public void setLocalPort(Long localPort) throws IOException {
        this.localPort = localPort;
        init();
    }

    public void setServerSocket(Socket serverSocket) throws IOException {
        this.serverSocket = serverSocket;
        init();
    }

    volatile String status;

    /**
     * 初始化连接
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
                log.info("local socket 正常关闭");
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
                log.info("local socket 正常关闭");
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

























