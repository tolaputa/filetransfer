/***********************************************************************************************
* Program Description : File Transfer를 제어하는 Manager
*                              제공 받은 File 목록의 파일을 
*							  - Source에서 읽어 전달해 달라고 요청
*							  - 전달받은 내용을 Target으로 전달
*							  - 각 상황을 기록
* Created DateTime : 2019/08/14
***********************************************************************************************/

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
//	import dbmgr.dbmgrb.*;
//	import util.DataFactory;


class C143FileTransferMgr { 
	// Declare global variables. They are starting with capital letter. 
	private static String SysDelimiter = ";", MyDelimiter = "&&", MyDelimiterWithTab = "	&&"; 
	private static String ReportStatus, FileLogging, SysPrint, StartDateTime, FinishDateTime, LogString; 
	private static String ProjectId, MigDate, Round, GroupId, Seq, AcceptValue, FileList, FTFileName, FTListName, LoadType="" ; 
	private static String FromNum="", BatchLimit="", BatchSize="", FromDate="", ToDate=""; 
	private static String InputLine = "", OutputLine = "", InvalidParameters ="", MsgLine=""; 
	private static String OutputFiles = ""; 
	private static String SourceIPAddr, SourceOSType; 
	private static String TargetIPAddr, TargetOSType; 

	private static String asIsValue = "", OfTotalCount = ""; 
	private static long ST, FT;								// start and finish timemill 
	private static long SelectedCount=0, ProcessedCount=0, UpdatedCount=0, DeletedCount=0, TotalCount = 0, MigBatchLimit=0; 
	private static int SourceSocketPort=0, TargetSocketPort=0; 
	private static int MigDisplayCount=0, MigBatchSize=0; 
	private static int ProcessNum=0, RecordCount = 0;

	private static String ProgramId = "C143FileTransferMgr";		// provided template generator with id, but can be changed by user 
	private static String Version = "Ver 2.1.1";
	private static String LogFileName, InputFileName, OutputFileName, FilePath, SourceColumnNames = "", TargetColumnNames = "";  


	// File Transfer에서 사용되는 CarriageReturn
	// Unix와 Window는 해당값이 달라, S/T CarriageReturn Option으로 사용한다.
 	private static String CR="\\r", LF="\\n", CRLF="\r\n";


	private static Socket SourceSocket, BaseSocket, TargetSocket; 
	private static ServerSocket SourceServerSocket, BaseServerSocket, TargetServerSocket;
	private static PrintWriter SourcePrintWriter, TargetPrintWriter;
	
	private static BufferedReader SourceBufferedReader, TargetBufferedReader;  
	private static BufferedReader FTListFile = null;  
	private static BufferedWriter OutputFile = null;  
	private static BufferedWriter LogFile = null;  

	private static Connection BConn = null;  

	// User can define variables at the below few lines. They are starting with small letter.  
	// Declare global variables.  



	// Main Flow  
	public static void main(String[] arg) throws Exception{  
		doPreparation(arg);  // 파일목록과 option값 설정
		transferSourceToTarget();  
		doFinalProcess();  
	}  



