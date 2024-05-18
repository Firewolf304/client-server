import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.XMLDecoder;
import java.io.*;
import java.util.Random;


public class Main implements java.io.Serializable{
    private static panel frameWindow;
    private static void runSave() {
        try {
            var file = new FileInputStream("save.bin");
            var stream = new ObjectInputStream(file);
            movedLabel label;
            while ((label = (movedLabel)stream.readObject()) != null) {
                var temp = new movedLabel(frameWindow, label.x, label.y);
                Random random = new Random();
                int numVertices = random.nextInt(30) + 12;
                int[] xPoints = new int[numVertices];
                int[] yPoints = new int[numVertices];
                for (int i = 0; i < numVertices; i++) {
                    xPoints[i] = random.nextInt(300);
                    yPoints[i] = random.nextInt(300);
                }
                temp.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
                temp.setIcon(createRandomPolygonIcon(temp.getWidth(), temp.getHeight(), xPoints, yPoints));
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
                var temp = new movedLabel(frameWindow, label.x, label.y);
                Random random = new Random();
                int numVertices = random.nextInt(30) + 12;
                int[] xPoints = new int[numVertices];
                int[] yPoints = new int[numVertices];
                for (int i = 0; i < numVertices; i++) {
                    xPoints[i] = random.nextInt(300);
                    yPoints[i] = random.nextInt(300);
                }
                temp.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
                temp.setIcon(createRandomPolygonIcon(temp.getWidth()-5, temp.getHeight()-5, xPoints, yPoints));
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
                window.setTitle("Laba1");

                //this.pack();

                //window.setSize(500,500);
                frameWindow = new panel(600, 400);
                runSave();
                window.add(frameWindow, BorderLayout.CENTER);


                window.pack();
                window.setResizable(false);
                window.setLayout(null);
                window.setIgnoreRepaint(false);
                window.setVisible(true);
                //frameWindow.startThread();
            }
        });

    }
    private static ImageIcon createRandomPolygonIcon(int width, int height, int[] xPoints, int[] yPoints) {
        Image image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();

        g2d.setColor(Color.BLUE);
        g2d.fillPolygon(xPoints, yPoints, xPoints.length);
        g2d.dispose();

        return new ImageIcon(image);
    }
}