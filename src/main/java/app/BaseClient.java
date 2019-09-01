package app;

import util.FileUtil;

import java.io.*;
import java.net.Socket;

public class BaseClient {

    public static void main(String[] args) throws IOException, InterruptedException {

        // 파일 갖고 오기 : Source 서버에 파일명 전송하여 해당파일의 byte를 가져옴.
        Socket sourceServerSocket = new Socket("localhost", 8000);
        OutputStream sourceOutputStream = sourceServerSocket.getOutputStream();
        PrintWriter sourcePrintWriter = new PrintWriter(new OutputStreamWriter(sourceOutputStream));
        sourcePrintWriter.println("/Users/dino/Pictures/img_02.jpg");
        sourcePrintWriter.flush();


        InputStream inputStream = sourceServerSocket.getInputStream();
        byte [] fileBytes = FileUtil.readAsByteArray(inputStream);


        sourceOutputStream.close();
        inputStream.close();
        sourceServerSocket.close();
        // 파일 갖고 오기 끝

//        byte [] fileBytes = FileUtil.readAsByteArray(new File("/Users/dino/Pictures/img_02.jpg"));

        // 파일 전송하기 : 위 갖고 온 파일의 byte를 Target Server 에 전송(파일명 전송은 안됨)
        Socket targetServerSocket = new Socket("localhost", 8001);
        OutputStream targetServerSocketOutputStream = targetServerSocket.getOutputStream();
        PrintWriter targetServerWriter = new PrintWriter(new OutputStreamWriter(targetServerSocket.getOutputStream()));
        targetServerWriter.println("/Users/dino/Desktop/img_27.jpg");
        targetServerWriter.flush();
        Thread.sleep(1);
        targetServerSocketOutputStream.flush();
        Thread.sleep(1);
        targetServerSocketOutputStream.write(fileBytes);
        targetServerSocket.close();
        // 파일 전송하기 끝

        System.exit(0);
    }
}
