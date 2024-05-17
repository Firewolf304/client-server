import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class socket {

    public ServerSocket server;
    String address = "127.0.0.1";
    int port = 8080;
    public ArrayList<Objector> threadList;
    private ArrayList<pair<Integer,Integer>> queue = new ArrayList<>();
    public socket(String address, int port) throws Exception {
        port = port;
        address = address;
        server = new ServerSocket(port, 0, InetAddress.getByName(address));
        threadList = new ArrayList<Objector>();
        while(true) {
            Boolean added = false;
            try {
                Socket message = server.accept();

                System.out.println("New client: " + message.getInetAddress().getHostAddress() + ":" + message.getPort());
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
                        var out = new BufferedWriter(new OutputStreamWriter(obj.connection.getOutputStream()));
                        send(out, "size " + String.valueOf(threadList.size()));
                    }
                }
            } catch (IOException e) {
                if(added) {
                    threadList.removeLast();
                }
            }
        }
    }
    MyRunnable RunThread = (id ) -> {
        var object = threadList.get(id);
        var in = new BufferedReader(new InputStreamReader(object.connection.getInputStream()));
        var out = new BufferedWriter(new OutputStreamWriter(object.connection.getOutputStream()));
        while(object.running.get()) {
            try {
                String[] line = in.readLine().split(" ",2 );
                System.out.println("Request by " + object.connection.getPort() + ": " + line[0]);
                switch (line[0]) {
                    case "size" : {
                        System.out.println("It is " + String.valueOf(threadList.size()));
                        send( out, "size " + String.valueOf(threadList.size()) );
                    }
                    case "sync" : {
                        System.out.println("Check client");
                        int request = Integer.valueOf(line[1]);
                        if(request >= 0 && request < threadList.size()) {
                            var sender = threadList.get(request);
                            var senderIn = new BufferedReader(new InputStreamReader(sender.connection.getInputStream()));
                            var senderOut = new BufferedWriter(new OutputStreamWriter(sender.connection.getOutputStream()));
                            if(sender.running.get()) {
                                send(senderOut, "sync");
                                System.out.println("Asked");
                                queue.add(new pair<>(sender.id, threadList.get(id).id));
                            }
                        }
                    }
                    case "get": {
                        for(var i : queue) {
                            if(i.getFirst() == object.id) {
                                var sender = threadList.get(i.getSecond());
                                var senderOut = new BufferedWriter(new OutputStreamWriter(sender.connection.getOutputStream()));
                                send(senderOut, String.join(" ", line)); // redirect
                                System.out.println("Redirected");
                                queue.remove(i);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Exception: " + e.getMessage());
                e.printStackTrace();
                object.running.set(false);
                Thread.currentThread().interrupt();
            }
        }
        /*while (running.get()) {
            try {
                String[] line = in.readLine().split(" ",2 );
                System.out.println("Request: " + line[0]);
                switch (line[0]) {
                    case "sync" : {
                        if(!isServer.get()) {
                            ByteArrayOutputStream xmlOut = new ByteArrayOutputStream();
                            XMLEncoder encoder = new XMLEncoder(xmlOut);
                            recursiveEncoder(mainPanel, encoder);
                            encoder.close();
                            send(out, "get " + xmlOut.toString().replace('\n', ' ').trim());
                        }
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
        }*/

        // add server try
    };

    public void send(BufferedWriter out, String message) throws Exception {
        out.write(message);
        out.newLine();
        out.flush();
    }
    private interface MyRunnable  {
        void run(int id) throws Exception;
    }
    private class Objector {
        Objector(int id, Socket connection, Thread thread) {
            this.id = id;
            this.thread = thread;
            this.running.set(true);
            this.connection = connection;
        }
        public Socket connection;
        public AtomicBoolean running = new AtomicBoolean(false);
        public int id = -1;
        public Thread thread;
    }
}
