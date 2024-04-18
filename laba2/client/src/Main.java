import java.io.*;
import java.net.*;

public class Main {
    public static void main(String[] args) throws Exception {
        InetAddress address = InetAddress.getByName("127.0.0.1");
        Socket me = new Socket(address, 8080);
        System.out.println("Connected to " + address.getHostName() + ":" + me.getPort());
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(me.getOutputStream()));
        BufferedReader input = new BufferedReader(new InputStreamReader(me.getInputStream()));
        output.write("Hello, nigga");
        output.newLine();
        output.flush();


        System.out.println("Server: " + input.readLine());
        me.close();
        output.close();



    }
}