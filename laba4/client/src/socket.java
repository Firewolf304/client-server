import javax.swing.*;
import java.awt.*;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class socket {
    public Objector message;      // двунаправленная хрень (как к серверу, так и обратно)

    String address = "127.0.0.1";
    int port = 8080;
    public JPanel mainPanel;
    private AtomicBoolean running = new AtomicBoolean(false);
    MyRunnable RunThread = ( object ) -> {
        while(object.running.get()) {
            try {
                DatagramPacket 	packet = new DatagramPacket(object.buf, object.buf.length);
                object.connection.receive(packet);
                String[] line = new String(packet.getData(), packet.getOffset(), packet.getLength()).split(" ",3 );
                System.out.println("Request from " + packet.getPort() + ": " + line[0]);
                switch (line[0]) {
                    case "sync" : {
                        ByteArrayOutputStream xmlOut = new ByteArrayOutputStream();
                        XMLEncoder encoder = new XMLEncoder(xmlOut);
                        recursiveEncoder(mainPanel, encoder);
                        encoder.close();
                        object.buf = ("get " + line[1] + " " + Base64.getEncoder().encodeToString(xmlOut.toString().replace('\n', '\0').trim().getBytes())).getBytes();
                        //object.buf = ("get " + xmlOut.toString().replace('\n', ' ').trim()).getBytes();
                        send(object, packet.getAddress(), packet.getPort());
                        System.out.println("Sending data");
                    } break;
                    case "get" : {
                        System.out.println("Getted hash");
                        System.out.println( line[2] );
                        line[2] = new String(Base64.getDecoder().decode(line[2].getBytes()));
                        removeAllComponentsOfType(mainPanel, movedLabel.class);
                        ByteArrayInputStream xmlIn = new ByteArrayInputStream(line[2].getBytes());
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
                    case "size": {
                        Component[] components = mainPanel.getComponents();
                        var size = components.length;
                        for (Component component : components) {
                            if (component instanceof JComboBox) {
                                ((JComboBox<ComboItem>)component).removeAllItems();
                                for(int value = 1; value < Integer.valueOf( line[1] ); value++) {
                                    ((JComboBox<ComboItem>)component).addItem(new ComboItem("Client " + String.valueOf(value), String.valueOf(value)));
                                }
                                ((JComboBox<ComboItem>)component).repaint();
                                break;
                            }
                        }
                    } break;
                    case "already" : {
                        //throw new Exception("Already used");
                    } break;
                }
            } catch (Exception e) {
                System.out.println("Exception: " + e.getMessage());
                e.printStackTrace();
                object.running.set(false);
                Thread.currentThread().interrupt();
            }
        }
        // add server try
    };
    public void send(Objector data, InetAddress destAddr, int destPort) {
        DatagramPacket pkt;

        // TO-DO: build the datagram packet and send it to the server
        pkt = new DatagramPacket(data.buf, data.buf.length, destAddr, destPort);
        try {
            data.connection.send(pkt);
        } catch (IOException e) {
            System.out.println("Error transmitting packet over network.");
        }
    }
    public void send(DatagramSocket data, byte[] buf, InetAddress destAddr, int destPort) {
        DatagramPacket pkt;

        // TO-DO: build the datagram packet and send it to the server
        pkt = new DatagramPacket(buf, buf.length, destAddr, destPort);
        try {
            data.send(pkt);
        } catch (IOException e) {
            System.out.println("Error transmitting packet over network.");
            System.exit(-1);
        }
    }
    socket(String address, int port, JPanel mainPanel) throws Exception {
        port = port;
        address = address;
        this.mainPanel = mainPanel;
        try {
            System.out.println("Trying client");
            do {
                var socket = new DatagramSocket();
                socket.connect(InetAddress.getByName(address), port);
                var buf = "free".getBytes();
                send(socket, buf, InetAddress.getByName(address), port);
                DatagramPacket read = new DatagramPacket(buf, buf.length);
                socket.receive(read);
                try {
                    this.port = Integer.valueOf(new String(read.getData(), read.getOffset(), read.getLength()));

                    socket.connect(InetAddress.getByName(address), port);
                    message = new Objector(0, socket, null);
                    message.packet = read;
                    message.thread = new Thread(() -> {
                        try {
                            RunThread.run(message);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                    message.thread.start();
                    try {
                        if(message.connection.isConnected())
                            return;
                    } catch (Exception e){ }
                    System.out.println("Connected by " + this.port);
                } catch (Exception e) {}
                //this.send("size");


            }
            while(message == null);
            return;
        } catch (Exception exp) {
            System.out.println("No connect");
            exp.printStackTrace();
        }
        throw new Exception("No methods sync");


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
    private interface MyRunnable  {
        void run(Objector id) throws Exception;
    }
    public class Objector {
        Objector(int id, DatagramSocket connection, Thread thread) {
            this.id = id;
            this.thread = thread;
            this.running.set(true);
            this.connection = connection;
        }
        public byte[] buf = new byte[5000];
        public DatagramSocket connection;
        public DatagramPacket packet;
        public AtomicBoolean running = new AtomicBoolean(false);
        public int id = -1;
        public Thread thread;
    }
    public static byte[] compressStringToGzip(String uncompressedString) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(uncompressedString.getBytes());
        }
        return byteArrayOutputStream.toByteArray();
    }
    public static byte[] decompressGzipToString(byte[] compressedBytes) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(compressedBytes))) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipInputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
        }
        return byteArrayOutputStream.toByteArray();
    }
    public static String decompressString(String compressedString) throws IOException {
        byte[] compressedBytes = compressedString.getBytes(StandardCharsets.ISO_8859_1);
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedBytes);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream)) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipInputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, len);
            }

            return new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
        }
    }

}
