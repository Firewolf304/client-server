import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

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
        var timer = new Timer(0, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                for(var item : mainPanel.getComponents()) {
                    if(item instanceof movedLabel )
                        item.setLocation( ((movedLabel)item).x, ((movedLabel)item).y);
                }
                repaint();
            }
        });
        timer.start();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("Click in " + e.getX() + ":" + e.getY());
                if(e.getButton() == MouseEvent.BUTTON1 && !paused) {

                    movedLabel label = null;
                    label = new movedLabel(mainPanel, e.getX(), e.getY());
                    Random random = new Random();
                    int numVertices = random.nextInt(30) + 12;
                    int[] xPoints = new int[numVertices];
                    int[] yPoints = new int[numVertices];
                    for (int i = 0; i < numVertices; i++) {
                        xPoints[i] = random.nextInt(getWidth() - 10) + 10;
                        yPoints[i] = random.nextInt(getHeight() - 10) + 10;
                    }
                    label.setIcon(createRandomPolygonIcon(300, 300, xPoints, yPoints));
                    label.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
                    add(label);
                    label.startThread();
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
                    timer.stop();
                    pauseComponentThreads();
                    paused = !paused;
                } else {
                    timer.start();
                    resumeComponentThreads();
                    paused = !paused;
                }
            }
        });
        pauseButton.setFont(new Font("Arial", Font.BOLD, 12));
        pauseButton.setPreferredSize(new Dimension(75, 25));
        pauseButton.setSize(new Dimension(75, 25));
        add(pauseButton, BorderLayout.WEST);

        var save = new JButton("SaveXML");
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
        save.setPreferredSize(new Dimension(75, 25));
        save.setSize(new Dimension(75, 25));
        save.setLayout(null);
        add(save);
        var saveBinar = new JButton("SaveBinary");
        saveBinar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                try {
                    var file = new FileOutputStream("save.bin");
                    var stream = new ObjectOutputStream(file);
                    recursiveObjStream(mainPanel.getComponents(), stream);
                    stream.close();
                    System.out.println("Saved");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        saveBinar.setFont(new Font("Arial", Font.BOLD, 12));
        saveBinar.setPreferredSize(new Dimension(75, 25));
        saveBinar.setSize(new Dimension(75, 25));
        saveBinar.setLayout(null);
        add(saveBinar);



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
    public void recursiveObjStream(Component component, ObjectOutputStream stream) throws IOException {
        var name = component.getClass().getName();
        if(name == "movedLabel")
            stream.writeObject(component);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                if (child instanceof JComponent) {
                    recursiveObjStream(child, stream);
                }
            }
        }
    }
    public void recursiveObjStream(Component[] component, ObjectOutputStream stream) throws IOException {
        for (Component child : component) {
            if (child instanceof JComponent) {
                recursiveObjStream(child, stream);
            }
        }
    }
    private static ImageIcon createRandomPolygonIcon(int width, int height, int[] xPoints, int[] yPoints) {
        Image image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();

        g2d.setColor(Color.BLUE);
        g2d.fillPolygon(xPoints, yPoints, xPoints.length);
        g2d.dispose();

        return new ImageIcon(image);
    }
    private static String imageIconToBase64(ImageIcon icon) {
        Image image = icon.getImage();
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(baos.toByteArray());
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
