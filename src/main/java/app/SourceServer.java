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
                // 파일명 얻기 : 파일명은 전체 경로
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                fileName = bufferedReader.readLine();
                // 파일명 얻기 끝

                // 파일 전송 : 위 파일명의 파일을 전송함.
                outputStream = socket.getOutputStream();
                outputStream.write(FileUtil.fileToByte(new File(fileName)));
                socket.getInputStream().close();
                outputStream.flush();
                outputStream.close();
                // 파일 전송 끝
            }
        }

    }
}
