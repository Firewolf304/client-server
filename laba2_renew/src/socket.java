import javax.swing.*;
import java.awt.*;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.ArrayList;
import java.util.List;

public class socket {
    private BufferedReader in;
    private BufferedWriter out;

    public ServerSocket server;
    public Socket message;      // двунаправленная хрень (как к серверу, так и обратно)

    String address = "127.0.0.1";
    int port = 8080;
    public JPanel mainPanel;
    private AtomicBoolean running = new AtomicBoolean(false);
    Thread recv = new Thread( () -> {
        running.set(true);
        while (running.get()) {
            try {
                String[] line = in.readLine().split(" ",2 );
                System.out.println("Request: " + line[0]);
                switch (line[0]) {
                    case "sync" : {
                        ByteArrayOutputStream xmlOut = new ByteArrayOutputStream();
                        XMLEncoder encoder = new XMLEncoder(xmlOut);
                        recursiveEncoder(mainPanel, encoder);
                        encoder.close();
                        send("get " + xmlOut.toString().replace('\n', ' ').trim());
                    } break;
                    case "get" : {
                        removeAllComponentsOfType(mainPanel, movedLabel.class);
                        ByteArrayInputStream xmlIn = new ByteArrayInputStream(line[1].getBytes());
                        XMLDecoder decoder = new XMLDecoder(xmlIn);
                        movedLabel label;
                        try {
                            while ((label = (movedLabel) decoder.readObject()) != null) {
                                label = new movedLabel(mainPanel, label.getText(), label.x, label.y);
                                mainPanel.add(label);
                                label.startThread();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } break;
                }
            } catch (Exception e) {
                System.out.println("Exception: " + e.getMessage());
                e.printStackTrace();
                running.set(false);
                Thread.currentThread().interrupt();
            }
        }
        try {
            System.out.println("Trying server");
            this.tryingServer();
        } catch (IOException ex) {
            System.out.println("Cannot start server by client:" + ex.getMessage());
        }
    });
    public void send(String message) throws Exception {
        out.write(message);
        out.newLine();
        out.flush();
    }
    public void tryingServer() throws IOException {
        server = new ServerSocket(port, 0, InetAddress.getByName(address));
        Thread wait = new Thread( () -> {
            while(true) {
                try {
                    message = server.accept();
                    System.out.println("New client: " + message.getInetAddress().getHostAddress() + ":" + message.getPort());
                    in = new BufferedReader(new InputStreamReader(message.getInputStream()));
                    out = new BufferedWriter(new OutputStreamWriter(message.getOutputStream()));
                    if(recv.isInterrupted())
                        recv.run();
                    else
                        recv.start();
                } catch (IOException e) {
                    running.set(false);
                }
            }
        }); wait.start();
    }
    socket(String address, int port, JPanel mainPanel) throws Exception {
        port = port;
        address = address;
        this.mainPanel = mainPanel;
        try {
            System.out.println("Trying client");
            message = new Socket(address, port);
            in = new BufferedReader(new InputStreamReader(message.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(message.getOutputStream()));
            System.out.println("Connected!");
            recv.start();
            this.send("sync ");


            return;
        } catch (Exception exp) { System.out.println("Server dont detect, trying another (" + exp.getMessage() + ")"); }
        try {
            System.out.println("Trying server");
            this.tryingServer();
        } catch (Exception exp) { throw new Exception("No methods sync"); }


    }
    public void recursiveEncoder(Component component, XMLEncoder enc) {
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
    public void recursiveEncoder(Component[] component, XMLEncoder enc) {
        for (Component child : component) {
            if (child instanceof JComponent) {
                recursiveEncoder(child, enc);
            }
        }
    }
    public <T extends Component> void removeAllComponentsOfType(Container container, Class<T> type) {
        Component[] components = container.getComponents();
        List<Component> componentsToRemove = new ArrayList<>();

        for (Component component : components) {
            if (type.isInstance(component)) {
                componentsToRemove.add(component);
            }
        }

        for (Component component : componentsToRemove) {
            container.remove(component);
        }

        container.revalidate();
        container.repaint();
    }
}
