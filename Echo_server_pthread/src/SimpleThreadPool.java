import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleThreadPool {
	private static final int PORT = 8080;
	private static final int THREAD_CNT=5;
	private static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_CNT);
	public static void main(String[] args) {
		
		try {
			ServerSocket serverSocket = new ServerSocket(PORT);
			
			while(true) {
				Socket socket = serverSocket.accept();
				try {
					threadPool.execute(new ConnectionWrap1(socket));
					
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
	}

}
private final static String DB_URL = "jdbc:mysql://127.0.0.1:3306/storereservation?serverTimezone=Asia/Seoul&useSSL=false";//강희정
private final static String USER_NAME = "storeDB";//강희정
private final static String PASSWORD = "12345678";//강희정


class ConnectionWrap1 implements Runnable{
	private Socket socket = null;
	public ConnectionWrap1(Socket socket) {
		this.socket=socket;
	}
	
	@Override
	public void run() {
		try {
			OutputStream stream = socket.getOutputStream();
			stream.write(new Date().toString().getBytes());
		}catch(IOException e) {
			e.printStackTrace();
		}finally {
			try {
				socket.close();
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
}