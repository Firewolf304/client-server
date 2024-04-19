import java.beans.XMLEncoder;
import java.io.*;
import java.net.InetAddress;
import java.net.*;
import javax.swing.*;
import java.awt.*;

public class Main {
    private static ServerSocket server;
    private static BufferedReader in;
    private static BufferedWriter out;
    public static void main(String[] args) throws Exception {
        System.out.println("Server init");
        InetAddress address = InetAddress.getByName("127.0.0.1");
        server = new ServerSocket(8080, 0, address);
        System.out.println("Server started by " + address.getHostName() + ":" + server.getLocalPort());

        var window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setTitle("Hello world!");

        //this.pack();

        //window.setSize(500,500);
        var frameWindow = new panel();
        frameWindow.setPreferredSize(new Dimension( 500, 500));
        window.add(frameWindow);


        window.pack();
        window.setResizable(false);
        window.setLayout(null);
        window.setIgnoreRepaint(false);
        window.setVisible(true);

        while(true) {
            Socket client = server.accept();
            try {
                ByteArrayOutputStream xmlOut = new ByteArrayOutputStream();
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

                System.out.println("Client: " + in.readLine());
                XMLEncoder encoder = new XMLEncoder(xmlOut);
                recursiveEncoder(frameWindow, encoder);
                encoder.close();
                out.write(xmlOut.toString().replace('\n', ' ').trim());
                out.newLine();
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

    public static void recursiveEncoder(Component component, XMLEncoder enc) {
        var name = component.getClass().getName();
        if(name == "movedLabel")
            enc.writeObject(component);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                if (child instanceof JComponent) {
                    recursiveEncoder(child, enc);
                }
            }
        }
    }
    public static void recursiveEncoder(Component[] component, XMLEncoder enc) {
        for (Component child : component) {
            if (child instanceof JComponent) {
                recursiveEncoder(child, enc);
            }
        }
    }
}