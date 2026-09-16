package SingleThreadedServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {


    public void run() throws IOException {
        int port = 8010; // this to uniquely identify which server is begin used from the serverside , server side can contain many servers like web servers port no 80 , db servers like postgresql server etc
        ServerSocket socket = new ServerSocket(port); // serversocket is only for listening to connections(channel) , when actual connection established , the data transfer happens in through socket object
        //socket.setSoTimeout(1000); // if no request come withing 1 second then breakout of the loop and throw execption
        while(true){
            try{
                System.out.println("SingleThreadedServer.Server is listening on port " + port);
                Socket acceptedConnections = socket.accept();
                System.out.println("Connect accepted from client " + acceptedConnections.getRemoteSocketAddress());
                PrintWriter toClient = new PrintWriter(acceptedConnections.getOutputStream()); // this just to print to the client , output input streams just act like pipelines so send receive bytes , this does the actual work of formatting and stuff
                BufferedReader fromClient = new BufferedReader(new InputStreamReader( acceptedConnections.getInputStream()));
                // input stream just bytes , inputstream reader converts it to characters , buffer does the readline formating etc , whereas in printing printwriter does both the jobs
                toClient.println("Hello from the server");
                toClient.flush();
                fromClient.close();
                toClient.close();
                acceptedConnections.close();
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        try{
            Server server = new Server();
            server.run();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
