import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.XMLDecoder;
import java.io.*;
import java.net.*;

public class Main {
    public static void main(String[] args) throws Exception {
        InetAddress address = InetAddress.getByName("127.0.0.1");
        Socket me = new Socket(address, 8080);
        System.out.println("Connected to " + address.getHostName() + ":" + me.getPort());
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(me.getOutputStream()));
        BufferedReader input = new BufferedReader(new InputStreamReader(me.getInputStream()));
        var window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setTitle("Hello world!");

        //this.pack();

        //window.setSize(500,500);

        try {
            output.write("Silence, nigga");
            output.newLine();
            output.flush();
            String value = input.readLine();
            System.out.println("Server: " + value);
            ByteArrayInputStream xmlIn = new ByteArrayInputStream(value.getBytes());
            XMLDecoder decoder = new XMLDecoder(xmlIn);
            var frameWindow = new panel(500, 500);
            movedLabel label;
            try {
                while ((label = (movedLabel) decoder.readObject()) != null) {
                    label = new movedLabel(frameWindow, label.getText(), label.x, label.y);
                    frameWindow.add(label);
                    label.startThread();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            var pauseButton = new JButton("Pause");
            pauseButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e)  {
                    super.mouseClicked(e);
                    if(!frameWindow.paused) {
                        frameWindow.pauseComponentThreads();
                        frameWindow.paused = !frameWindow.paused;
                    } else {
                        frameWindow.resumeComponentThreads();
                        frameWindow.paused = !frameWindow.paused;
                    }
                }
            });
            pauseButton.setFont(new Font("Arial", Font.BOLD, 12));
            pauseButton.setPreferredSize(new Dimension(100, 50));
            pauseButton.setSize(new Dimension(100, 50));
            frameWindow.add(pauseButton, BorderLayout.WEST);


            window.add(frameWindow);
            window.pack();
            window.setResizable(false);
            window.setLayout(null);
            window.setIgnoreRepaint(false);
            window.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            me.close();
            output.close();
        }


    }
}