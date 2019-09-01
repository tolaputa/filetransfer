package app;

import util.FileUtil;

import java.io.*;
import java.net.Socket;

public class BaseClient {

    public static void main(String[] args) throws IOException {

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


        Socket targetServerSocket = new Socket("localhost", 8001);
        OutputStream targetServerSocketOutputStream = targetServerSocket.getOutputStream();
        /*PrintWriter targetServerWriter = new PrintWriter(new OutputStreamWriter(targetServerSocket.getOutputStream()));
        targetServerWriter.println("/Users/dino/Desktop/img_10.jpg");
        targetServerWriter.flush();*/

        targetServerSocketOutputStream.write(fileBytes);
        targetServerSocketOutputStream.flush();
        targetServerSocket.close();

        System.exit(0);
    }
}
