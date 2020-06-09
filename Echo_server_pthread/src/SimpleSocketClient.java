import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class SimpleSocketClient {
	private static final int PORT = 8080;

	public static void main(String[] args) {
		try {
			Socket socket = new Socket("localhost", PORT);
			while(true) {
			InputStream stream = socket.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			String response = br.readLine();
			System.out.println(response);
			Thread.sleep(5000);
			}
			//socket.close();
			//System.exit(0);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
