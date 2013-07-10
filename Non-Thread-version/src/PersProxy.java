
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
			proxySocket = welcomeSocket.accept();		
			Executor service = Executors.newCachedThreadPool();
			
			// Communication between BROWSER - PROXY
			while (true)
			{
				// make sure the proxy socket is still open
				if (proxySocket.isClosed())
				{
					proxySocket = welcomeSocket.accept();
					System.out.println("The proxy socket was closed! Had to re-open it inside the while loop");
				}
				
				// creates a new request class.
				Request request = new Request(proxySocket);
				
				// create a new engine for handling the request and start it up on the executor
				Engine eng = new Engine(proxySocket, request);
				service.execute(eng);
			}
			
			/* NOT REACHED */
		}

		/* This method reads the request
		 * 
		 */
	}
