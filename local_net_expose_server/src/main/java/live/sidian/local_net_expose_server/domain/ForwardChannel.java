package live.sidian.local_net_expose_server.domain;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
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
 * TODO expose socket关闭了, 但client端的线程还在跑
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
        clientToExpose(new ChannelInputStream(clientSocket.getInputStream()), exposeSocket.getOutputStream());
    }

    /**
     * clientSocket input => exposeSocket output
     *
     * @param inputStream  暴露端口的输出流
     * @param outputStream 客户端的输入流
     */
    private void clientToExpose(InputStream inputStream, OutputStream outputStream) {
        ThreadUtil.execute(() -> {
            try {
                byte[] bytes = new byte[1024];
                int len;
                while (!ArrayUtil.contains(new int[]{-1, -2}, len = inputStream.read(bytes))) {
                    outputStream.write(bytes, 0, len);
                    System.out.println(StrUtil.str(ArrayUtil.sub(bytes, 0, len), "utf-8"));
                }
                if (len == -2) {
                    log.info("client请求关闭expose连接");
                    closeExposeSocket();
                }
                if (len == -1) {
                    log.info("client socket 正常关闭");
                    close();
                }
            } catch (SocketException e) {
                e.printStackTrace();
                try {
                    outputStream.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            } catch (IOException e) {
                log.error("转发异常", e);
                status = "error";
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
            ChannelOutputStream channelOutputStream = new ChannelOutputStream(outputStream);
            try {
                // 要求client建立内网连接
                channelOutputStream.writeOp(OPEN);
                // 开始传输数据
                transfer(inputStream, channelOutputStream);
                // 要求client关闭内网连接
                channelOutputStream.writeOp(CLOSE);
                closeExposeSocket();
                log.info("expose socket 关闭");
            } catch (SocketException e) {
                e.printStackTrace();
                log.info("expose socket 关闭, e:" + e.getMessage());
                try {
                    channelOutputStream.writeOp(CLOSE);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                closeExposeSocket();
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
            System.out.println(StrUtil.str(bytes, "utf-8"));
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

























