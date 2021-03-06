
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

	// Try and change the proxy to handle CONNECT and POST requests.
	// Play around with buffer size.
	// Send a 404 for exceptions?
	// adding class variables instead of methods ones.

	public class PersProxy 
	{
		public static final String CRLF = "\r\n";
		private static Socket proxySocket;
		
		
		@SuppressWarnings("resource")
		public static void main(String[] args) throws Exception
		{

			ServerSocket welcomeSocket = new ServerSocket(2000);

			// Communication between BROWSER - PROXY
			while (true)
			{
				proxySocket = welcomeSocket.accept();
				
				
				System.out.println("New request recieved:");
				// creates a new request class.
				Request request = new Request(proxySocket);
				
				// create a new engine for handling the request and start it up on the executor
				Engine eng = new Engine(proxySocket, request);
				eng.run();
				//service.execute(eng);

			}
			
			/* NOT REACHED */
		}
	}
