import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.sql.*;

public class Threadserver {
	private static final int PORT=9190;
	private static final int THREAD_CNT=5;
	private static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_CNT);
	private final static String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
	private final static String DB_URL = "jdbc:mysql://127.0.0.1:3306/storereservation?serverTimezone=Asia/Seoul";
	private final static String USER_NAME = "root";
	private final static String PASSWORD = "12345678";
	
	static ServerSocket serverSocket = null;
	
	Scanner sc = new Scanner(System.in);
	public static void main(String[] args) {
		Connection conn = null;
		Statement state = null;
		try {
			Class.forName(JDBC_DRIVER);
			conn=DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
			System.out.println("[ MySQL Connection  ] \n");
			state = conn.createStatement();
			
			String sql;
			sql = "SELECT * FROM storereservation.store";
			ResultSet rs = state.executeQuery(sql);
			while(rs.next()) {
				String indexNo = rs.getString("indexNo");
				String storeName = rs.getString("storeName");
				String storeNumber = rs.getString("storeNumber");
				String delivery = rs.getString("delivery");
				String location = rs.getString("location");
				String full1 = indexNo + " " + storeName + " " + storeNumber + " " + delivery + " " + location;
				System.out.println(full1);
			}
			
			rs.close();
			state.close();
			conn.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(state!=null)
					state.close();
			}catch(SQLException ex1) {
				
			}
			try {
				if(conn!=null)
					conn.close();
			}catch(SQLException ex1) {
				
			}
		}
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress("0.0.0.0", PORT));
			
			System.out.println("[server] binding");
			while(true) {
			Socket socket = serverSocket.accept();
			InetSocketAddress socketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
			System.out.println("[server] connected by client");
			System.out.println("[server] Connect with " + socketAddress.getHostString() + " " + socket.getPort());
			try {
				threadPool.execute(new ConnectionWrap(socket));
			}catch(Exception e) {
				e.printStackTrace();
			}
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
}

class ConnectionWrap implements Runnable{
	private Socket socket = null;
	
	//DBManager db = new DBManager(); //db관리
	
	ServerSocket serverSocket = null;
	public ConnectionWrap(Socket socket) {
		this.socket=socket;
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				InputStream is = socket.getInputStream();
				InputStreamReader isr = new InputStreamReader(is, "UTF-8");
				BufferedReader br = new BufferedReader(isr);
				//가져와서 StreamWriter, PrintWriter로 감싼다
				OutputStream os = socket.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
				PrintWriter pw = new PrintWriter(osw, true);
				String intro = "\n 안녕하세요 \n 안녕하세요";
				
				String buffer = null;
				buffer = br.readLine();
				//db.storeList(); 
				
				if(buffer == null) {
					System.out.println("[server] closed by client");
					break;
				}
				System.out.println("[server] recieved : "+buffer);
				//pw.println(buffer);
				pw.println(intro);
			}
		}catch(IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if(serverSocket !=null && !serverSocket.isClosed())
					serverSocket.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}

class DBManager {
	private final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private final String DB_URL = "jdbc:mysql://localhost/storereservation?&useSSL=false"; //접속할 DB 서버
	
	private final String USER_NAME = "storeDB"; //DB에 접속할 사용자 이름을 상수로 정의
	private final String PASSWORD = "12345678"; //사용자의 비밀번호를 상수로 정의
	
	Connection conn = null;
	Statement state = null;
	
	DBManager(){
		try {
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
			state = conn.createStatement();
		} catch(ClassNotFoundException cnfe) {       
			cnfe.printStackTrace();
		}
		catch(SQLException se) {
			se.printStackTrace();
		}
		finally {
			if(conn!=null) try {conn.close();} catch(SQLException se) {}
			if(state!=null) try {state.close();} catch(SQLException se) {}
					
		}
	}
	void storeList() {
		String sql; //SQL문을 저장할 String
		sql = "SELECT * FROM store";
		ResultSet rs;
		try {
			rs = state.executeQuery(sql);//SQL문을 전달하여 실행
			System.out.println("번호 | 가게이름 | 빈 좌석 수");
			while(rs.next()){
				int indexNo = rs.getInt("indexNo");
				String storeName = rs.getString("storeName");
				int emptyTable = rs.getInt("emptyTable");
				
				
				System.out.println(indexNo+ " | "+storeName+" | "+emptyTable); 
			    }
			    //rs.close();
				//state.close();
				//conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		
	}
	void menuList(int indexNo) {
		String sql; //SQL문을 저장할 String
		sql = "SELECT menuid, menuName, price FROM menu where indexNo="+indexNo;
		ResultSet rs;
		try {
			rs = state.executeQuery(sql);//SQL문을 전달하여 실행
			while(rs.next()){
				int menuid = rs.getInt("indexNo");
				String storename = rs.getString("storeName");
				String storeNumber = rs.getString("storeNumber");
				//String delivery = rs.getString("delivery");
				int emplyTable = rs.getInt("emptyTable");
				int watingNumber = rs.getInt("watingNumber");
				String location = rs.getString("location");
				//System.out.println("Number: "+ number + "\nName: " + name + "\nKOR: " + kor); 
				//System.out.println("MATH: "+ math + "\nENG: " + eng + "\n-------------\n");
		    }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
					
		    
	}
			
		 
		
	}
