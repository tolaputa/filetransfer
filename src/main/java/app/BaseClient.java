package app;

import delegator.FileWriteThread;
import util.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class BaseClient {



    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 8000);

        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        printWriter.println("/Users/dino/Picture/img_02.jpg");
        printWriter.flush();

//        FileWriteThread fileWriter = new FileWriteThread(socket);
//        fileWriter.run();


        InputStream inputStream = socket.getInputStream();
        FileUtil.byteToIfile(FileUtil.readAsByteArray(inputStream), "/Users/dino/Desktop/img_02.jpg");

        socket.close();





//        socket.close();

//        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//        System.out.println(bufferedReader.readLine());



//        InputThread inputThread = new InputThread(socket, bufferedReader);


        /*OutputStream outputStream = socket.getOutputStream();
        outputStream.write("/Users/dino/Picture/img_02.jpg".getBytes());
        outputStream.flush();
        InputStream inputStream = socket.getInputStream();
        FileUtil.byteToIfile(FileUtil.readAsByteArray(inputStream), "/Users/dino/Desktop/img_02.jpg");
        socket.close();
        System.exit(0);*/
    }
}
