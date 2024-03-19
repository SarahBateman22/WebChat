import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.Scanner;

public class WebServer {
    public static void main(String[] args) throws IOException {

        RoomManager roomManager_=new RoomManager();

        ServerSocket server = new ServerSocket(8080);

        System.out.println("Server is listening...");

        while (true) {
            Socket client = server.accept();

            Thread thread = new Thread(new MyRunnable(client, roomManager_));

            thread.start();

        }
    }
}