	/************************************************************************* 
	  1. accept optional values and record initial progress
	*************************************************************************/ 
	public static void doPreparation(String[] arg)  throws Exception{ 
		ST = System.currentTimeMillis(); // for calculate elapsed time 
//		StartDateTime = DataFactory.getCurrentDateTime("/", " ", ":");
		StartDateTime = new SimpleDateFormat().format(new Date());
		LogString =		"\n\n------------------------------------------------------------------------" +
							"\n Program Id : " + ProgramId + "   " + Version + "   " + StartDateTime + 
							"\n------------------------------------------------------------------------"; 

		AcceptValue = ""; 
		for(int i=0; i<arg.length; i++){ 
			AcceptValue = AcceptValue + (arg[i]); 
		} 
		String[] temp = AcceptValue.split(SysDelimiter); 
//		for(int i=0; i<temp.length; i++){			// you may use this function for checking the accept values 
//			System.out.println(i + " " + temp[i]); 
//		} 

		// LogFileName should have LogFileName, MigDate, Round and GroupID 
		for(int i=0; i<temp.length; i++) {
			if (temp[i].toLowerCase().indexOf("projectid") > -1)	{
				ProjectId	 = temp[i].substring(9);
			} else if (temp[i].toLowerCase().indexOf("ftlistname") > -1)	{
				FTListName	= temp[i].substring(10);  
			} else {
				InvalidParameters = InvalidParameters + temp[i]; 
			}
		}
//		System.out.println("FTListName = " + FTListName);
		if (!"".equals(InvalidParameters)) { 
			System.out.println("\n Invalid accept parameter....  " + InvalidParameters + "\n");   
			reportStatus(5, "Invalid accept parameter....  " + InvalidParameters);   
			closeFiles();   
			System.exit(0);   
		}

		LogFileName = "D:\\MigPro\\executable\\log\\"+ProjectId + "_C143FileTransferMgr_"+StartDateTime.replaceAll("/", "").substring(0,8)+".miglog";

		openLogFile( ); 

		SysPrintLog(LogString);  // Announce program start 
		SysPrintLog("\nAcceptValue : " + AcceptValue); 

		// TransferList 존재 여부 확인
		openFTListFile();

		// 환경변수 가져오기
		getFTEnvOptions();

		// User can define accept values at the below few lines  

		reportStatus(1, ""); 
	} 


	/*************************************************************************
	  2. get FT File from Source and transfer to the Target
	*************************************************************************/
	public static void transferSourceToTarget() throws Exception {
		SourceSocket = new Socket(SourceIPAddr, SourceSocketPort);
		SourcePrintWriter = new PrintWriter(SourceSocket.getOutputStream(), true);
//		sendMsg(SourceIPAddr, SourceSocketPort, "Ready1?");
		try{
			long st = System.currentTimeMillis(); // for calculate elapsed time 
			FTFileName = getFTFileName();
			System.out.println("FTFileName = " + FTFileName);
			while (null != FTFileName) {
				sendMsg(SourceIPAddr, SourceSocketPort, FTFileName);
				transferSourceMsgToTarget(); // Source로 부터 받은 msg를 Target으로 전달
				FTFileName = getFTFileName();
			}

			reportStatus(3, ""); 

		}catch(Exception e){ 
			String err = e + ""; 
			LogString =		"\n------------------------------------------------------------------------" + 
								"\nError at the transferSourceToTarget() : " + e + 
								"\n------------------------------------------------------------------------\n"; 
			SysPrintLog(LogString);  
			e.printStackTrace();  
			closeFiles(); 
			System.exit(0); 
		}finally{ 
		} 
	} 




	/*************************************************************************
	  4. final process 
	*************************************************************************/
	public static void doFinalProcess(){

		/*LogString = "\n------------------------------------------------------------------------\n" +
						"Completed successfully... (" + ProgramId + ")   " + DataFactory.getCurrentDateTime("/", " ", ":") + "\n" + 
						"OutputFileName : " + OutputFileName + "\n" + 
						"Elapsed time (hh:mm:ss) : " + DataFactory.getElapsedTime(ST, ":") + "\n" + 
						"TotalCount : " + DataFactory.numberEdit(ProcessedCount) +  "\n" +
						"------------------------------------------------------------------------\n\n\n";
		SysPrintLog(LogString);*/
		closeFiles();
	}


