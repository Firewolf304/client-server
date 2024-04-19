import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class panel extends JPanel implements java.io.Serializable {
    public ArrayList<movedLabel> massive;
    //Thread panelMovement;
    JPanel mainPanel = this;
    boolean paused = false;
    public panel(int sizeX, int sizeY) {
        //this.setSize(sizeX, sizeY);
        super(new FlowLayout(FlowLayout.LEFT));
        //setLayout(null);
        this.setPreferredSize(new Dimension( sizeX, sizeY));
        this.setDoubleBuffered(true);
        this.massive = new ArrayList<>();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("Click in " + e.getX() + ":" + e.getY());
                if(e.getButton() == MouseEvent.BUTTON1 && !paused) {
                    var label = new movedLabel(mainPanel, "3123123", e.getX(), e.getY());
                    add(label);
                    label.startThread();
                    repaint();
                } else if(e.getButton() == MouseEvent.BUTTON3) { // заглушка
                    System.out.println( "Found: " + e.getComponent().getName() );
                }
            }
        });
        var pauseButton = new JButton("Pause");
        pauseButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                //var panelComps = mainPanel.getComponents();
                // ((movedLabel) panelComps[1]).mutex.lock();
                if(!paused) {
                    pauseComponentThreads();
                    paused = !paused;
                } else {
                    resumeComponentThreads();
                    paused = !paused;
                }
            }
        });
        pauseButton.setFont(new Font("Arial", Font.BOLD, 12));
        pauseButton.setPreferredSize(new Dimension(100, 50));
        pauseButton.setSize(new Dimension(100, 50));
        add(pauseButton, BorderLayout.WEST);

        var save = new JButton("Save");
        save.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                try {
                    XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream("save.xml")));
                    recursiveEncoder(mainPanel.getComponents(), encoder);
                    encoder.close();
                    System.out.println("Saved");
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        });
        save.setFont(new Font("Arial", Font.BOLD, 12));
        save.setPreferredSize(new Dimension(100, 50));
        save.setSize(new Dimension(100, 50));
        save.setLayout(null);
        add(save);



    }
    public void pauseComponentThreads() {
        var list = Arrays.asList(mainPanel.getComponents());
        list.forEach((component) -> {
            if(component.getClass().getName() == "movedLabel") {
                System.out.println("Locked: " + component.getClass().getName() + Arrays.asList( mainPanel.getComponents() ).indexOf(component));
                ((movedLabel)component).mutex.lock();
            }
        } );
    }
    public void resumeComponentThreads() {
        var list = Arrays.asList(mainPanel.getComponents());
        list.forEach((component) -> {
            if(component.getClass().getName() == "movedLabel") {
                System.out.println("Unlocked: " + component.getClass().getName() + Arrays.asList( mainPanel.getComponents() ).indexOf(component));
                ((movedLabel)component).mutex.unlock();
            }
        } );
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


    /*public void startThread() {
        this.panelMovement = new Thread(this);
        this.panelMovement.start();
    }


    @Override
    public void run() {

        // label
        var label = new movedLabel(this,"123123123");
        this.add(label);

        // cycle
        label.startThread();

    }
    @Override
    public void run() {
        this.massive = new ArrayList<>();

        // label
        int Xval = 1;
        int Yval = 1;
        int x = 0, y = 0;
        JLabel label = new JLabel("123");
        label.setVerticalTextPosition(JLabel.TOP);
        //this.label.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
        label.setDoubleBuffered(true);
        label.setVerticalAlignment(JLabel.CENTER);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
        label.setSize(10,10);
        this.add(label);

        // cycle
        int FPS = 60;
        double drawInterval  = 1 /FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        while(true) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;
            if(delta >= 1) {
                // update
                if(x + Xval + label.getWidth() > this.getWidth() || x + Xval < 0) {
                    Xval *= -1;
                }
                if(y + Yval + label.getHeight() > this.getHeight() || y + Yval < 0) {
                    Yval *= -1;
                }
                x+= Xval; y+=Yval;
                label.setLocation(x,y);

                // repaint
                label.repaint();
                delta--;
            }

        }
    }*/
}
