import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;

// test change
public class Engine implements Runnable
{

	static private Hashtable<String, Socket> hosts = new Hashtable<String, Socket>();
	private Socket browserToProxyConn;
	private Request request;
	private Response response;
	
	// The engine gets a request from the main proxy and a connection to which send the reply.
	public Engine(Socket browserToProxyConn, Request request)
	{
		this.browserToProxyConn = browserToProxyConn;
		this.request = request;
	}
	
	@Override
	public void run()
	{	
		Socket toWebServer = null;
		
		try
		{	// Get the socket to talk to the remote web server - either create or re-use.
			toWebServer = checkTCPConnection();
		}
		catch (UnknownHostException e) 
		{
			System.out.println("Host could not be found");
			e.printStackTrace();
		}
		catch (IOException e)
		{
			System.out.println("IO Exception!");
			e.printStackTrace();
		}
		
		if (toWebServer != null)
		{
			
			try
			{	// send the request to the web server
				request.sendRequestToWebServer(toWebServer);
			}
			catch (IOException e)
			{
				System.out.println("Could not send the request to the web server!");
				e.printStackTrace();
			}
			
			try 
			{
				this.response = new Response(request, toWebServer);
				//System.out.println("New response created.");
			} 
			catch (IOException e) 
			{
				System.out.println("Could not create a new response!");
				e.printStackTrace();
			}
			
			// Read and parse the response
			try 
			{
				response.getFromServer();
				System.out.println("Response recieved!");
				System.out.println(response.toString());
			} 
			catch (IOException e) 
			{
				System.out.println("Could not get a the response from the server!");
				e.printStackTrace();
			}
			catch (Exception e) 
			{
				System.out.println("Could not get a the response from the server!");
				e.printStackTrace();
			}
			
			// forward the reply from the server
			try 
			{
				response.sendToBrowser(browserToProxyConn);
			} 
			catch (IOException e) 
			{
				System.out.println("Could not send the response to browser!");
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Could not handel the request!");
		}
	}

	/* This message checks and opens a TCP connection to the host server
	 * if the connection is open and is in the hash table already, it reuses that connection.
	 * if not, it opens a new connection and puts it in the hash table.
	 * 
	 * It returns a TCP connection the open TCP connection.
	 */
	private Socket checkTCPConnection() throws UnknownHostException, IOException
	{
		Socket toWebServer; /* hosts.get(request.getHostName());
		
		if ((toWebServer != null))
		{
			if (!toWebServer.isClosed())
			{
				System.out.println("TCP connection reused.");
				return toWebServer;
			}
		}
		// If it couldn't get the web server TCP connection from the hash table
		System.out.println("The TCP connection is new or was closed!");
	
		hosts.put(request.getHostName(), toWebServer);
		*/
		return 	toWebServer = new Socket(request.getHostName(), request.getPort());
		
		
	}
}
