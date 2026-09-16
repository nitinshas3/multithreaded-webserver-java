package SingleThreadedServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    public void run() throws IOException {
        int port = 8010;
        InetAddress address = InetAddress.getByName("localhost"); // whenever server is running on your system it is always called by local host , so this is just getting the ip address of the name local host , basically this is just doing dns on localhost , by this we will get the ip address of the server we want to cnnect
        System.out.println("Connected to: " + address);
        Socket socket = new Socket(address,port); // remember socket means ip address plus port number , detailed address of the server , this is that
        PrintWriter toSocket = new PrintWriter(socket.getOutputStream());
        BufferedReader fromSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        toSocket.println("Hello from the client");
        toSocket.flush();
        String line = fromSocket.readLine();
        System.out.println("Response from the server is " + line);
        toSocket.close();
        fromSocket.close();
        socket.close();
    }

    public static void main(String[] args) {
        try{
            Client client = new Client();
            client.run();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
