package app;

import delegator.SourceThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SourceServer {

    private static ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8000);
        while (true) {
            threadPool.execute(SourceThread.getInstance(serverSocket.accept()));
        }
    }
}
