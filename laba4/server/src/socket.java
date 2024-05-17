import javax.swing.*;
import javax.swing.text.Document;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

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
                DatagramPacket packet = new DatagramPacket(object.buf, object.buf.length);
                object.connection.receive(packet);
                object.packet = packet;
                String[] line = new String(packet.getData(), packet.getOffset(), packet.getLength()).split(" ", 3);
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
                        System.out.println("Check client");
                        int request = Integer.valueOf(line[1]);
                        if (request > 0 && request < threadList.size()) {
                            var sender = threadList.get(request);
                            if (sender.running.get()) {
                                sender.buf = (String.join(" ", line)).getBytes();
                                send(sender, sender.packet.getAddress(), sender.packet.getPort());
                                System.out.println("Asked");
                                //send(sender, sender.packet.getAddress(), sender.packet.getPort()); // redirect
                                queue.add(new pair<>(sender.id, threadList.get(id).id));
                            }
                        }
                    } break;
                    case "get": {
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
        DatagramPacket		pkt;

        // TO-DO: build the datagram packet and send it to the server
        pkt = new DatagramPacket(data.buf, data.buf.length, destAddr, destPort);
        try {
            data.connection.send(pkt);
        } catch (IOException e) {
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
        public byte[] buf = new byte[5000];
        public DatagramSocket connection;
        public AtomicBoolean running = new AtomicBoolean(false);
        public int id = -1;
        public Thread thread;
        public DatagramPacket packet;
    }
}
