package MultiThreadedServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

    public Runnable getRunnable() {
        return () -> {
            int port = 8010;
            try {
                InetAddress address = InetAddress.getByName("localhost");
                Socket socket = new Socket(address, port);
                try (
                        PrintWriter toSocket = new PrintWriter(socket.getOutputStream(), true);
                        BufferedReader fromSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()))
                ) {
                    toSocket.println("Hello from Client " + socket.getLocalSocketAddress());
                    String line = fromSocket.readLine();
                    System.out.println("Response from Server " + line);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // socket auto-closes because of try-with-resources
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    public static void main(String[] args){
        Client client = new Client();
        for(int i=0; i<100; i++){
            try{
                Thread thread = new Thread(client.getRunnable());
                thread.start();
            }catch(Exception ex){
                return;
            }
        }
    }
}
// this is simple , just create 100 threads for each request sent to the server nd rrn them all at once , each thread sends request to the server , each threds uses lamda expression to implement the runnable interface and that is passed in the thread constuctor