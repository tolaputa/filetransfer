package util;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtil {

    private static final String ALGORITHM = "SHA-256";

    /**
     * 파일을 읽어 바이트배열로 전환
     *
     * @param file
     * @return
     */
    public static byte[] fileToByte(File file){
        try {
            return FileUtil.readAsByteArray(file);
        } catch (IOException e) {
            return null;
        }
    }

    public static byte[] readAsByteArray(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        byte[] ret = FileUtil.readAsByteArray(in);
        in.close();
        return ret;
    }

    public static byte[] readAsByteArray(InputStream inStream) throws IOException {
        int size = 1024;
        byte[] ba = new byte[size];
        int readSoFar = 0;

        while (true) {
            int nRead = inStream.read(ba, readSoFar, size - readSoFar);
            if (nRead == -1)
                break;
            readSoFar += nRead;
            if (readSoFar == size) {
                int newSize = size * 2;
                byte[] newBa = new byte[newSize];
                System.arraycopy(ba, 0, newBa, 0, size);
                ba = newBa;
                size = newSize;
            }
        }

        byte[] newBa = new byte[readSoFar];
        System.arraycopy(ba, 0, newBa, 0, readSoFar);
        return newBa;
    }

    /**
     * 바이트배열을 읽어 파일로 변환
     *
     * @param bytes
     * @param fileName
     * @return
     */
    public static File byteToFile(byte[] bytes, String fileName){
        File file = new File(fileName);
        try {
            OutputStream output = new FileOutputStream(file);
            BufferedOutputStream bof = new BufferedOutputStream(output);
            bof.write(bytes);
            bof.close();
            output.close();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
        return file;
    }

    /**
     * File을 직접 받아 생성
     * @param fileName
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static String makeChecksum(String fileName) throws NoSuchAlgorithmException, IOException {
        return generateChecksum(readAsByteArray(new File(fileName)));
    }

    /**
     * Byte를 받아 생성
     * @param bytes
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static String makeChecksum(byte[] bytes) throws NoSuchAlgorithmException, IOException {
        return generateChecksum(bytes);
    }

    /**
     * Generate checksum
     * @param bytes
     * @return
     * @throws NoSuchAlgorithmException
     */
    private static String generateChecksum(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(ALGORITHM);
        md.update(bytes);
        bytes = md.digest();

        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

}
