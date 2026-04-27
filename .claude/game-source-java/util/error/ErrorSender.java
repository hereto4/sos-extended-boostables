package util.error;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import snake2d.LOG;

class ErrorSender {



	public static void main(String[] args) throws Exception {

//		get("https://urafitu7e7.execute-api.us-east-2.amazonaws.com/default/Bugs2?TableName=Bugs");
//		
	
		new ErrorSender().send("babababa", "hello there", "dasdafsdfs\n \tdasdas");
		
		//post("https://urafitu7e7.execute-api.us-east-2.amazonaws.com/default/Bugs2");
		
//		sendPOST();
		LOG.ln("POST DONE");
	}
	
	public boolean send(String key, String message, String out) throws Exception{
		URL obj = new URL("https://gamebugs-f058.restdb.io/rest/bugs");
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Accept", "*/*");
		con.setRequestProperty("x-apikey", "60599d5bff8b0c1fbbc28dfb");
		con.setRequestProperty("Content-Type", "application/json");

		String code = "\""+ key + "\"";
		
		{
			message = message.replaceAll("\\t", " ");
			message = message.replaceAll("\\r\\n|\\r|\\n", " ");

			
			message = "\""+ message + "\"";
		}
		
		
		
		{
			out = out.replaceAll("\\t", " ");
			String[] ss = out.split("\\r\\n|\\r|\\n");
			out = "[";
			for (int i = 0; i < ss.length; i++) {
				out += toHex(ss[i]);
				if (i < ss.length-1)
					out += ", ";
			}
			
			out += "]";
		}
		
		
		LOG.ln(code);

		LOG.ln(message);
		

		LOG.ln(out);
		
		String body = "{"
				+ "\"key\": " + code + ","
				+ "\"message\": " + message + ","
				+ "\"dump\": " + out
				+ "}";
		
		LOG.ln();
		LOG.ln(body);
		
		byte[] bs = body.getBytes(StandardCharsets.UTF_8);
		
		con.setRequestProperty( "charset", "utf-8");
		con.setRequestProperty( "Content-Length", Integer.toString( bs.length ));
		
		//con.setRequestProperty("Content-Length", ""+112);
		
		
		con.setDoOutput(true);
		OutputStream os = con.getOutputStream();
		os.write(bs);
		os.flush();
		os.close();
		// For POST only - END

		int responseCode = con.getResponseCode();
		LOG.ln("POST Response Code :: " + responseCode);

		if (responseCode == HttpURLConnection.HTTP_CREATED) { //success
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			LOG.ln(response.toString());
			return true;
		} else {
			LOG.ln("POST request not worked");
			return false;
		}
		
	}
	
	private String toHex(String out) {
		byte[] bs = out.getBytes(StandardCharsets.UTF_8);
		StringBuilder str = new StringBuilder();
	    for(int i = 0; i < bs.length; i++) {
	        str.append(String.format("%x", bs[i]));
	    }
	    out = "\"" + str.toString() + "\"";
	    LOG.ln(out);
	    return out;
	}

}
