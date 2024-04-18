import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private static ServerSocket server;
    private static BufferedReader in;
    private static BufferedWriter out;
    public static void main(String[] args) throws Exception {
        System.out.println("Server init");
        InetAddress address = InetAddress.getByName("127.0.0.1");
        server = new ServerSocket(8080, 0, address);
        System.out.println("Server started by " + address.getHostName() + ":" + server.getLocalPort());
        while(true) {
            Socket client = server.accept();
            try {
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

                System.out.println("Client: " + in.readLine());
                out.write("Goodbye, nigga");
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                client.close();
                in.close();
                out.close();
            }
        }

    }
}