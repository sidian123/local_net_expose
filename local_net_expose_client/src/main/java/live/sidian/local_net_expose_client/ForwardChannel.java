package live.sidian.local_net_expose_client;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import live.sidian.local_net_expose_common.infrastructure.ChannelInputStream;
import live.sidian.local_net_expose_common.infrastructure.ChannelOutputStream;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;

import static live.sidian.local_net_expose_common.infrastructure.EncoderConstant.OpConstant.CLOSE;
import static live.sidian.local_net_expose_common.infrastructure.EncoderConstant.OpConstant.OPEN;

/**
 * 转发通道, 两个socket组成一个数据流通道.
 *
 * @author sidian
 * @date 2020/7/25 11:58
 */
@Slf4j
@Deprecated
public class ForwardChannel {
    /**
     * 局域网内要被转发的端口
     */
    volatile Long localPort;

    /**
     * 与服务器建立的socket
     */
    volatile Socket serverSocket;

    /**
     * 与内网端口建立的连接. 无setter方法
     */
    volatile Socket localSocket;

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
     */
    public void init() {
        if (serverSocket == null || serverSocket.isClosed() || localPort == null) {
            return;
        }
        status = "ok";
        ThreadUtil.execute(() -> {
            try {
                // 等待server命令并执行
                ChannelInputStream serverInputStream = new ChannelInputStream(serverSocket.getInputStream());
                while (true) {
                    waitServerCommand(serverInputStream);
                }
            } catch (IOException e) {
                log.info("传输失败", e);
            }
        });
    }


    private void waitServerCommand(ChannelInputStream serverInputStream) throws IOException {
        // 打印当前状态
        log.info("当前状态:" + toString());
        // 读取server命令
        int op = serverInputStream.readOp();
        log.info("接收到Server命令:" + op);
        // 执行
        switch (op) {
            case OPEN:
                // 与内网端口建立传输通道
                log.info("与内网端口建立传输通道");
                establishChannel(serverInputStream);
                break;
            case CLOSE:
                // 关闭与内网端口的连接
                log.info("关闭与内网端口的连接");
                closeLocalSocket();
                break;
            default:
                log.error("不支持该操作:" + op);
        }
    }

    /**
     * 与内网端口建立传输通道
     *
     * @param serverInputStream server socket的输入流
     */
    private void establishChannel(ChannelInputStream serverInputStream) throws IOException {
        // 与内网端口建立连接
        localSocket = new Socket("127.0.0.1", Math.toIntExact(localPort));
        // localSocket input => serverSocket output
        localToServer(localSocket.getInputStream(), new ChannelOutputStream(serverSocket.getOutputStream()));
        // serverSocket input => localSocket output
        serverToLocal(serverInputStream, localSocket.getOutputStream());
    }

    /**
     * localSocket input => serverSocket output
     */
    private void localToServer(InputStream inputStream, ChannelOutputStream outputStream) {
        ThreadUtil.execute(() -> {
            try {
                byte[] bytes = new byte[1024];
                int len = -1;
                while ((len = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, len);
                }
                closeLocalSocket();
                log.info("告之server, local socket关闭了");
                outputStream.writeOp(CLOSE);
            } catch (SocketException e) {
                e.printStackTrace();
                closeLocalSocket();
                log.info("local socket 正常关闭, e:" + e.getMessage());
            } catch (IOException e) {
                log.error("转发失败", e);
                status = "error";
                close();
            }
        });
    }

    /**
     * serverSocket input => localSocket output
     */
    private void serverToLocal(ChannelInputStream inputStream, OutputStream outputStream) {
        try {
            byte[] bytes = new byte[1024];
            int len;
            while (!ArrayUtil.contains(new int[]{-1, -2}, len = inputStream.read(bytes))) {
                outputStream.write(bytes, 0, len);
            }
            if (len == -2) {
                log.info("server socket发送了命令");
            } else if (len == -1) {
                log.info("serer 关闭了连接");
                close();
            }
        } catch (IOException e) {
            log.error("转发失败", e);
            log.info("local socket closed? " + localSocket.isClosed());
            status = "error";
            close();
        }

    }


    @Override
    public String toString() {
        LinkedList<String> strings = new LinkedList<>();
        strings.offer("局域网内连接的端口:" + localPort);
        if (serverSocket != null) {
            strings.offer("server socket closed? " + serverSocket.isClosed());
        }
        if (localSocket != null) {
            strings.offer("local socket closed? " + localSocket.isClosed());
        }
        return String.join("\t", strings);
    }

    public synchronized void closeLocalSocket() {
        try {
            localSocket.close();
        } catch (IOException e) {
            log.error("关闭local socket失败", e);
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

























