import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class movedLabel extends JLabel implements Runnable, java.io.Serializable {
    public int  Xval = 1, Yval = 1;
    public int x = 0, y = 0;
    public Thread Movement;
    public JPanel mainPanel;
    private AtomicBoolean running = new AtomicBoolean(false);
    Mutex mutex;

    public movedLabel() {
        super();
        this.addMouseListener(new mouseDetect());
        this.setDoubleBuffered(true);
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
        this.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
    }
    public movedLabel(JPanel panel, String value) {
        super(value);
        this.addMouseListener(new mouseDetect());
        this.setDoubleBuffered(true);
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
        this.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
        this.mainPanel = panel;
    }
    public movedLabel(JPanel panel, String value, int xPose, int yPose) {
        super(value);
        this.addMouseListener(new mouseDetect());
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
        this.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
        this.mainPanel = panel;
        this.x = xPose; this.y = yPose;
        setBounds(this.x, this.y, 50, 50);
    }

    public void startThread() {
        mutex = new Mutex(false);
        this.running.set(true);
        this.Movement = new Thread(this);
        this.Movement.start();

        System.out.println("Started " + Arrays.asList( mainPanel.getComponents() ).indexOf(this));
    }
    public void stop() {
        this.running.set(false);
    }

    public boolean isRunning() {
        return this.running.get();
    }
    @Override
    public void run() {
        Random rnd = new Random();
        this.setLocation(x,y);
        while (this.running.get()) {
            mutex.step(); // pause method
            Xval = rnd.nextInt(21) - 10;
            Yval = rnd.nextInt(21) - 10;

            if (( x + Xval + this.getWidth() > mainPanel.getWidth() && x + Xval + this.getWidth() > x + (-Xval) + this.getWidth() ) || x + Xval < 0) {
                Xval *= -1;
            }
            if (( y + Yval + this.getHeight() > mainPanel.getHeight() && y + Yval + this.getHeight() > y + (-Yval) + this.getHeight()) || y + Yval < 0) {
                Yval *= -1;
            }
            x += Xval;
            y += Yval;
            this.setLocation(x, y);
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // repaint
            this.repaint();

        }
    }

    class mouseDetect implements MouseListener, java.io.Serializable{

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

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
