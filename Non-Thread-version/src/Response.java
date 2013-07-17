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
	private static final int CONTENT_LEN = 1;
	private int content_len_num = -1;
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
		this.fromWebServer = fromWebServer;
		
		inFromServerBufferedReader = new BufferedReader(new InputStreamReader(fromWebServer.getInputStream()));
	}
	
	/*
	 * This method gets the response from the server. 
	 * It changes the ArrayLists body and headers to be the body and the headers of the response from the server.
	 * This method also changes the mode field, dictating how the body will be read from the server.
	 */
	public synchronized void getFromServer() throws Exception
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
		System.out.println("This is from the headers/response");
		String line = inFromServerBufferedReader.readLine();
		
		while (!line.equals(""))
		{
			this.headers.add(line+CRLF);
			
			if (line.contains("Content-Length:"))
			{
				this.mode = CONTENT_LEN;
				try
				{
					content_len_num = Integer.parseInt(line.split(" ")[1]);
				}
				catch (Exception e)
				{
					System.out.println("Number of content length was not a number!");
				}
				
			}
			else if (line.contains("Chunked:"))
			{
				this.mode = CHUNKED;
			}
			
			line = inFromServerBufferedReader.readLine();
		}		
		this.headers.add(CRLF);
		/*
		for (String s: this.headers)
		{
			System.out.print(s);
		}
		System.out.println(this.mode);
		*/
	}

	/*
	 * This methods gets the body of an HTTP response.
	 * It checks to see what is the this.mode - if it's 1, it uses the content length method to recieve the body.
	 * If it's 2, it uses the chunked method to recieve the body.
	 */
	private void getBodyFromServer() throws Exception 
	{
		if (this.mode == CONTENT_LEN)
			getBodyByContLen();
		else
			getBodyByChunked();
		
	}
	
	/*
	 * Gets the body by the chunked method.
	 */
	private void getBodyByChunked() 
	{
		// TODO Auto-generated method stub	
	}

	/*
	 * Gets the body by the content length.
	 */
	private void getBodyByContLen() throws Exception
	{
		if (content_len_num <= 0)
			System.out.println("ERROR CHECKING THE CONT_LEN_NUM");
		else
		{
			byte[] arrBytes = new byte[content_len_num];
			BufferedInputStream myReader =  new BufferedInputStream(fromWebServer.getInputStream(),content_len_num);
			myReader.read(arrBytes);
			for (byte b: arrBytes)
			{
				body.add(new Byte(b));
			}
		}
		
	}

	/*
	 * This method sends the response from the server back to the browser.
	 */
	public synchronized void sendToBrowser(Socket toBrowser) throws IOException 
	{
		BufferedWriter headerWriter = new BufferedWriter(new OutputStreamWriter(toBrowser.getOutputStream()));
		BufferedOutputStream bodyWriter =  new BufferedOutputStream(toBrowser.getOutputStream());	
		
		for (String s: headers)
			headerWriter.write(s);
		
		headerWriter.flush();
		
		for (Byte b: body)
			bodyWriter.write(Byte.valueOf(b));
		
		bodyWriter.flush();
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
			str += s;
		}
		
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