package app;

import util.FileUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TargetServer {

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(8001);
        Socket socket;
        InputStream inputStream;
        BufferedReader bufferedReader;
        String fileName;
        byte[] fileBytes;

        while (true) {
            socket = serverSocket.accept();
            if ( socket.isConnected() ) {
                // 파일명 얻기
                inputStream = socket.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                fileName = bufferedReader.readLine();
                // 파일명 얻기 끝

                // 파일 쓰기
                fileBytes = FileUtil.readAsByteArray(inputStream);
                FileUtil.byteToFile(fileBytes, fileName);
                // 파일 쓰기 끝
                socket.getInputStream().close();
            }
        }

    }
}
