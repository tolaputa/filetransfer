package app;

import util.FileUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SourceServer {

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(8000);
        Socket socket;
        OutputStream outputStream;
        BufferedReader bufferedReader;
        String fileName;

        while (true) {
            socket = serverSocket.accept();
            if ( socket.isConnected() ) {
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                fileName = bufferedReader.readLine();
                outputStream = socket.getOutputStream();
                outputStream.write(FileUtil.fileToByte(new File(fileName)));
                socket.getInputStream().close();
                outputStream.flush();
                outputStream.close();
            }
        }

    }
}
