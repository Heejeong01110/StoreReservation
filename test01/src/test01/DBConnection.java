package test01;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBConnection {

	private Connection conn;
	private Statement st;
	private ResultSet rs;
	
	public DBConnection() {
		try 
		{
			Class.forName("com.mysql.jdbc.Driver");
			conn=DriverManager.getConnection("jdbc:mysql://localhost:3306/restaurant","root", "0000");
			st=conn.createStatement();
			
			System.out.println("데이터베이스 연결 성공");
		}
		catch(Exception e) 
		{
			System.out.println("데이터베이스 연결 오류:"+e.getMessage());
		}
	}
	public boolean store(String indexNo, String storeName, String storeNumber, String delivery, int emptyTable, String location, int watingNumber) 
	{
		try 
		{
			String SQL ="SELECT * FROM resNo WHERE storeName='"+storeName+ "'and storeNumber='"+storeNumber+"'ans delivery='"+delivery+
					"'and emptyTable="+emptyTable+ "and location='"+location+ "'and watingNumber="+ watingNumber;
			rs=st.executeQuery(SQL);
			if(rs.next()) 
			{
				return true;
			}
		}
		catch(Exception e) 
		{
			System.out.println("데이터베이스 검색 오류:"+e.getMessage());
		}
		return false;
	}
	public boolean menu(String indexNo, String storeName, String storeNumber, String delivery, int emptyTable, String location, int watingNumber) 
	{
		try 
		{
			String SQL ="SELECT * FROM resNo WHERE indexNo="+indexNo;
			rs=st.executeQuery(SQL);
			if(rs.next()) 
			{
				return true;
			}
		}
		catch(Exception e) 
		{
			System.out.println("데이터베이스 검색 오류:"+e.getMessage());
		}
		return false;
	}
	public boolean reservation(String indexNo, String storeName, String storeNumber, String delivery, int emptyTable, String location, int watingNumber) 
	{
		try 
		{
			String SQL ="SELECT * FROM resNo WHERE indexNo="+indexNo;
			rs=st.executeQuery(SQL);
			if(rs.next()) 
			{
				return true;
			}
		}
		catch(Exception e) 
		{
			System.out.println("데이터베이스 검색 오류:"+e.getMessage());
		}
		return false;
	}
}
