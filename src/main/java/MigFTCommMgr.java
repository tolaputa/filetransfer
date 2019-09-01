/***********************************************************************************************
* Program Description : File Transfer를 위해 S/T 각 node에 설치되는 CommManager
*                              제공 받은 FileName으로  
*							  - Source에서 읽어 전달하거나
*							  - 전달받은 내용을 Target에서 write
*							  - 각 상황을 기록
* Created DateTime : 2019/08/14
***********************************************************************************************/

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;

public class MigFTCommMgr {

	private static String StartDateTime, FinishDateTime, LogString, Origin; 
	private static String LogFileName, OptionFileName, FileName, InputLine, FTMsg; 
	private static String ConsoleDispLevel, FileLogLevel;  
	private static String AcceptValue = "", InvalidParameters="", SysDelimiter = ";"; 

	private static char EOD = (char)26; // EOD means EOF
	private static String CR="\\r", LF="\\n", CRLF="\\r\\n";
//	private static InetAddress SourceIP, BaseIP = null;
	private static int SocketPort = 0;
	private static String SourceIP, BaseIP, TargetIP;
	private static Socket SourceSocket, BaseSocket, TargetSocket;
	private static ServerSocket SourceServerSocket, BaseServerSocket, TargetServerSocket;
	private static BufferedReader BaseBufferedReader = null;
	private static BufferedReader FTListFile = null;  
	private static BufferedReader OptionFile = null;  
	private static BufferedWriter OutputFile = null;  
	private static BufferedWriter LogFile = null;  
	private static PrintWriter BasePrintWriter  = null;


	public static void main(String[] arg) throws Exception {
		for(int i=0; i<arg.length; i++){ 
			AcceptValue = AcceptValue + (arg[i]); 
		} 
		String[] temp = AcceptValue.split(SysDelimiter); 
//		for(int i=0; i<temp.length; i++){			// you may use this function for checking the accept values 
//			System.out.println(i + " " + temp[i]); 
//		} 
		for(int i=0; i<temp.length; i++) {
			if (temp[i].toLowerCase().indexOf("optionfile") > -1)	{
				OptionFileName	 = temp[i].substring(10);
			} else {
				InvalidParameters = InvalidParameters + temp[i]; 
			}
		}
//		System.out.println("FTListName = " + FTListName);
		if (!"".equals(InvalidParameters)) { 
			System.out.println("\n Invalid accept parameter....  " + InvalidParameters + "\n");   
			closeFiles();   
			System.exit(0);   
		}

		getEnvOptions();
		standBy();
		System.exit(0); 
	}

	public static void standBy() throws Exception{
		Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
		System.out.println("\n\n   STANDBY for FileTranfer...  " + timeStamp.toString().substring(0, 19).replace("-", "/"));

		String msg = "";
		BaseServerSocket = new ServerSocket(SocketPort);

		// Base의 접속을 기다린다.
		SourceSocket = BaseServerSocket.accept(); // Base에서 message를 보낼 때 까지 대기한다.
		// Base의 message를 받아들임
		BaseBufferedReader = new BufferedReader(new InputStreamReader(SourceSocket.getInputStream())); // Base의 message를 받아들임

		while (true) {
			// FileName 받아들임
			System.out.println("\n 1");
			FileName = BaseBufferedReader.readLine();
			System.out.println(" << FTFileName : " + FileName + ">>");
//			Thread.sleep(5000);
			if ("FT_QUIT".equals(FileName)) {
				break;
			} else {
				if (openFTListFile(FileName)) {
					sendFTFileToBase();
				} else {
					break;
				}
			} 
//			BasePrintWriter  = new PrintWriter(SourceSocket.getOutputStream(), true);
//			BasePrintWriter.println("ACK");  //잘 받았다는 메세지 보냄
		}
		System.out.println("\n   END standBy()...");
	}


	public static void sendFTFileToBase() throws Exception{
			try{ 
				// Try to connect to the Base
				System.out.println("1. Connecting to the Base (" + BaseIP + " : " + (SocketPort+1) + ").......");
	//			Socket SourceSocket = new Socket("72.2.116.17", 2011);
				BaseSocket = new Socket(BaseIP, SocketPort+1);

				BasePrintWriter = new PrintWriter(BaseSocket.getOutputStream(), true);

				while (true) {
					FTMsg = getFTListFileData();
					if (null != FTMsg) {
						// Node에서 Base로 메시지 보냄
						BasePrintWriter.println(FTMsg);  //Node에서 Base로 메시지 보냄
						System.out.println("2. Sent Message : " + FTMsg);
			Thread.sleep(1000);
					} else {
						break;
					}
				}
				BasePrintWriter.println("FT_Done");  //Node에서 Base로 메시지 보냄
				System.out.println("3. FT_Done " );
			}catch (Exception e){ 
				System.out.println("\n------------------------------------------------------------------------" + 
									"\n Connection error... (" + BaseIP + " : " + (SocketPort+1) + ")\n" + 
									" The BASE might have not waited for me. Please check it out.......\n " + 
											e + 
									"\n------------------------------------------------------------------------\n"); 
				if (BaseSocket != null)		BaseSocket.close(); // Sender 소켓 닫기
				System.exit(0); 
			}finally{ } 

			
		// Base가 잘 받았는지 확인
//		InputBufferedReader = new BufferedReader(new InputStreamReader(SourceSocket.getInputStream())); // Base로 부터 결과 메세지를 받아들임
//		String readLine = InputBufferedReader.readLine();   // Base로부터 온 전송 결과 중 한 줄을 읽음 
//		System.out.println("3. Sent Result : " + readLine);

	}



