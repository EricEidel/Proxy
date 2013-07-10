package old;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

// Try and change the proxy to handle CONNECT and POST requests.
// Play around with buffer size.
// Send a 404 for exceptions?
// adding class variables instead of methods ones.

public class Proxy 
{
	static final String CRLF = "\r\n";
	static String host_name;
	private static int count;
	private static int bSize;
	private static byte[] buffer;
	private static Hashtable<String, Socket> hosts;
	Socket proxySocket;
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception
	{
		//Welcome soccet + accept
		
		hosts = new Hashtable<String, Socket>();

		ServerSocket welcomeSocket = new ServerSocket(2000);
		
		
		// Communication between BROWSER - PROXY
		
		Socket proxySocket = welcomeSocket.accept();
		
		while (true)
		{
			try
				{
					if (proxySocket.isClosed())
					{
						System.out.println("\nNEW WELCOME SOCKET ACCEPT.");
						proxySocket= welcomeSocket.accept();
					}
					else
					{
						System.out.println("\nRE-USED!");
					}
					// initialize
					count = 0;
					bSize = 1000;
					
					BufferedReader browserToProxyReader = new BufferedReader(new InputStreamReader(proxySocket.getInputStream()));
					//DataOutputStream outToBrowser = new DataOutputStream(proxySocket.getOutputStream());
					//OutputStreamWriter proxyToBrowserWriter = new OutputStreamWriter(outToBrowser);
					
					int port = -1; 	// The port the request is going to
					host_name = "";	// The host the request is going to
					
					
					String httpRequest = browserToProxyReader.readLine();
			
					if (httpRequest != null)
					{
						// Parse the host name.
						host_name = parseHost(httpRequest);
						port = 80;
						
						//System.out.println("MESSAGE FROM BROWSER TO PROXY:");
						
						// Gets all the headers as strings and adds them to the list. 
						// NOTE: If host header is in there, it changes host_name to the host header.
						ArrayList<String> list = new ArrayList<String>();
						list = get_message(httpRequest, browserToProxyReader);
						
						// FINISH READING MESSAGE FROM BROWSER TO PROXY
						System.out.println("HOST: " + host_name);
						//System.out.println("PORT: " + port);
						
						Socket toServer;
						
						//This is the channel for PROXY - WEBSERVER communication
						if (hosts.containsKey(host_name))
						{
							System.out.println("Old host used!");
							toServer = hosts.get(host_name);
							if (toServer.isClosed())
							{
								System.out.println("Host was closed!");
								toServer = new Socket(host_name, port);
								hosts.put(host_name,toServer);
							}
						}
						else
						{
							System.out.println("New hosts created!");
							toServer = new Socket(host_name, port);
							hosts.put(host_name,toServer);
						}
						
						
						
						//BufferedReader webserverToProxyReader = new BufferedReader(new InputStreamReader(toServer.getInputStream())); // This is where the server answers the request
						OutputStreamWriter proxyToWebserverWriter = new OutputStreamWriter(new DataOutputStream(toServer.getOutputStream()));
						
						System.out.println("MSG FROM PROXY TO SERVER:");
						
						// Fowards the message recieved from the browser to the web-server
						send_message_to_server(list, proxyToWebserverWriter);
						
						// Reads the response from the web-server and sends it back to the browser
			
				       	buffer = new byte[bSize];
				       	// Writer to the proxy-browser
				        BufferedOutputStream myWriter = new BufferedOutputStream(proxySocket.getOutputStream());
				        // Reader from the server-proxy.
				        BufferedInputStream myReader =  new BufferedInputStream(toServer.getInputStream(),1024);
						//System.out.println("\nFINISHED A CYCLE");
						
						int b; // the byte read from the file
				        while ((b = myReader.read( )) != -1) 
				        {
				    		BufferWrite(b, myWriter);
				    		System.out.print((char)b);
				        }
				        flush(myWriter);
				        
				        myReader.close();
				        myWriter.close();
				        //proxySocket.close();
					}
				}
			catch (Exception e)
				{
					System.out.println("An error has occured!");
					System.out.println(e.getMessage());
				}
		}
	}
	
	private static void flush(BufferedOutputStream myWriter) 
	{
	if(count > 0)
	    {
	        byte[] temp = Arrays.copyOfRange(buffer,0,count);
	        try 
	        {
	        	myWriter.write(temp);
	        } 
	        catch (IOException e) 
	        {
	          System.out.println("Error in writing to file");
	          e.printStackTrace();
	        }
	    }
	}

	private static void BufferWrite(int b, BufferedOutputStream myWriter)
	{
	if(count < bSize)
	{
	    buffer[count] = (byte)b;
	    count++;
	}
	
	if(count == bSize)
	{
	    try 
	    {
	        myWriter.write(buffer);
	    } 
	    catch (IOException e) 
	    {
	        System.out.println("Error in writing to file");
	    }
	    count = 0;
	    for(byte t:buffer)
	        t = 0;
	}
	
	}


	// This method simpley sends all the string list has to the web server
	private static void send_message_to_server(ArrayList<String> list, OutputStreamWriter proxyToWebserverWriter) throws Exception
	{
		for (String s: list)
		{
			proxyToWebserverWriter.append(s);
			System.out.print(s);
		}
		
		proxyToWebserverWriter.flush();		
	}

	/* This method appends to list all the headers from a request off the browser. 
	*  If there's a host header, it changes host_name to it's value.
	*/
	private static ArrayList<String> get_message(String httpRequest, BufferedReader browserToProxyReader) throws Exception
	{
		ArrayList<String> list = new ArrayList<String>();
		
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
		list.add("Connection: Keep-Alive");
		list.add(CRLF);
		System.out.println();
		
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
			System.out.println("This isn't a get request!");
			System.out.println(httpRequest);
			throw (new Exception());
		}	
		return host_name;
	}
}

