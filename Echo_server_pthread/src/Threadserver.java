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
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Threadserver {
	private static final int PORT=9190;
	private static final int THREAD_CNT=5;
	private static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_CNT);

	static ServerSocket serverSocket = null;
	
	Scanner sc = new Scanner(System.in);
	public static void main(String[] args) {
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
				//outputStream 가져와서 StreamWriter, PrintWriter로 감싼다
				OutputStream os = socket.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
				PrintWriter pw = new PrintWriter(osw, true);
				
				String buffer = null;
				buffer = br.readLine();
				
				if(buffer == null) {
					System.out.println("[server] closed by client");
					break;
				}
				System.out.println("[server] recieved : "+buffer);
				pw.println(buffer);
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