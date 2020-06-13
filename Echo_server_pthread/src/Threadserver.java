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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Threadserver {
	private static final int PORT=9190;
	private static final int THREAD_CNT=5;
	private static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_CNT);
	static String script1;
	static ServerSocket serverSocket = null;
	
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
					threadPool.execute(new ConnectionWrap(socket, script1));
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
	private String script1=null;
	private String script2=null;
	ServerSocket serverSocket = null;
	private final static String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
	//private final static String DB_URL = "jdbc:mysql://127.0.0.1:3306/storereservation?serverTimezone=Asia/Seoul";
	//private final static String USER_NAME = "root";
	//private final static String PASSWORD = "12345678";
	private final static String DB_URL = "jdbc:mysql://127.0.0.1:3306/storereservation?serverTimezone=Asia/Seoul&useSSL=false";//강희정
	private final static String USER_NAME = "storeDB";//강희정
	private final static String PASSWORD = "12345678";//강희정
	
	static Connection conn = null;
	static Statement state = null;
	
	private int indexsave;
	private int emptytable;
	
	UserInfo ui = new UserInfo();
	
	public ConnectionWrap(Socket socket, String script1) {
		this.socket=socket;
		this.script1=script1;
	}
	String DBRead(String kind, String selectNo) {
		try {
			Class.forName(JDBC_DRIVER);
			conn=DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
			System.out.println("[ MySQL Connection  ] \n");
			state = conn.createStatement();
		    ResultSet rs;
		      
		    String sql;
		    script2 = null; //init
		    switch(kind) {
		    case "storeList":
		    	sql = "SELECT * FROM storereservation.store";
		         rs = state.executeQuery(sql);

		         while(rs.next()) {
		            String indexNo = rs.getString("indexNo");
		            String storeName = rs.getString("storeName");
		            String storeNumber = rs.getString("storeNumber");
		            String delivery = rs.getString("delivery");
		            String location = rs.getString("location");
		               if(script2==null) {
		                  script2 = indexNo + " " + storeName + " " + storeNumber + " " + delivery + " " + location +"\n";
		               }else {
		                  script2 += indexNo + " " + storeName + " " + storeNumber + " " + delivery + " " + location +"\n";
		               }
		            }
		         
		            rs.close();
		            state.close();
		            conn.close();
		            return script2;
		            
		         case "menuList":
		            sql = "SELECT * FROM storereservation.menu where indexNo = "+selectNo;
		            rs = state.executeQuery(sql);

		            while(rs.next()) {
		               String menuid = rs.getString("menuid");
		               indexsave = rs.getInt("indexNo");
		               String menuName = rs.getString("menuName");
		               String price = rs.getString("price");
		               if(script2==null)
		                  script2 = menuid + " " + menuName + " " + price +"\n";
		               else 
		                  script2 += menuid + " " + menuName + " " + price +"\n";
		            }
		            rs.close();
		            state.close();
		            conn.close();
		            return script2;
		            
		         case "emptyTable":
		        	 sql = "SELECT emptyTable FROM storereservation.store where indexNo = " + selectNo;
			         rs = state.executeQuery(sql);

			         while(rs.next())
			            emptytable = rs.getInt("emptyTable");
			         rs.close();
			         state.close();
			         conn.close();
			         return Integer.toString(emptytable);
			         
		         case "resList"://reservation list
		            sql = "SELECT * FROM storereservation.reservation where userId = '"+ selectNo+ "'";
		            rs = state.executeQuery(sql);

		            while(rs.next()) {
		               String resNo = rs.getString("resNo");
		               String indexNo = rs.getString("indexNo");
		               String userPhone = rs.getString("userPhone");
		               String userNumber = rs.getString("userNumber");
		               if(script2==null) 
		                  script2 = resNo + " " + indexNo + " " + userPhone + " " + userNumber +"\n";
		               else
		                  script2 += resNo + " " + indexNo + " " + userPhone + " " + userNumber +"\n";
		            }
		            rs.close();
		            state.close();
		            conn.close();
		            return script2;
		            
		         default :
		            script2 = "잘못된 입력입니다.";
		            return script2;
		      }
		}//try end
		catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(state!=null)
					state.close();
		    }catch(SQLException ex1) {}
		    try {
		    	if(conn!=null)
		    		conn.close();
		    }catch(SQLException ex1) {}
		}
		return script2;
	}
	void DBUpdate(String storeNo, int emptyUpdate) {
	      Connection conn = null;
	      PreparedStatement pstmt = null;
	      try {
	    	  Class.forName(JDBC_DRIVER);
	          conn=DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
	          System.out.println("[ MySQL Connection  ] \n");
	          
	          String sql;
	          script2 = null; //init  
	          sql = "UPDATE storereservation.store set emptytable=? where indexNo = ' "+storeNo+" ' ";
	          pstmt = conn.prepareStatement(sql);
	            
	          pstmt.setInt(1,emptyUpdate);
	          int r = pstmt.executeUpdate();  
	          pstmt.close();
	          conn.close();
	      }//try end
	      catch (SQLException e) { 
	    	  System.out.println("[SQL Error : " + e.getMessage() + "]"); 
	      } catch (ClassNotFoundException e1) { 
	            System.out.println("[JDBC Connector Driver 오류 : " + e1.getMessage() + "]"); 
	      } finally { //사용순서와 반대로 close 함 
	    	  if (pstmt != null) { 
	    		  try { 
	    			  pstmt.close(); 
	               } catch (SQLException e) {
	                  e.printStackTrace();
	               }
	    	  }
	          if (conn != null) { 
	        	  try { 
	        		  conn.close(); 
	               } catch (SQLException e) { 
	                  e.printStackTrace(); 
	               } 
	          }
	      }
	}
	
	static class UserInfo {
		private String UserId = null;
		private String UserPhone = null;
		private String UserNumber = null;
		
		public void putId(String id) {
		      this.UserId=id;
		}
		public void putPhone(String phone) {
			this.UserPhone = phone;
		}
		public void putNumber(String number) {
		   this.UserNumber= number;
		}
		int checkIdPhone() { //main menu 3 ==null
		   if(this.UserId==null||this.UserPhone==null) {
		      return 0;
		   }
		   else {
		      return 1;
		   }
		}
		int checkId() {//main menu 2 ==null
		   if(this.UserId==null) {
		      return 0;
		   }
		   else {
		      return 1;
		   }
		}
	}
	
	void DBUpdate(String kind, String storeNo, String emptyUpdate) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			Class.forName(JDBC_DRIVER);
		    conn=DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
		    System.out.println("[ MySQL Connection  ] \n");
		      
		    String sql;
		    script2 = null; //init
		      
		    sql = "UPDATE storereservation.store set emptytable=? where indexNo = ' "+storeNo+" ' ";
		    pstmt = conn.prepareStatement(sql);
		    
		    pstmt.setString(1,emptyUpdate);
		    pstmt.executeUpdate();
		    pstmt.close();
		    conn.close();

		}//try end
		catch (SQLException e) { 
	    	  System.out.println("[SQL Error : " + e.getMessage() + "]"); 
	      } catch (ClassNotFoundException e1) { 
	            System.out.println("[JDBC Connector Driver 오류 : " + e1.getMessage() + "]"); 
	      } finally { //사용순서와 반대로 close 함 
	    	  if (pstmt != null) { 
	    		  try { 
	    			  pstmt.close(); 
	               } catch (SQLException e) {
	                  e.printStackTrace();
	               }
	    	  }
	          if (conn != null) { 
	        	  try { 
	        		  conn.close(); 
	               } catch (SQLException e) { 
	                  e.printStackTrace(); 
	               } 
	          }
	      }
	}
	
	void DBWrite(String indexNo, UserInfo ui) { //fill1 output. 
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			Class.forName(JDBC_DRIVER);
		    conn=DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
		    System.out.println("[ MySQL Connection  ] \n");
		    
		    String sql;
		    script2 = null; //init
		    
		    sql = "UPDATE storereservation.store` SET `emptyTable` = '4' WHERE (`indexNo` = '1')";
		    pstmt = conn.prepareStatement(sql);
		      
		    pstmt.setInt(1,Integer.parseInt(indexNo));
		    pstmt.setString(2,ui.UserId);
		    pstmt.setInt(3,Integer.parseInt(ui.UserPhone));
		    pstmt.setInt(4,Integer.parseInt(ui.UserNumber));
		    int r = pstmt.executeUpdate();
		      
		    pstmt.close();
		    conn.close();
		}//try end
		catch (SQLException e) { 
	    	  System.out.println("[SQL Error : " + e.getMessage() + "]"); 
	      } catch (ClassNotFoundException e1) { 
	            System.out.println("[JDBC Connector Driver 오류 : " + e1.getMessage() + "]"); 
	      } finally { //사용순서와 반대로 close 함 
	    	  if (pstmt != null) { 
	    		  try { 
	    			  pstmt.close(); 
	               } catch (SQLException e) {
	                  e.printStackTrace();
	               }
	    	  }
	          if (conn != null) { 
	        	  try { 
	        		  conn.close(); 
	               } catch (SQLException e) { 
	                  e.printStackTrace(); 
	               } 
	          }
	      }
	}
		
	
	@Override
	public void run() {
		try {
			while(true) {
				InputStream is = socket.getInputStream();
				InputStreamReader isr = new InputStreamReader(is, "UTF-8"); //EUC_KR
				BufferedReader br = new BufferedReader(isr);
				OutputStream os = socket.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8"); //EUC_KR
				PrintWriter pw = new PrintWriter(osw, true);
				String buffer = null;
				int temp;
				
				String selstore;
				buffer = br.readLine();
				if(buffer == null) {
					System.out.println("[server] closed by client");
					break;
				}
				System.out.println("[server] recieved : "+buffer);
				
				//pw.println(a) --> a(String)을 보내는 함수
				//무조건 한번은 써야함!!
				
				if(buffer.equals("1")) {		//처음 1. 음식점 확인 누를 경우
					while(true) {
						pw.println(DBRead("storeList", "temp"));//store list send
						buffer=null;
						buffer=br.readLine();
						selstore = buffer;
						if(buffer == null) {
							System.out.println("[server] closed by client");
							break;
						}
						System.out.println("[server] recieved : "+buffer);
						pw.println(DBRead("menuList", buffer));
						
						temp = Integer.parseInt(DBRead("emptyTable", Integer.toString(indexsave)));	//3
						//System.out.println(temp);
						System.out.println(indexsave);
						//System.out.println(DBRead("emptyTable", Integer.toString(indexsave)));
						
						
						DBUpdate(Integer.toString(indexsave),temp-1);				//자리 꽉찼을 시 예약안되게끔 추가
						
						buffer=null;
						buffer=br.readLine();
						if(buffer == null) {
							System.out.println("[server] closed by client");
							break;
						}
						pw.println("인원수를 입력하세요"); //read Usernum
						buffer=null;
						buffer=br.readLine();
						ui.UserNumber = buffer;
						System.out.println(ui.UserId + " " + ui.UserPhone);
						if(ui.UserId==null || ui.UserPhone==null) {
							pw.println("예약 아이디와 전화번호를 입력하세요");
							break;
						}
						DBWrite(selstore, ui);
						pw.println("예약 완료되었습니다.");
						break;
					}
				}else if(buffer.equals("2")) {		//처음 2. 예약확인 누를경우
					pw.println("예약을 확인할 아이디를 입력하세요");
					buffer=null;
					buffer=br.readLine();
					if(buffer == null) {
						System.out.println("[server] closed by client");
						break;
					}
					pw.println(DBRead("resList", buffer));
				}else if(buffer.equals("3")) { //3. 정보입력
					
					pw.println("아이디를 입력해주세요.");
					buffer=null;
					buffer=br.readLine();
					
					ui.putId(buffer);
					pw.println("전화번호를 입력해주세요.");
					buffer=null;
					buffer=br.readLine();
					ui.putPhone(buffer);
					pw.println("입력되었습니다.");
					System.out.println(ui.UserId + " " + ui.UserPhone);
						
					continue;
				}else {								//이상한 번호를 눌렀을 경우
					pw.println("다시 선택하여주십시오");
					continue;
				}
				
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