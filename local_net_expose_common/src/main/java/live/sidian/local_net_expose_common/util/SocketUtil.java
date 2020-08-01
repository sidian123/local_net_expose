package live.sidian.local_net_expose_common.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author sidian
 * @date 2020/8/1 16:36
 */
@Slf4j
public class SocketUtil {
    public static void close(Socket socket) {
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (IOException e) {
            log.warn("关闭socket失败", e);
        }
    }

    public static void close(ServerSocket serverSocket) {
        if (serverSocket == null) {
            return;
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            log.warn("关闭server socket失败");
        }
    }
}
