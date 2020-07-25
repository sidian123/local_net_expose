package live.sidian.local_net_expose_test;

import cn.hutool.core.thread.ThreadUtil;
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
//                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(finalSocket.getInputStream(), "utf-8"));
//                    String line;
//                    while((line=bufferedReader.readLine())!=null){
//                        System.out.println(line);
//                    }
                    byte[] bytes = new byte[1024];
                    int len = -1;
                    InputStream inputStream = finalSocket.getInputStream();
                    OutputStream outputStream = pythonSocket.getOutputStream();
                    while ((len = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, len);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            ThreadUtil.execute(() -> {
                log.info("发送响应");
                try {
//                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(finalSocket1.getOutputStream(), "utf-8"));
//                    bufferedWriter.write(resposne);
                    byte[] bytes = new byte[1024];
                    int len = -1;
                    InputStream inputStream = pythonSocket.getInputStream();
                    OutputStream outputStream = finalSocket.getOutputStream();
                    while ((len = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, len);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

}
