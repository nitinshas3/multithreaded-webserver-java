import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SingleThreaded {


    public void run() throws IOException {
        int port = 8010; // this to uniquely identify which server is begin used from the serverside , server side can contain many servers like web servers port no 80 , db servers like postgresql server etc
        ServerSocket socket = new ServerSocket(port); // serversocket is only for listening to connections(channel) , when actual connection established , the data transfer happens in through socket object
        socket.setSoTimeout(1000); // if no request come withing 1 second then breakout of the loop and throw execption
        while(true){
            try{
                System.out.println("Server is listening on port " + port);
                Socket acceptedConnections = socket.accept();
                System.out.println("Connect accepted from client " + acceptedConnections.getRemoteSocketAddress());
                PrintWriter writer = new PrintWriter(acceptedConnections.getOutputStream());
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {

    }
}
