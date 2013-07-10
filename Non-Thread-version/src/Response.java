import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;


public class Response 
{
	public static final String CRLF = "\r\n";
	private static final int CONTENT_LEN = 1;
	private static final int CHUNKED = 2;
	
	private int mode;
	private Request request;
	private ArrayList<String> headers;
	private ArrayList<Byte> body;
	private Socket fromWebServer;
	
	private BufferedReader inFromServerBufferedReader;
	
	public Response(Request request, Socket fromWebServer) throws IOException
	{
		this.headers = new ArrayList<String>();
		this.body = new ArrayList<Byte>();
		
		this.setFromWebServer(fromWebServer);
		
		inFromServerBufferedReader = new BufferedReader(new InputStreamReader(fromWebServer.getInputStream()));
	}
	
	/*
	 * This method gets the response from the server. 
	 * It changes the ArrayLists body and headers to be the body and the headers of the response from the server.
	 * This method also changes the mode field, dictating how the body will be read from the server.
	 */
	public synchronized void getFromServer() throws IOException
	{
		getHeadersFromServer();
		
	}

	/* This method gets the headers from the server
	 * returns an ArrayList of type String with the headers as plain text.
	 * This method also sets the mode - chunked or content length - by which the body will be read.
	 * The CRLF is appended to this list.
	 */
	private void getHeadersFromServer() throws IOException 
	{
		System.out.println("This is from the headers/response");
		String line = inFromServerBufferedReader.readLine();
		System.out.println(line);
		
		while (!line.equals(""))
		{
			this.headers.add(line+CRLF);
			
			if (line.contains("content-length:"))
			{
				this.mode = CONTENT_LEN;
			}
			else
			{
				this.mode = CHUNKED;
			}
			
			line = inFromServerBufferedReader.readLine();
			System.out.println(line);
		}		
		this.headers.add(CRLF);
	}

	/*
	 * This method sends the response from the server back to the browser.
	 */
	public synchronized void sendToBrowser() 
	{
		// TODO Auto-generated method stub
		
	}
	
	/*
	 * This parses the response into a nice string.
	 */
	public String toString()
	{
		System.out.println("\nPrinting out the response:");
		String str = "";
		
		for (String s: headers)
		{
			str += s + "\n";
		}
		
		str += CRLF;
		
		byte[] bytes = new byte[body.size()];
		
		int index = 0;
		for (Byte b: body)
		{
			bytes[index]=b;
			index++;
		}
		
		str += new String(bytes);
		
		return str;
	}
	// =========== GETTERS AND SETTERS FROM HERE ON ==================
	
	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public ArrayList<String> getHeaders() {
		return headers;
	}

	public void setHeaders(ArrayList<String> headers) {
		this.headers = headers;
	}

	public ArrayList<Byte> getBody() {
		return body;
	}

	public void setBody(ArrayList<Byte> body) {
		this.body = body;
	}


	public Socket getFromWebServer() {
		return fromWebServer;
	}


	public void setFromWebServer(Socket fromWebServer) {
		this.fromWebServer = fromWebServer;
	}
}