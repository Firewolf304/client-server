import javax.swing.*;
import javax.swing.text.Document;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class socket {
    int port = 8080;
    public ArrayList<Objector> threadList;
    private ArrayList<pair<Integer,Integer>> queue = new ArrayList<>();
    public socket(int port) throws Exception {
        port = port;
        threadList = new ArrayList<Objector>();
        while(true) {
            Boolean added = false;
            byte[] buf = new byte[1024];
            try {
                DatagramSocket message = new DatagramSocket(this.port);
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                message.receive(packet);
                System.out.println("New client: " + packet.getAddress().getHostAddress() + ":" + packet.getPort());
                threadList.add(
                        new Objector(
                                threadList.size(),
                                message,
                                new Thread(() -> {
                                    try {
                                        RunThread.run(threadList.size()-1);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                        ));
                added = true;
                threadList.getLast().thread.start();
                for(var obj : this.threadList) {
                    if(obj.running.get()) {
                        System.out.println("Updated id" + obj.id );
                        obj.buf = ("size " + String.valueOf(threadList.size())).getBytes();
                        send(obj, packet.getAddress(), packet.getPort());
                    }
                }
            } catch (IOException e) {
                if(added) {
                    threadList.removeLast();
                }
            }
        }
    }
    public void addThread(DatagramSocket message) {
        threadList.add(
                new Objector(
                        threadList.size(),
                        message,
                        new Thread(() -> {
                            try {
                                RunThread.run(threadList.size()-1);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
                ));
    }
    MyRunnable RunThread = (id ) -> {
        var object = threadList.get(id);
        while(object.running.get()) {

            try {
                object.buf = new byte[5000];
                DatagramPacket packet = new DatagramPacket(object.buf, object.buf.length);
                object.connection.receive(packet);
                object.packet = packet;
                String[] line = new String(packet.getData(), packet.getOffset(), packet.getLength()).split(" ", 2);
                System.out.println("Request from " + object.id + "(" + object.packet.getPort() + "): " + line[0]);
                switch (line[0]) {
                    case "size": {
                        System.out.println("It is " + String.valueOf(threadList.size()));
                        object.buf = ("size " + String.valueOf(threadList.size())).getBytes();
                        send(object, packet.getAddress(), packet.getPort());
                    } break;
                    case "free" : {
                        System.out.println("Free is " + String.valueOf(++this.port));
                        object.buf = (String.valueOf(this.port)).getBytes();
                        send(object, packet.getAddress(), packet.getPort());
                        Thread.sleep(500);
                        System.out.println("New client: " + packet.getAddress().getHostAddress() + ":" + packet.getPort());
                        //this.port++;
                        /*DatagramSocket message;
                        try {
                            message = new DatagramSocket(this.port);
                        } catch (Exception e) {
                            message = new DatagramSocket(this.port+1);
                        }*/
                        addThread(object.connection);
                        System.out.println("Saved to id" + threadList.getLast().id + "=" + packet.getPort() );
                        //threadList.getLast().packet = packet;
                        threadList.getLast().thread.start();
                        for(var i = 0; i < this.threadList.size(); i++) {
                            if(threadList.get(i) .running.get()) {
                                if(threadList.get(i).packet != null) {
                                    System.out.println("Updated id" + threadList.get(i).id + "=" + threadList.get(i).packet.getAddress() + ":" + threadList.get(i).packet.getPort());
                                    threadList.get(i).buf = ("size " + String.valueOf(threadList.size())).getBytes();
                                    send(threadList.get(i), threadList.get(i).packet.getAddress(), threadList.get(i).packet.getPort());
                                }
                            }
                        }
                    } break;
                    case "isonline" : {
                        object.buf = ("already").getBytes();
                        send(object, packet.getAddress(), packet.getPort());
                    } break;
                    case "isonli" : {
                        object.buf = ("already").getBytes();
                        send(object, packet.getAddress(), packet.getPort());
                    } break;
                    case "sync": {
                        //int request = Integer.valueOf(line[1]);
                        var file = new File("save.xml");
                        if(!file.exists()) {
                            file.createNewFile();
                        }
                        var fw = new FileWriter(file.getAbsoluteFile());
                        var bw = new BufferedWriter(fw);
                        bw.write( decompressGzipBase64ToString(line[1]) );
                        bw.close();
                        fw.close();
                        System.out.println("Saved " + line[1].length());

                    } break;
                    case "get": {
                        var file = new FileInputStream("save.xml");
                        String red = readFromInputStream(file);
                        object.buf = ("get " + compressStringToGzipBase64(red)).getBytes();
                        //object.connection.send(new DatagramPacket(object.buf, object.buf.length, object.packet.getAddress(), object.packet.getPort()));
                        send(object, object.packet.getAddress(), object.packet.getPort());
                        System.out.println("Length: " + object.buf.length);
                        /*
                        for (var i : queue) {
                            if (i.getFirst() == Integer.valueOf(line[1])) {
                                var sender = threadList.get(i.getSecond());
                                sender.buf = String.join(" ", line).getBytes();
                                send(sender, sender.packet.getAddress(), sender.packet.getPort()); // redirect

                                System.out.println("Redirected " + i.getFirst() + " to " + i.getSecond());
                                System.out.println(String.join(" ", line));
                                queue.remove(i);
                                break;
                            }
                        }
                        */
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

    public void send(Objector data, InetAddress destAddr, int destPort) throws IOException {
        DatagramPacket		pkt;

        // TO-DO: build the datagram packet and send it to the server
        pkt = new DatagramPacket(data.buf, data.buf.length, destAddr, destPort);
        var receive = new DatagramPacket(data.buf, data.buf.length);
        try {
            data.connection.send(pkt);
        } catch (IOException e) {
            //data.connection.receive(receive);
            System.out.println("Error transmitting packet over network.");
        }
    }
    private interface MyRunnable  {
        void run(int id) throws Exception;
    }
    private class Objector {
        Objector(int id, DatagramSocket connection, Thread thread) {
            this.id = id;
            this.thread = thread;
            this.running.set(true);
            this.connection = connection;
        }
        public byte[] buf = new byte[62000];
        public DatagramSocket connection;
        public AtomicBoolean running = new AtomicBoolean(false);
        public int id = -1;
        public Thread thread;
        public DatagramPacket packet;
    }
    private String readFromInputStream(InputStream inputStream)
            throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }
    public static String decompressGzipBase64ToString(String compressedBase64) throws IOException {
        if (compressedBase64 == null || compressedBase64.length() == 0) {
            return null;
        }
        byte[] compressedBytes = Base64.getDecoder().decode(compressedBase64);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedBytes);
        GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;
        while ((len = gzipInputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, len);
        }
        gzipInputStream.close();
        return byteArrayOutputStream.toString(StandardCharsets.UTF_8.name());
    }
    public static String compressStringToGzipBase64(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        gzipOutputStream.write(str.getBytes(StandardCharsets.UTF_8));
        gzipOutputStream.close();
        byte[] compressedBytes = byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(compressedBytes);
    }
}
