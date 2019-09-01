package vo;

import java.io.Serializable;
import java.net.Socket;

public class SocketVO implements Serializable {

    private static final long serialVersionUID = 7231930132162027715L;

    private Socket socket;

    private String fileName;

    private SocketVO(Socket socket, String fileName) {
        this.socket = socket;
        this.fileName = fileName;
    }

    public static SocketVO getInstance(Socket socket, String fileName) {
        return new SocketVO(socket, fileName);
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
