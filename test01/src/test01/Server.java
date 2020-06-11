package test01;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.InetAddress;

public class Server{
	static ExecutorService executorService; //스레드풀
	static ServerSocket serverSocket;
	static List<Client> connections = new Vector<Client>();
	DBConnection connection = new DBConnection(); //DB와 연동
	
	public static final int PORT = 9000;
	static void startServer() 
	{
		// 서버 시작 시 호출
		// 스레드풀 생성
		executorService = Executors.newFixedThreadPool
		(
			Runtime.getRuntime().availableProcessors()
	    );
		
		try 
		{
			serverSocket = new ServerSocket();	// 1. 소켓 생성
			InetAddress inetAddress = InetAddress.getLocalHost();
			String localhost = inetAddress.getHostAddress();
			serverSocket.bind(new InetSocketAddress(localhost, PORT)); //2. 바인딩
			System.out.println("[서버] binding " + localhost);
		} catch(Exception e) 
		{
			if(!serverSocket.isClosed()) 
			{ 
				stopServer(); //서버 종료 시 호출
			}
			return;
		}
		
		// 수락 작업 생성
		Runnable runnable = new Runnable() 
		{
			@Override
			public void run() 
			{	
				System.out.println("[서버 시작]");
				while(true) 
				{
					try 
					{
						// 연결 수락
						Socket socket = serverSocket.accept(); // 3. accept (클라이언트 연결요청 기다림)
						System.out.println("[연결 수락: " + socket.getRemoteSocketAddress()  + ": " + Thread.currentThread().getName() + "]");		
						// 클라이언트 접속 요청 시 객체 하나씩 생성해서 저장
						Client client = new Client(socket);
						connections.add(client); //
						System.out.println("[연결 개수: " + connections.size() + "]");
					} catch (Exception e) 
					{
						if(!serverSocket.isClosed()) 
						{ 
							stopServer(); //서버 종료 시 호출
						}
						break;
					}
				}
			}
		};
		// 스레드풀에서 처리
		executorService.submit(runnable);
	}
	static void stopServer() //서버 종료시 호출
	{ 
		try 
		{
			// 모든 소켓 닫기
			Iterator<Client> iterator = connections.iterator();
			while(iterator.hasNext()) 
			{
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			// 서버 소켓 닫기
			if(serverSocket!=null && !serverSocket.isClosed()) 
			{ 
				serverSocket.close(); 
			}
			// 스레드풀 종료
			if(executorService!=null && !executorService.isShutdown()) 
			{ 
				executorService.shutdown(); 
			}
			System.out.println("[서버 멈춤]");
		} catch (Exception e) 
		{ 
		}
	}
	static class Client 
	{
		Socket socket;
		
		Client(Socket socket) 
		{
			this.socket = socket;
			send("방문해주셔서 감사합니다. 원하시는 항목을 선택해주세요. \n");
			while(true)
			{
				send("1. 주변 음식점 찾기, 2. 예약 내역 확인하기, 3. 개인정보 입력, 4.종료\n" 
				+"번호 선택:\n");
			
				int receive_a = 0;
				receive(); //클라이언트가 선택한 번호를 받기
				
				if(receive_a==1) //1.주변 음식점 찾기
				{
					/*
					if() 
					{
					String store_infor = connection.store();
					send("식당 목록:"+ store_infor);
					}
					else if() 
					{
						
					}
					*/
				}
				else if(receive_a==2) //2.예약 내역 확인하기
				{
					
				} 
				else if(receive_a==3) //3.개인정보 입력
				{
					send("개인정보가 입력되었습니다.");
				} 
			}
		}
		
		void receive() 
		{
			// 받기 작업 생성
			Runnable runnable = new Runnable() 
			{
				@Override
				public void run() 
				{
					try 
					{
						while(true) 
						{
							byte[] byteArr = new byte[100];
							InputStream inputStream = socket.getInputStream();
							
							// 데이터 read
							int readByteCount = inputStream.read(byteArr);
							
							// 클라이언트가 정상적으로 Socket의 close()를 호출했을 경우
							if(readByteCount == -1) 
							{  
								throw new IOException(); 
							}
							
							System.out.println("[요청 처리: " + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName() + "]");
							
							// 문자열로 변환
							String data = new String(byteArr, 0, readByteCount, "UTF-8");
							
							// 클라이언트가 stop server라고 보내오면 서버 종료
							if(data.equals("stop server")) 
							{
								stopServer();
							}
							
							// 모든 클라이언트에게 데이터 보냄
							for(Client client : connections) 
							{
								client.send(data); 
							}
						}
					} catch(Exception e) 
					{
						try 
						{
							connections.remove(Client.this);
							System.out.println("[클라이언트 통신 안됨: " + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName() + "]");
							socket.close();
						} catch (IOException e2) 
						{
						}
					}
				}
			};
			// 스레드풀에서 처리
			executorService.submit(runnable);
		}
	
		void send(String data) 
		{
			// 보내기 작업 생성
			Runnable runnable = new Runnable() 
			{
				@Override
				public void run() 
				{
					try 
					{
						// 클라이언트로 데이터 보내기
						byte[] byteArr = data.getBytes("UTF-8");
						OutputStream outputStream = socket.getOutputStream();
						// 데이터 write
						outputStream.write(byteArr);
						outputStream.flush();
					} catch(Exception e) 
					{
						try 
						{
							System.out.println("[클라이언트 통신 안됨: " + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName() + "]");
							connections.remove(Client.this);
							socket.close();
						} catch (IOException e2) {}
					}
				}
			};
			// 스레드풀에서 처리
			executorService.submit(runnable);
		}
	}
	
	public static void main(String[] args) 
	{
		startServer();
	}
}