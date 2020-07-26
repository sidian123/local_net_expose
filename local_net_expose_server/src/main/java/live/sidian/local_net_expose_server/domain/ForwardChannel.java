package live.sidian.local_net_expose_server.domain;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;

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
        log.info("开始建立通道");
        status = "ok";
        // exposeSocket input => clientSocket output
        exposeToClient(exposeSocket.getInputStream(), clientSocket.getOutputStream());
        // clientSocket input => exposeSocket output
        clientToExpose(clientSocket.getInputStream(), exposeSocket.getOutputStream());
    }

    /**
     * clientSocket input => exposeSocket output
     * 一般server与client的连接不会轻易断开
     *
     * @param inputStream  暴露端口的输出流
     * @param outputStream 客户端的输入流
     */
    private void clientToExpose(InputStream inputStream, OutputStream outputStream) {
        ThreadUtil.execute(() -> {
            try {
                transfer(inputStream, outputStream);
                log.info("client socket 正常关闭");
            } catch (SocketException e) {
                if (e.getMessage().equals("Socket closed")) {
                    log.info("client socket 正常关闭. e:" + e.getMessage());
                } else {
                    log.error("client socket 异常", e);
                }
            } catch (IOException e) {
                log.error("转发异常", e);
                status = "error";
            } finally {
                // clientSocket关闭流时, 说明clientSocket正常结束了,那么整个通道不可用了
                close();
            }
        });
    }

    /**
     * exposeSocket input => clientSocket output
     *
     * @param inputStream  暴露端口的输入流
     * @param outputStream 客户端的输出流
     */
    private void exposeToClient(InputStream inputStream, OutputStream outputStream) {
        ThreadUtil.execute(() -> {
            try {
                transfer(inputStream, outputStream);
                closeExposeSocket();
                log.info("expose socket 关闭");
            } catch (SocketException e) {
                log.info("expose socket 关闭");
            } catch (IOException e) {
                log.error("转发异常", e);
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

    @Override
    public String toString() {
        LinkedList<String> strings = new LinkedList<>();
        if (clientSocket != null) {
            strings.add("client socket closed? " + clientSocket.isClosed());
        }
        if (exposeSocket != null) {
            strings.add("expose socket closed? " + exposeSocket.isClosed());
        }
        return String.join("\n", strings);
    }
}

























