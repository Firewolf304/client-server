import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class movedLabel extends JLabel implements Runnable, java.io.Serializable {
    public int  Xval = 1, Yval = 1;
    public int x = 0, y = 0;
    public transient Thread move;
    private JPanel mainPanel;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    transient Mutex mutex;
    /*@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Random random = new Random();
        int numVertices = random.nextInt(12) + 3;
        int[] xPoints = new int[numVertices];
        int[] yPoints = new int[numVertices];
        for (int i = 0; i < numVertices; i++) {
            xPoints[i] = random.nextInt(getWidth());
            yPoints[i] = random.nextInt(getHeight());
        }
        g.setColor(Color.BLUE);
        g.fillPolygon(xPoints, yPoints, numVertices);
    }*/

    public movedLabel() {
        super();
        this.addMouseListener(new mouseDetect());
        this.setDoubleBuffered(true);
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
    }
    public movedLabel(JPanel panel) {
        super();
        this.addMouseListener(new mouseDetect());
        this.setDoubleBuffered(true);
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
        this.mainPanel = panel;
    }
    public movedLabel(JPanel panel, int xPose, int yPose) {
        super();
        this.addMouseListener(new mouseDetect());
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
        this.mainPanel = panel;
        this.x = xPose; this.y = yPose;
        setBounds(this.x, this.y, 50, 50);
    }

    public void startThread() {
        mutex = new Mutex(false);
        isRunning.set(true);
        move = new Thread(this);
        move.start();
        System.out.println("Started " + Arrays.asList( mainPanel.getComponents() ).indexOf(this));
    }
    public void stop() {
        move.interrupt();
        isRunning.set(false);
    }
    public void pause() {

    }
    public boolean getIsRunning() {
        return isRunning.get();
    }

    @Override
    public void run() {
        Random rnd = new Random();
        this.setLocation(x,y);
        while (isRunning.get()) {
            mutex.step(); // pause method
            Xval = rnd.nextInt(21) - 10;
            Yval = rnd.nextInt(21) - 10;

            if (( x + Xval + getWidth() > mainPanel.getWidth() && x + Xval + getWidth() > x + (-Xval) + getWidth() ) || x + Xval < 0) {
                Xval *= -1;
            }
            if (( y + Yval + getHeight() > mainPanel.getHeight() && y + Yval + getHeight() > y + (-Yval) + getHeight()) || y + Yval < 0) {
                Yval *= -1;
            }
            x += Xval;
            y += Yval;
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

    class mouseDetect implements MouseListener{

        @Override
        public void mouseClicked(MouseEvent mouseEvent) {
            if(mouseEvent.getButton() == MouseEvent.BUTTON3) {
                var list = Arrays.asList(mainPanel.getComponents());
                System.out.println("Removed " + list.indexOf(mouseEvent.getSource()));
                stop();
                ((movedLabel)mouseEvent.getSource()).getParent().repaint();
                ((movedLabel)mouseEvent.getSource()).getParent().remove(list.indexOf(mouseEvent.getSource()));
            }
        }

        @Override
        public void mousePressed(MouseEvent mouseEvent) {

        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent) {

        }

        @Override
        public void mouseEntered(MouseEvent mouseEvent) {

        }

        @Override
        public void mouseExited(MouseEvent mouseEvent) {

        }
    }
    public class Mutex {
        private final AtomicBoolean lock;
        private final Object mutex;

        public Mutex(boolean lock) {
            this.lock = new AtomicBoolean(lock);
            this.mutex = new Object();
        }

        public void step() {
            if (lock.get()) synchronized(mutex) {
                try {
                    mutex.wait();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        public void lock() {
            lock.set(true);
        }

        public void unlock() {
            lock.set(false);

            synchronized(mutex) {
                mutex.notify();
            }
        }
    }
}
