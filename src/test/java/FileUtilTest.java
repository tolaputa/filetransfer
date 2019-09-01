import util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class FileUtilTest {

    private static byte[] bytes;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        // 파일명을 변경해서 테스트 하세요.
        String fileName = args.length > 0 ? args[0] : "/Users/dino/Documents/Utilities/IDE/IntelliJ/ForMac/ideaIU-2019.1.dmg";
        readFileAsByteArrayTest(fileName);
        makeChecksumTest();
    }

    /**
     * 파일을 바이트로 읽는 테스트
     * @param fileName
     * @throws IOException
     */
    private static void readFileAsByteArrayTest(String fileName) throws IOException {
        bytes = FileUtil.readAsByteArray(new File(fileName));
        System.out.printf("File size is %d byte, %f MB\n", bytes.length, (double)bytes.length/(1024d*1024d));
    }

    /**
     * Checksum test
     */
    private static void makeChecksumTest() throws IOException, NoSuchAlgorithmException {
        String checksum = FileUtil.makeChecksum(bytes);
        System.out.printf("Checksum is %s\n", checksum);
    }
}
