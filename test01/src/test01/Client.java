package test01;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client{
	static Socket socket;
	
	static void startClient() {
		// 스레드 생성
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					// 소켓 생성 및 연결 요청
					socket = new Socket();
					socket.connect(new InetSocketAddress("localhost", 9191));
				} catch(Exception e) {
					System.out.println("[서버 통신 안됨]");
					if(!socket.isClosed()) { stopClient(); }
					return;
				}
				// 서버에서 보낸 데이터 받기
				receive();
			}
		};
		// 스레드 시작
		thread.start();
	}
	
	static void stopClient() {
		try {
			System.out.println("[연결 끊음]");
			// 연결 끊기
			if(socket!=null && !socket.isClosed()) {
				socket.close();
			}
		} catch (IOException e) {}
	}	
	
	static void receive() {
		while(true) {
			try {
				byte[] byteArr = new byte[100];
				InputStream inputStream = socket.getInputStream();
				
				// 데이터 read
				int readByteCount = inputStream.read(byteArr);
				
				// 서버가 정상적으로 Socket의 close()를 호출했을 경우
				if(readByteCount == -1) { throw new IOException(); }
				
				// 문자열로 변환
				String data = new String(byteArr, 0, readByteCount, "UTF-8");
				
				System.out.println("[받기 완료] "  + data);
			} catch (Exception e) {
				System.out.println("[서버 통신 안됨]");
				stopClient();
				break;
			}
		}
	}
	
	static void send(String data) {
		// 스레드 생성
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					// 서버로 데이터 보내기
					byte[] byteArr = data.getBytes("UTF-8");
					OutputStream outputStream = socket.getOutputStream();
					// 데이터 write
					outputStream.write(byteArr);
					outputStream.flush();
					System.out.println("[보내기 완료]");
				} catch(Exception e) {
					System.out.println("[서버 통신 안됨]");
					stopClient();
				}				
			}
		};
		thread.start();
	}

	
	public static void main(String[] args) {
		startClient();
		while(true) {
			Scanner sc = new Scanner(System.in);
			String message = sc.nextLine();
			// stop client라고 입력하면 해당 클라이언트 종료
			if(message.equals("stop client"))
				break;
			send(message);
		}
		stopClient();
	}
}