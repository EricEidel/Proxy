import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;


public class Response 
{
	public static final String CRLF = "\r\n";

	private Request request;
	private ArrayList<String> headers;
	private ArrayList<Byte> body;
	private Socket fromWebServer;
	
	private BufferedReader inFromServerBufferedReader;
	
	public Response(Request request, Socket fromWebServer) throws IOException
	{
		this.headers = new ArrayList<String>();
		this.body = new ArrayList<Byte>();
		this.fromWebServer = fromWebServer;
		
		inFromServerBufferedReader = new BufferedReader(new InputStreamReader(fromWebServer.getInputStream()));
	}
	
	/*
	 * This method gets the response from the server. 
	 * It changes the ArrayLists body and headers to be the body and the headers of the response from the server.
	 * This method also changes the mode field, dictating how the body will be read from the server.
	 */
	public void getFromServer() throws Exception
	{
		getHeadersFromServer();
		getBodyFromServer();		
	
	}

	/* This method gets the headers from the server
	 * returns an ArrayList of type String with the headers as plain text.
	 * This method also sets the mode - chunked or content length - by which the body will be read.
	 * The CRLF is appended to this list.
	 */
	private void getHeadersFromServer() throws IOException 
	{
		String line = inFromServerBufferedReader.readLine();
		System.out.println("Headers from server:");
		while (!line.equals(""))
		{
			this.headers.add(line+CRLF);	
			System.out.print(line+CRLF);		
			line = inFromServerBufferedReader.readLine();
			
		}		
		this.headers.add(CRLF);
		System.out.println();

	}

	/*
	 * This methods gets the body of an HTTP response.
	 * It checks to see what is the this.mode - if it's 1, it uses the content length method to recieve the body.
	 * If it's 2, it uses the chunked method to recieve the body.
	 */
	private void getBodyFromServer() throws Exception 
	{		 
		// This method should check either the mode is chunked or not and get body in two different ways.
		// This has been an older version now, 
		// so we just implemented a simple "read all bytes to end of stream" until we fix this to work.
		
		
		// Reader from the server-proxy.
        BufferedInputStream myReader =  new BufferedInputStream(fromWebServer.getInputStream(),1024);
		
        System.out.println("From getBodyBytes");
        
		int b; // the byte read from the file
        while ((b = myReader.read()) != -1) 
        {
        	System.out.print(b);
    		body.add(new Byte((byte)b));
    		//System.out.print((char)b);
        }
	}
	/*
	 * This method sends the response from the server back to the browser.
	 */
	public void sendToBrowser(Socket toBrowser) throws IOException 
	{
		BufferedWriter headerWriter = new BufferedWriter(new OutputStreamWriter(toBrowser.getOutputStream()));
		BufferedOutputStream bodyWriter =  new BufferedOutputStream(toBrowser.getOutputStream());	
		
		System.out.println("This is the response sent back to the browser:");
		for (String s: headers)
		{
			headerWriter.write(s);
			System.out.print(s);
		}
		
		headerWriter.flush();
		
		for (Byte b: body)
		{
			bodyWriter.write(Byte.valueOf(b));
			System.out.print(b.toString());
		}
		
		bodyWriter.flush();
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