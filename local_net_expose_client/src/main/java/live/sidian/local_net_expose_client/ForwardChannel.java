package live.sidian.local_net_expose_client;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.net.SocketException;

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

    public ForwardChannel(Socket serverSocket, Long localPort) {
        this.serverSocket = serverSocket;
        this.localPort = localPort;
        init();
    }

    public void setLocalPort(Long localPort) {
        this.localPort = localPort;
        init();
    }

    public void setServerSocket(Socket serverSocket) {
        this.serverSocket = serverSocket;
        init();
    }

    volatile String status;

    /**
     * 初始化连接
     * TODO bug server输入流在两个线程中被读取了
     */
    public void init() {
        if (serverSocket == null || serverSocket.isClosed() || localPort == null) {
            return;
        }
        status = "ok";
        ThreadUtil.execute(() -> {
            try {
                // 等待服务端发起请求
                PushbackInputStream serverInputStream = new PushbackInputStream(serverSocket.getInputStream());
                int b = -1;
                while ((b = serverInputStream.read()) != -1) { // 有数据
                    serverInputStream.unread(b);
                    // 准备建立通道
                    _init(serverInputStream);
                }
            } catch (IOException e) {
                log.info("传输失败", e);
            }
        });
    }

    private void _init(PushbackInputStream serverInputStream) throws IOException {
        // 与内网端口建立连接
        Socket localSocket = new Socket("127.0.0.1", Math.toIntExact(localPort));
        log.info(String.format("与内网端口建立连接, local socket hashcode:%d closed? %s", localSocket.hashCode(), localSocket.isClosed()));
        // 获取流
        InputStream localInputStream = localSocket.getInputStream();
        OutputStream localOutputStream = localSocket.getOutputStream();
        OutputStream serverOutputStream = serverSocket.getOutputStream();
        // serverSocket input => localSocket output
        ThreadUtil.execute(() -> {
            try {
                transfer(serverInputStream, localOutputStream);
                log.info("server socket 正常关闭");
            } catch (IOException e) {
                log.error("转发失败", e);
                log.info("local socket closed? " + localSocket.isClosed());
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
            } catch (SocketException e) {
                e.printStackTrace();
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
        int len = -1;
        while ((len = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, len);
        }
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

