	// report current status to the Dashboard and log the status  
	public static void reportStatus(int phase, String processNum){ 
		try{ 
			/*SysPrintLog("\n   - reportStatus(" + phase + ")  Elapsed : " + DataFactory.getDurationActual(ST, "min") + "min" + "\n" +
							"       Processed : _" + processNum + "  " + DataFactory.numberEdit(SelectedCount) +
							"   Timestamp : " + DataFactory.getCurrentDateTime("/", " ", ":"));  */

		}catch(Exception e){ 
			LogString =		"\n------------------------------------------------------------------------" + 
								"\nError at the reportStatus() : " + e + 
								"\n------------------------------------------------------------------------\n"; 
			SysPrintLog(LogString);  
			closeFiles(); 
			System.exit(0); 
		}finally{ 
		} 
	} 


	public static void transferSourceMsgToTarget() throws Exception {
		String msg = "";
		SourceServerSocket = new ServerSocket(SourceSocketPort+1);
		TargetServerSocket = new ServerSocket(TargetSocketPort);

		// Source의 접속을 기다린다.
		SourceSocket = SourceServerSocket.accept(); // Source에서 message를 보낼 때 까지 대기한다.
		// Source의 message를 받아들임
		SourceBufferedReader = new BufferedReader(new InputStreamReader(SourceSocket.getInputStream())); // Source의 message를 받아들임

		while (true) {
			// msg 받아들임
			msg = SourceBufferedReader.readLine();
			if ("FTDone".equals(msg)) {
				break;
			} else {
//				TargetPrintWriter  = new PrintWriter(TargetSocket.getOutputStream(), true);
//				TargetPrintWriter.println(msg);  // Target으로 메세지 보냄
				System.out.println("Transfer Message : " + msg);
				Thread.sleep(1000);
			} 
		}
		System.out.println("\n   FTDone.");

	}


	public static String getFTFileName(){ 
		String inputLine = null; 
		try{ 
			for(inputLine = FTListFile.readLine(); inputLine != null; inputLine = FTListFile.readLine() ) { 
				if (-1 <inputLine.toLowerCase().indexOf("rem") && inputLine.toLowerCase().indexOf("rem") < 3) { // REM은 수행하지 않는다.
					// skip
				} else if ("".equals(inputLine.trim())) {
					// skip
				} else {
					break;
				}
			} 
//			System.out.println("inputLine = " +inputLine);
		}catch (Exception e){ 
			LogString =		"\n------------------------------------------------------------------------" + 
								"\nError at the getFlatFileData() : " + e + 
								"\n------------------------------------------------------------------------\n"; 
			SysPrintLog(LogString);  
			closeFiles(); 
			System.exit(0); 
		}finally{ 
		} 
		return inputLine; 
	} 


	public static long getTotalCount(){ 
/*		String InputLine = null; 
		long totalCount = 0; 
		try{ 
			for(InputLine = InputFile.readLine(); InputLine != null; InputLine = InputFile.readLine() ) { 
				if (InputLine.trim().length() > 0){ 
					totalCount++; 
				} 
			} 
		}catch (Exception e){ 
			LogString =		"\n------------------------------------------------------------------------" + 
								"\nError at the getTotalCount() : " + e + 
								"\n------------------------------------------------------------------------\n"; 
			SysPrintLog(LogString);  
			closeFiles(); 
			System.exit(0); 
		}finally{ 
		} */
		return 0; 
	} 


	public static void sendMsg(String ipAddr, int socketPort, String msg) throws Exception { 
		try{ 
			// Try to connect to the Receiver
			System.out.println("1. Connecting to the Node (" + ipAddr + " : " + socketPort + ").......");
//			SourceSocket = new Socket(ipAddr, socketPort);
			// Base에서 목적지로 메시지 보냄
//			SourcePrintWriter = new PrintWriter(SourceSocket.getOutputStream(), true);
			SourcePrintWriter.println(msg);  //Sender에서 Receiver로 메시지 보냄
	//		SourcePrintWriter.print(msg);  //Sender에서 Receiver로 메시지 보냄
//			SourcePrintWriter.flush();
			System.out.println("2. Sent Message : " + msg);
		}catch (Exception e){ 
			System.out.println("\n------------------------------------------------------------------------" + 
								"\n Connection error at the Receiver (" + ipAddr + " : " + socketPort + ")\n" + 
								" The receiver might have not waited for senders. Please check it out.......\n " + 
										e + 
								"\n------------------------------------------------------------------------\n"); 
			if (SourceSocket != null)		SourceSocket.close(); // 목적지로 향하는 소켓 닫기
			System.exit(0); 
		}finally{ } 


	}

