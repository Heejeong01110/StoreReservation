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

public class Threadserver {
	private static final int PORT=9190;
	private static final int THREAD_CNT=5;
	private static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_CNT);
	private final static String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
	private final static String DB_URL = "jdbc:mysql://127.0.0.1:3306/storereservation?serverTimezone=Asia/Seoul";
	private final static String USER_NAME = "root";
	private final static String PASSWORD = "12345678";
	static String full1;
	
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
				if(full1==null) {
					full1 = indexNo + " " + storeName + " " + storeNumber + " " + delivery + " " + location +"\n";
				}else {
				full1 += indexNo + " " + storeName + " " + storeNumber + " " + delivery + " " + location +"\n";
				}
				//System.out.println(full1);
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
				threadPool.execute(new ConnectionWrap(socket, full1));
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
	private String full1=null;
	ServerSocket serverSocket = null;
	public ConnectionWrap(Socket socket, String full) {
		this.socket=socket;
		this.full1=full;
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				InputStream is = socket.getInputStream();
				InputStreamReader isr = new InputStreamReader(is, "UTF-8");
				BufferedReader br = new BufferedReader(isr);
				//outputStream 가져와서 StreamWriter, PrintWriter로 감싼다
				OutputStream os = socket.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
				PrintWriter pw = new PrintWriter(osw, true);
				String intro = "\n 안녕하세요 \n 안녕하세요";
				
				String buffer = null;
				buffer = br.readLine();
				
				if(buffer == null) {
					System.out.println("[server] closed by client");
					break;
				}
				System.out.println("[server] recieved : "+buffer);
				if(buffer.equals("1")) {
					System.out.println("다음거 ㄱㄱ");
					pw.println("다음거 ㄲ");
					buffer=null;
					while(true) {
						buffer=br.readLine();
						System.out.println("[server] recieved : "+buffer);
						if(buffer.equals("1")) {
							System.out.println("그다음거 하자");
							pw.println("그다음거 하자");
						}
					}
				}
				//pw.println(buffer);
				pw.println(full1);
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