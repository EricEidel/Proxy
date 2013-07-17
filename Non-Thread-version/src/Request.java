import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;


public class Request 
{
	static final String CRLF = "\r\n";
	
	private String httpMethod; // this indicates which httpMethod this request represents
	
	private ArrayList<String> headers;
	private ArrayList<String> body;
	
	private String hostName;
	private int port;
	private Socket browserSocket;
	private InputStream inFromBrowserStream;
	//private OutputStream outToBrowserStream;
	
	private BufferedReader inFromBrowserBufferedReader;
	
	public Request(Socket browserSocket) throws Exception
	{
		this.headers = new ArrayList<String>();
		this.setBody(new ArrayList<String>());
		this.setHostName("");
		this.setPort(80);
		this.setBrowserSocket(browserSocket);
		
		this.inFromBrowserStream = browserSocket.getInputStream();
		//this.outToBrowserStream = browserSocket.getOutputStream();
		
		this.inFromBrowserBufferedReader = new BufferedReader(new InputStreamReader(inFromBrowserStream));
		
		initParsing();
		
		System.out.println("HOST: " + getHostName());
		System.out.println("PORT: " + getPort());
	}

	/* This method starts to parse the request - it only reads up to the first empty line, where the headers of the request end.
	 * It has to be synchronized, since it reads from the input stream.
	 * It will populate the headers with headers.
	 * This version ignores the body of the request as get requests have no body.
	 * (I left it in the design so that we can expend to post and other requests in the future).
	 */
	public synchronized void initParsing() throws Exception
	{
		String line = inFromBrowserBufferedReader.readLine();
		parseFirstLine(line);
		System.out.println("First line:");
		System.out.println(line);
		while (!line.equals(""))
		{
			headers.add(line+CRLF);
			
			if (line.contains("Host:"))
			{
				String copy = new String(line);
				setHostName(copy.replace("Host: ", ""));
			}
			
			line = inFromBrowserBufferedReader.readLine();
		}
		headers.add(CRLF);
	}

	// This parses the first line to check the type of the HTTP request.
	private void parseFirstLine(String line) throws UnsupportedHttpMethodException
	{
		// 7 different commands that can be in first line: GET, POST, HEAD, OPTIONS, PUT, DELETE, TRACE and CONNECT

		String[] strArr = line.split(" ");
		this.httpMethod = strArr[0];
		
		if (httpMethod.equalsIgnoreCase("GET"))
		{
			this.setHostName(strArr[1].replace("http://", ""));
			this.setPort(80);
		}
		else // for now throw an exception that will be handled by the engine to send a 404.
		{
			throw new UnsupportedHttpMethodException();
		}
	}

	/*
	 * returns the request in a nicely formated String.
	 */
	
	public String toString()
	{
		System.out.println("\nPrinting out the request:");
		String str = "";
		
		for (String s: headers)
		{
			str += s;
		}
		
		return str;
	}
	
	/*
	 * This method forwards the request to the TCP connection toWebServer
	 */
	public synchronized void sendRequestToWebServer(Socket toWebServer) throws IOException 
	{
		OutputStreamWriter proxyToWebserverWriter = new OutputStreamWriter(new DataOutputStream(toWebServer.getOutputStream()));
		for (String s: headers)
		{
			proxyToWebserverWriter.append(s);
			System.out.print(s);
		}	
		proxyToWebserverWriter.flush();		
	}
	
	
	// ======== GETTERS AND SETTERS FROM HERE ON ===================
	
	public Socket getBrowserSocket() {
		return browserSocket;
	}

	public void setBrowserSocket(Socket browserSocket) {
		this.browserSocket = browserSocket;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public ArrayList<String> getBody() {
		return body;
	}

	public void setBody(ArrayList<String> body) {
		this.body = body;
	}
	
	public ArrayList<String> getHeaders() {
		return headers;
	}

	public void setHeaders(ArrayList<String> headers) {
		this.headers = headers;
	}
}
