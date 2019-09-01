package app;

import util.FileUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TargetServer {

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(8001);
        Socket socket;
        OutputStream outputStream;
        BufferedReader bufferedReader;
        String fileName;
        byte[] fileBytes;

        while (true) {
            socket = serverSocket.accept();
            if ( socket.isConnected() ) {
//                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                fileName = bufferedReader.readLine();
                fileBytes = FileUtil.readAsByteArray(socket.getInputStream());
                FileUtil.byteToFile(fileBytes, "/Users/dino/Desktop/img_11.jpg");
//                bufferedReader.close();
                socket.getInputStream().close();
            }
        }

    }
}
