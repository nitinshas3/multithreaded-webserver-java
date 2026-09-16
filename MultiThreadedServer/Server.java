package MultiThreadedServer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class Server {

    // functional interface to pass as an argument in threads
    Consumer<Socket> socketConsumer = clientSocket -> {
        try (PrintWriter toClient = new PrintWriter(clientSocket.getOutputStream(), true)) {
            toClient.println("Hello from the server");
            // try-with-resources auto-closes PrintWriter
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };



    public static void main(String[] args) {
        int port = 8010;
        try{
            Server server = new Server();
            ServerSocket serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(10000);
            System.out.println("server is listening to " + port);
            while(true){
                Socket acceptedConnections = serverSocket.accept();
                //Thread thread = new Thread(); // when we get a new socket object , create a new thread and execute that socket in that thread by this we can achieve running of several sockets on several threads
                // very simple , just create a lambda function which takes in a socket as input and executes it , here executes it meaning returns response to the user , in real servers accept input and perform some operatinos and return , leave that as of now , so yeah , when we get socket object , just create new thread , and pass the lambda with socket object in new thread and run that new thread , you will be runnign that socket operation in a new thread
                Thread thread = new Thread(()->server.socketConsumer.accept(acceptedConnections));
                thread.start();
            }


        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



}
