package live.sidian.local_net_expose_test;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
@SpringBootApplication
public class LocalNetExposeTestApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(LocalNetExposeTestApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8001);
        Socket socket;
        while ((socket = serverSocket.accept()) != null) {
            Socket pythonSocket = new Socket("127.0.0.1", 8002);

            log.info("建立一条连接");
            Socket finalSocket = socket;
            ThreadUtil.execute(() -> {
                try {
                    byte[] bytes = new byte[1024];
                    int len = -1;
                    InputStream inputStream = finalSocket.getInputStream();
                    OutputStream outputStream = pythonSocket.getOutputStream();
                    while ((len = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, len);
                        System.out.println(StrUtil.str(ArrayUtil.sub(bytes, 0, len), "utf-8"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                log.info("o -> p close");
            });
            ThreadUtil.execute(() -> {
                try {
                    byte[] bytes = new byte[1024];
                    int len = -1;
                    InputStream inputStream = pythonSocket.getInputStream();
                    OutputStream outputStream = finalSocket.getOutputStream();
                    while ((len = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, len);
                        System.out.println(StrUtil.str(ArrayUtil.sub(bytes, 0, len), "utf-8"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                log.info("p -> o close");
            });
        }
    }

}
