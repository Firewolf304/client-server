import javax.swing.*;
import java.awt.*;
import java.beans.XMLDecoder;
import java.io.*;


public class Main implements java.io.Serializable{
    private static panel frameWindow;
    private static void runSave() {
        try {
            var file = new FileInputStream("save.bin");
            var stream = new ObjectInputStream(file);
            movedLabel label;
            while ((label = (movedLabel)stream.readObject()) != null) {
                var temp = new movedLabel(frameWindow, label.getText(), label.x, label.y);
                frameWindow.add(temp);
                temp.startThread();
            }
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            var file = new FileInputStream("save.xml");
            XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(file));
            movedLabel label;
            while ((label = (movedLabel) decoder.readObject()) != null) {
                var temp = new movedLabel(frameWindow, label.getText(), label.x, label.y);
                frameWindow.add(temp);
                temp.startThread();
            }
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                var window = new JFrame();
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                window.setTitle("Hello world!");

                //this.pack();

                //window.setSize(500,500);
                frameWindow = new panel(500, 500);
                runSave();
                window.add(frameWindow);


                window.pack();
                window.setResizable(false);
                window.setLayout(null);
                window.setIgnoreRepaint(false);
                window.setVisible(true);
                //frameWindow.startThread();
            }
        });

    }
}