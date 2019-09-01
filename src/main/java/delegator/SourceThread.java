package delegator;

import util.FileUtil;

import java.io.*;
import java.net.Socket;

public class SourceThread implements Runnable {

    private Socket socket;

    private PrintWriter printWriter;

    private BufferedReader bufferedReader;

    private String fileName;

    private OutputStream os;

    private SourceThread(Socket Socket) {
        this.socket = Socket;
    }

    public static SourceThread getInstance(Socket socketVO) {
        return new SourceThread(socketVO);
    }

    @Override
    public void run() {

        try {
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            OutputStream os = socket.getOutputStream();
            os.write(FileUtil.fileToByte(new File(bufferedReader.readLine())));
        } catch (Exception e) {
            try {
                bufferedReader.close();
            } catch (IOException ee) {
                e.printStackTrace();
            }
            printWriter.close();
            try {
                socket.close();
            } catch (IOException ee) {
                e.printStackTrace();
            }
        } finally {

        }

        /*try {
            InputStreamReader reader = new InputStreamReader(socket.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(reader);
            System.out.printf("Read line is %s\n", bufferedReader.readLine());
            String fileName = bufferedReader.readLine();
            OutputStream os = socket.getOutputStream();
            os.write(FileUtil.fileToByte(new File(fileName)));
            System.out.println("Sent file.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