	private static void tryReConnect() {
		try
		{
//               ServerSocket.close();
			//empty my old lost connection and let it get by garbage col. immediately 
			SourceSocket=null;
			System.gc();
			//Wait a new client Socket connection and address this to my local variable
			SourceSocket = BaseServerSocket.accept(); // Waiting for another Connection
			System.out.println("Connection established...");
		}catch (Exception e) {
			System.out.println("ReConnect not successful "+e.getMessage());
		}
	}


	
	public static boolean openFTListFile(String fileName){  
		try {  
			FTListFile = new BufferedReader(new InputStreamReader(new FileInputStream(fileName))); 
		}catch (Exception e){  
			System.out.println("\n------------------------------------------------------------------------" + 
								"\nError at the openFTListFile() : " + e + 
								"\n------------------------------------------------------------------------\n"); 
			return false;
		}finally{  
		}  
		return true;
	} 


	public static String getFTListFileData(){ 
		InputLine = null; 
		try{ 
			for(InputLine = FTListFile.readLine(); InputLine != null; InputLine = FTListFile.readLine() ) { 
				if (InputLine.trim().length() > 0){ 
//					InputLine = InputLine.replaceAll(ConvertedCR, CarriageReturn); 
					break; 
				} 
			} 
		}catch (Exception e){ 
			System.out.println("\n------------------------------------------------------------------------" + 
								"\nError at the getFlatFileData() : " + e + 
								"\n------------------------------------------------------------------------\n"); 
			closeFiles(); 
			System.exit(0); 
		}finally{ 
		} 
		return InputLine; 
	} 


	public static void getEnvOptions() throws Exception {
		String optionLine;
		openOptionFile();
		optionLine = getOptionLine();
		while (null != optionLine) {
//			System.out.println(optionLine + "   '" + optionLine.substring(0,3).toLowerCase()+"'");  
			if (optionLine.toLowerCase().indexOf("socketport") > -1)	{
				SocketPort = Integer.parseInt(optionLine.substring(10).trim());
			} else if (optionLine.toLowerCase().indexOf("origin") > -1)	{
				Origin	= optionLine.substring(6).trim();  
			} else if (optionLine.toLowerCase().indexOf("baseip") > -1)	{
				BaseIP	= optionLine.substring(6).trim();  
			}
			optionLine = getOptionLine();
		}
		if (Origin.equals("Source")) {
//			socketNo = 홀수
		} else if (Origin.equals("Target")) {
//			socketNo = 짝수
		} else {
//			Origin과 Port를 확인하세요.
		}

//		SourceIP = InetAddress.getLocalHost().toString();
//		System.out.println("IP of my system is := "+SourceIP.getHostAddress());		
//		SourceIP = "192.168.0.35";
//		CommPort = 2011;

	}

	public static void openOptionFile(){  
		try {  
			OptionFile = new BufferedReader(new InputStreamReader(new FileInputStream(OptionFileName))); 
		}catch (Exception e){  
			LogString =		"\n------------------------------------------------------------------------" + 
								"\nError when open OptionFile : " + e + 
								"\n------------------------------------------------------------------------\n"; 
			displayConsoleLog(LogString, "3", "3");  
			closeFiles();  
			System.exit(0);  
		}finally{  
		}  
	} 

	public static String getOptionLine(){ 
		InputLine = null; 
		try{ 
			for(InputLine = OptionFile.readLine(); InputLine != null; InputLine = OptionFile.readLine() ) { 
				if (InputLine.trim().length() > 0 && !"rem".equals(InputLine.substring(0,3).toLowerCase())){ 
					break; 
				} 
			} 
		}catch (Exception e){ 
			LogString =		"\n-----------------------------------------------------------------------------" + 
								"\nError at the getOptionLine() : " + e + 
								"\n-----------------------------------------------------------------------------\n"; 
			displayConsoleLog(LogString, "3", "3");  
			closeFiles(); 
			System.exit(0); 
		}finally{ 
		} 
		return InputLine; 
	} 


	public static void openLogFile(){ 
		try {  
			LogFile = new BufferedWriter(new FileWriter(LogFileName, true)); 
		}catch (Exception e){  
			LogString =		"\n------------------------------------------------------------------------" + 
								"\nError when open LogFile : " + e + 
								"\n------------------------------------------------------------------------\n"; 
			System.out.println(LogString);  
			closeFiles();  
			System.exit(0);  
		}finally{  
		}  
	} 

	public static void displayConsoleLog(String logText, String consoleDispLevel, String fileLogLevel){ 
		try{ 
			if (consoleDispLevel.compareTo(ConsoleDispLevel) <= 0) {  
				System.out.print(logText);  
			}  
			if (fileLogLevel.compareTo(FileLogLevel) <= 0) {  
				LogFile.write(logText);  
			}  
		}catch(Exception e){ 
			System.out.print("\n\nError at the displayConsoleLog() : " + e); 
			e.printStackTrace(); 
			closeFiles(); 
			System.exit(0); 
		}finally{ 
		} 
	} 


	public static void closeFiles(){ 
		try { 
			if (null != LogFile)							LogFile.close(); 
			if (null != OptionFile)						OptionFile.close();
			if (null != BaseBufferedReader)	BaseBufferedReader.close();  // 입력 스트림 닫기
			if (null != BasePrintWriter)				BasePrintWriter.close();  // 출력 스트림 닫기
			if (null != SourceSocket)						SourceSocket.close();   // Node 소켓 닫기
		}catch (Exception e){ 
			System.out.println("Error at the closeFiles() : " + e); 
			System.exit(0); 
		}finally{ 
		} 
	}


}

