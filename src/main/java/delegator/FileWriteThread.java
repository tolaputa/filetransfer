package delegator;

import util.FileUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class FileWriteThread implements Runnable {

    private Socket socket;

    public FileWriteThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            inputStream = socket.getInputStream();
            FileUtil.byteToIfile(FileUtil.readAsByteArray(inputStream), "/Users/dino/Desktop/img_02.jpg");
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileUtil.byteToIfile(FileUtil.readAsByteArray(inputStream), "/Users/dino/Desktop/img_02.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
