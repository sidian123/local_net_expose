package live.sidian.local_net_expose_test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
            TransmitChannel transmitChannel = new TransmitChannel(socket, pythonSocket);
        }
    }

}