	public static void SysPrintLog(String arg){ 
		try{ 
			LogFile.write(arg); 
			System.out.println(arg); 
		}catch(Exception e){ 
			System.out.println("Error at the SysPrintLog() : " + e); 
			closeFiles(); 
			System.exit(0); 
		}finally{ 
		} 
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


	public static void openOutputFile(){  
		try {  
			OutputFile = new BufferedWriter(new FileWriter(OutputFileName));  
		}catch (Exception e){  
			LogString =		"\n------------------------------------------------------------------------" + 
								"\nError when open OutputFile : " + e + 
								"\n------------------------------------------------------------------------\n"; 
			SysPrintLog(LogString);  
			closeFiles();  
			System.exit(0);  
		}finally{  
		}  
	}  


	public static void openFTListFile(){  
		try {  
			FTListFile = new BufferedReader(new InputStreamReader(new FileInputStream(FTListName))); 
		}catch (Exception e){  
			LogString =		"\n------------------------------------------------------------------------" + 
								"\nError when open FTListFile : " + e + 
								"\n------------------------------------------------------------------------\n"; 
			SysPrintLog(LogString);  
			closeFiles();  
			System.exit(0);  
		}finally{  
		}  
	} 

	public static void getFTEnvOptions(){ // Base에 정의된 FT 환경변수 값을 읽어들인다.
		/*PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuffer sqlList = new StringBuffer();
		String sql = "SELECT GenCode, Description, GenValue, GenValue1, GenValue2, Active FROM MigCode " + 
						" WHERE GenType = 'FileTransfer' AND GenCode = ? "; // GenCode : ProjectId _ S/T

		try{
			BConn = dbmgr.dbmgrb.DBManager.getConnection();
			sqlList.append(sql);

			pstmt = BConn.prepareStatement(sqlList.toString());
			pstmt.setString(1, ProjectId + "_S");

			rs = pstmt.executeQuery();
			if (rs.next()) { 
				SourceIPAddr			= rs.getString("GenValue");
				SourceSocketPort	= Integer.parseInt(rs.getString("GenValue1").trim());
				SourceOSType			= rs.getString("GenValue2");
			}

			sqlList = new StringBuffer();
			sqlList.append(sql);

			pstmt = BConn.prepareStatement(sqlList.toString());
			pstmt.setString(1, ProjectId + "_T");

			rs = pstmt.executeQuery();
			if (rs.next()) { 
				TargetIPAddr			= rs.getString("GenValue");
				TargetSocketPort	= Integer.parseInt(rs.getString("GenValue1").trim());
				TargetOSType			= rs.getString("GenValue2");
			}

//			System.out.println("SourceIPAddr : " + SourceIPAddr); 
//			System.out.println("TargetSocketPort : " + TargetSocketPort); 

		}catch (Exception e){  
			LogString =		"\n------------------------------------------------------------------------" + 
								"\nError at the getEnvOptions() : " + e + 
								"\n------------------------------------------------------------------------\n"; 
			SysPrintLog(LogString);  
			closeFiles();  
			System.exit(0);  
		}finally{  
			dbmgr.dbmgrb.DBManager.close(rs, pstmt, BConn); 
		}  */
	}

	public static void closeFiles(){ 
		try { 
			if (null != LogFile) LogFile.close(); 
//			InputFile.close();
			if (null != OutputFile) OutputFile.close();
		}catch (Exception e){ 
			System.out.println("Error at the closeFiles() : " + e); 
			System.exit(0); 
		}finally{ 
		} 
	}


}  // The end of the program... 
