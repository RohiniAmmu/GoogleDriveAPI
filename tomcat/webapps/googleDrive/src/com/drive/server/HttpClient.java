package com.drive.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.net.InetSocketAddress;

public class HttpClient {

    private static Random random = new Random();
    private String url;
    private HttpURLConnection connection = null;
    private OutputStream os = null;
    private String boundary = "---------------------------" + randomString() + randomString() + randomString();
    private boolean multipart = false;
    private boolean post = false;
    private boolean put = false;

    public HttpClient(String url) throws IOException {
        setURL(url);
    }

    public String getURL () {
        return url;
    }

    public void setURL(String url) throws IOException {
	   setURL(url,30000);//30 sec
    }

    public void setURL(String url, int timeout) throws IOException {
        this.url = url;
        URL urlo = new URL(url);
        connection = (HttpURLConnection) urlo.openConnection();
        connection.setConnectTimeout(timeout); 
        connection.setUseCaches(false);
    }

    public void setDoOutput(boolean isTrue){
        connection.setDoOutput(isTrue);
    }

    public static String randomString() {
        return Long.toString(random.nextLong(), 36);
    }

    private void boundary() throws IOException {
        write("--"); 
        write(boundary);
    }

    public void connect() throws IOException {
        if (os == null) {
            os = connection.getOutputStream();
        }
    }

    public void write(char c) throws IOException {
        connect();
        os.write(c);
    }

    public void write(String s) throws IOException {
        connect();
        os.write(s.getBytes());
    }

    public void writeln(String s) throws IOException {
        write(s);
        nl();
    }

    public void nl() throws IOException {
        write("\r\n"); 
    }

    public void setReadTimeout(int timeout){
        connection.setReadTimeout(timeout);
    }    

    public void doMultiPart() throws IOException {
        multipart = true;
        post = true;
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connect();
    }

    public void doDelete() throws IOException {
        connection.setRequestMethod("DELETE");
        connection.setDoOutput(true);
        connect();
    }
    
    public void doPost() throws IOException {
        multipart = false;
        post = true;
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connect();
    }

    public void doPost(String contentType) throws IOException {
        multipart = false;
        post = true;
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Content-Type", contentType);
        connect();
    }

    public void doPut() throws IOException {
        multipart = false;
        put = true;
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connect();
    }

    public void doPut(String contentType) throws IOException {
        multipart = false;
        put = true;
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Content-Type", contentType);
        connect();
    }

    public InputStream doGet()throws IOException{
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.connect();
        return connection.getInputStream();
    }

    private void writeName(String name) throws IOException {
        nl();
        write("Content-Disposition: form-data; name=\""); 
        write(name);
        write('"'); 
    }

    public void setHeader(String header, String value) {
        connection.setRequestProperty(header, value);
    }

    public static long pipe(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[500000];
        int nread;
        long total = 0;
        synchronized (in) {
            while ((nread = in.read(buf, 0, buf.length)) >= 0) {
                out.write(buf, 0, nread);
                total += nread;
            }
        }
        out.flush();
        return total;
    }

    public void setParameter(String name, String value) throws IOException {
        if (multipart) {
            boundary();
            writeName(name);
            nl();
            nl();
            writeln(value);
        } else if (post || put) {
            write("&" + name + "=" + value);
        }
    }

    public void setParameter(String name, String filename, String transferEncoding, InputStream is) throws IOException {
        boundary();
        writeName(name);
        write("; filename=\""); 
        write(filename);
        write('"');
        nl();
        write("Content-Type: "); 
        String type = connection.guessContentTypeFromName(filename);
        if (type == null) {
            type = "application/octet-stream"; 
        }
        writeln(type);
        nl();
        if (transferEncoding != null) {
            write("Content-Transfer-Encoding: "); 
            writeln(transferEncoding);
            nl();
        }
        pipe(is, os);
        nl();
    }

    public InputStream getInputStream() throws IOException {
        return connection.getInputStream();
    }
    
    public OutputStream getOutputStream() throws IOException {
        return connection.getOutputStream();
    }

    public InputStream post() throws IOException {
        if (multipart) {
            boundary();
            writeln("--");
        }
        if (post) {
            os.close();
        }
        connection.connect();
        return connection.getInputStream();
    }

    public InputStream put() throws IOException {
        if (multipart) {
            boundary();
            writeln("--");
        }
        if (put) {
            os.close();
        }
        connection.connect();
        return connection.getInputStream();
    }
    
    public InputStream delete() throws IOException {
        connection.connect();
        return connection.getInputStream();
    }

    public InputStream get() throws IOException {
        if (multipart || post) {
            return post();
        }
        connection.connect();
        return connection.getInputStream();
    }

    public InputStream getErrorStream() {
        return connection.getErrorStream();
    }

    public int getResponseCode() throws IOException {
        return connection.getResponseCode();
    }

    public void disConnect() {
        if(connection != null){
            connection.disconnect();
        }
    }

    public String getErrorMessage() throws IOException{
        String errorContent ="";
        String line ="";
        BufferedReader in   = new BufferedReader(new InputStreamReader( getErrorStream() ));
        while (( line = in.readLine() ) != null ) {
            errorContent += line;
        }
        return errorContent;
    }

    public StringBuilder getSuccessResponse()throws IOException{
        String line ="";
        StringBuilder httpResponse = new StringBuilder();
        BufferedReader in = new BufferedReader( new InputStreamReader( getInputStream() ) );
        while (( line = in.readLine() ) != null) {
            httpResponse.append( line + System.getProperty( "line.separator" ) );
        }
        return httpResponse;
    }
}
