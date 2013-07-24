package old;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;

// Try and change the proxy to handle CONNECT and POST requests.
// Play around with buffer size.
// Send a 404 for exceptions?
// adding class variables instead of methods ones.

public class Proxy 
{
	static final String CRLF = "\r\n";
	static String host_name;
	private static int bSize;
	private static Hashtable<String, Socket> hosts;
	Socket proxySocket;
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception
	{
		hosts = new Hashtable<String, Socket>();
		
		// Communication between BROWSER - PROXY
		ServerSocket welcomeSocket = new ServerSocket(2000);
		Socket proxySocket = welcomeSocket.accept();
		
		while (true)
		{
			try
			{
				//check status of proxySocket for new cycle
				if (proxySocket.isClosed())
				{
					System.out.println("\n=== NEW proxySocket ===\n");
					proxySocket= welcomeSocket.accept();
				}
				else
				{
					System.out.println("\n=== RE-USED proxySocket ===\n");
				}
				// initialize
				//count = 0;
				bSize = 16384;
				
				//create reader for requests from browser
				BufferedReader browserToProxyReader = new BufferedReader(new InputStreamReader(proxySocket.getInputStream()));
				
				int port = -1; 	// requested port
				host_name = "";	// requested host
				
				//first line of request header
				String httpRequest = browserToProxyReader.readLine();
		
				if (httpRequest != null)
				{
					// Parse the host name.
					host_name = parseHost(httpRequest);
					port = 80;
					
					System.out.println("=== BROWSER REQUEST ===");
					
					// Gets all the headers as strings and adds them to the list. 
					// NOTE: If host header is in there, it changes host_name to the host header.
					ArrayList<String> list = new ArrayList<String>();
					list = get_message(httpRequest, browserToProxyReader);
					
					// between PROXY - SERVER
					Socket toServer;
					
					// check hosts hash table
					if (hosts.containsKey(host_name))
					{
						System.out.println("=== Old host found ===");
						toServer = hosts.get(host_name);
						if (toServer.isClosed())
						{
							System.out.println("=== Host was closed ===");
							toServer = new Socket(host_name, port);
							hosts.put(host_name,toServer);
						}
					}
					else
					{
						System.out.println("=== New hosts created ===");
						toServer = new Socket(host_name, port);
						hosts.put(host_name,toServer);
					}
					
					
					// writes requests from proxy to server
					OutputStreamWriter proxyToWebserverWriter = new OutputStreamWriter(new DataOutputStream(toServer.getOutputStream()));
					
					System.out.println("=== PROXY TO SERVER ===");
					
					// Forwards the request received from the browser to the web-server
					send_message_to_server(list, proxyToWebserverWriter);
					
					
					// Reads the response from the web-server and sends it back to the browser
					
					// server response reader (server -> proxy)
					InputStream serverResponseReader =  toServer.getInputStream();
					
					// browser response writer (proxy -> browser)
					DataOutputStream proxyToBrowserWriter = new DataOutputStream(proxySocket.getOutputStream());
					
					try {
						System.out.println("=== WAITING FOR SERVER ===");
						
						byte[] buffer = new byte[bSize];
						int bytesRead = serverResponseReader.read(buffer, 0, bSize);
						while (bytesRead != -1)
						{
							proxyToBrowserWriter.write(buffer, 0, bytesRead);
							bytesRead = serverResponseReader.read(buffer, 0, bSize);
						}
						proxyToBrowserWriter.flush();
						
						serverResponseReader.close();
						proxyToBrowserWriter.close();
						
						System.out.println("=== REQUEST COMPLETED ===");
					}
					catch (Exception e)
					{
						System.out.println("=== ERROR ENCOUNTERED ===");
						e.printStackTrace();
						proxyToBrowserWriter.writeBytes("");
					}
					
					System.gc(); // suggest garbage collection
				}
				
				else
				{
					//System.out.println("=== REQUEST NULL, RESET proxySocket ===");
					proxySocket= welcomeSocket.accept();
				}
			}
			
			catch (Exception e)
			{
				System.out.println("=== ERROR, RESET proxySocket === ");
				e.printStackTrace();
				proxySocket= welcomeSocket.accept();
			}
			
		}// end of infinite while loop
	}


	// This method simply sends all the string list has to the web server
	private static void send_message_to_server(ArrayList<String> list, OutputStreamWriter proxyToWebserverWriter) throws Exception
	{
		for (String s: list)
		{
			proxyToWebserverWriter.append(s);
		}
		
		proxyToWebserverWriter.flush();		
	}

	/* This method appends to list all the headers from a request off the browser, print to console. 
	*  If there's a host header, it changes host_name to it's value.
	*/
	private static ArrayList<String> get_message(String httpRequest, BufferedReader browserToProxyReader) throws Exception
	{
		ArrayList<String> list = new ArrayList<String>();
		
		//keep reading from browser until empty line
		while (!httpRequest.equals(""))
		{
			if (httpRequest.contains("Host:"))
				host_name = httpRequest.split(" ")[1];
					
			//if (httpRequest.contains("Accept-Encoding:"))
				//="Accept-Encoding: None";
			
			list.add(httpRequest+CRLF);
			System.out.println(httpRequest);
			
			httpRequest = browserToProxyReader.readLine();
		}
		list.add(CRLF);
		list.add(CRLF);
		
		return list;
	}

	/* This method parses the first request to get the host name. 
	*
	*  Throws Exception if the request is not a get request.
	*/
	private static String parseHost(String httpRequest) throws Exception
	{
		String[] req; // This captures the first line and breaks it down to find the host and the port
		String[] sec_part;	// Second part of parsing the host and port
		String host_name;
		
		// Parse request
		//System.out.println(httpRequest);
		req = httpRequest.split(" ");
		// If it's a get request, parse it as a get request
		if (req[0].equalsIgnoreCase("GET"))
		{
			sec_part = req[1].split("//");
			host_name = sec_part[1];
			if (host_name.contains("/"))
				host_name = host_name.split("/")[0];
			
			//System.out.println(host_name);
			//host_name  = host_name.substring(0, host_name.length()-1);
		}	
		else
		{
			System.out.println("=== NOT GET request === ");
			System.out.println(httpRequest);
			throw (new Exception("None GET request"));
		}	
		return host_name;
	}
}

