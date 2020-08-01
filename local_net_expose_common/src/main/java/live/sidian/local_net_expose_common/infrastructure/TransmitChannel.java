package live.sidian.local_net_expose_common.infrastructure;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import live.sidian.local_net_expose_common.util.SocketUtil;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 将两个socket连接起来的传输通道.
 * 举例, A连接B, C连接D, 当B和C建立了传输通道后, 相当于A连接到D. B和C关闭, 仅当A和D都发送了EOF, 或者生成异常
 *
 * @author sidian
 * @date 2020/7/28 23:52
 */
@Slf4j
public class TransmitChannel {
    Socket socket;
    Socket socket2;

    @Setter
    boolean showContent = false;

    /**
     * 由于EOF或异常退出的流向(一个通道, 两个方向的流)
     */
    AtomicLong exitNum = new AtomicLong(0);

    public TransmitChannel(@NonNull Socket socket, @NonNull Socket socket2) throws IOException {
        // 参数校验
        if (socket.isClosed() || socket2.isClosed()) {
            throw new IllegalArgumentException("不能传入被关闭的socket");
        }
        // 初始化属性
        this.socket = socket;
        this.socket2 = socket2;
        // 初始化通道
        initChannel();
    }

    private void initChannel() throws IOException {
        doInitChannel(socket.getInputStream(), socket2.getOutputStream(), socket2);
        doInitChannel(socket2.getInputStream(), socket.getOutputStream(), socket);
    }

    private void doInitChannel(InputStream inputStream, OutputStream outputStream, Socket socket) {
        ThreadUtil.execute(() -> {
            try {
                byte[] bytes = new byte[1024];
                int len;
                while ((len = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, len);
                    if (showContent) {
                        System.out.println(StrUtil.str(ArrayUtil.sub(bytes, 0, len), "utf-8"));
                    }
                }
                socket.shutdownOutput(); // EOF读不出来, 需要手动写入
                exitNum.incrementAndGet();
                canAndClose();
            } catch (IOException e) {
                log.error("通道中数据传输失败", e);
                close();
            }
        });
    }

    /**
     * 若可以关闭两个socket, 则关闭.
     * 用于正常关闭, 仅当两端都返回EOF时关闭.
     */
    private void canAndClose() {
        if (exitNum.get() == 2) {
            close();
        }
    }

    public void close() {
//        log.info("一条传输隧道被关闭");
        SocketUtil.close(socket);
        SocketUtil.close(socket2);
    }
}
