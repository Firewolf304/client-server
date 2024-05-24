import netscape.javascript.JSObject;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.json.*;

public class panel extends JPanel implements java.io.Serializable {
    public ArrayList<movedLabel> massive;
    //Thread panelMovement;
    JPanel mainPanel = this;
    boolean paused = false;
    String serverLink = "http://127.0.0.1:8080";
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
        pauseButton.setFont(new Font("Arial", Font.BOLD, 10));
        pauseButton.setPreferredSize(new Dimension(75, 25));
        pauseButton.setSize(new Dimension(75, 25));
        add(pauseButton, BorderLayout.WEST);

        /*var save = new JButton("Save");
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
        var saveBinar = new JButton("SaveBin");
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
        saveBinar.setPreferredSize(new Dimension(100, 50));
        saveBinar.setSize(new Dimension(100, 50));
        saveBinar.setLayout(null);
        add(saveBinar);*/

        var sync = new JButton("Sync");
        sync.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            try {
                var request = new Request(new URI(serverLink + "/save?name="+new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime())+".xml"));
                ByteArrayOutputStream xmlOut = new ByteArrayOutputStream();
                XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(xmlOut));
                recursiveEncoder(mainPanel.getComponents(), encoder);
                encoder.close();
                request.pushBody(compressStringToGzipBase64(xmlOut.toString()));

                var result = send(request);
                System.out.println("Result: " + result.body());
                encoder.close();
                xmlOut.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            }
        });
        sync.setFont(new Font("Arial", Font.BOLD, 12));
        sync.setPreferredSize(new Dimension(100, 25));
        sync.setSize(new Dimension(100, 25));
        sync.setLayout(null);
        add(sync);


        var select = new JComboBox<ComboItem>();
        select.setFont(new Font("Arial", Font.BOLD, 9));
        select.setPreferredSize(new Dimension(100, 25));
        select.setSize(new Dimension(100, 25));
        select.setLayout(null);
        add(select);
        var Upd = new JButton("Upd");
        Upd.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                select.removeAllItems();
                try {
                    var request = new Request(new URI(serverLink + "/list"));
                    var result = send(request);
                    String val = result.body().toString();
                    JsonReader jsonReader = Json.createReader(new StringReader(val));
                    JsonObject jsonObject = jsonReader.readObject();
                    jsonReader.close();
                    JsonArray jsonArray = jsonObject.get("LIST").asJsonArray();
                    for(JsonValue value : jsonArray) {
                        select.addItem(new ComboItem(value.toString().replace("\"", ""), value.toString().replace("\"", "")));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        Upd.setFont(new Font("Arial", Font.BOLD, 12));
        Upd.setPreferredSize(new Dimension(75, 25));
        Upd.setSize(new Dimension(75, 25));
        Upd.setLayout(null);
        add(Upd);

        var get = new JButton("Get");
        get.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                try {
                    var line = ((ComboItem) select.getSelectedItem()).getValue();
                    var request = new Request(new URI(serverLink + "/get?name="+line ));
                    var result = send(request);
                    var val = decompressGzipBase64ToString(result.body().toString());
                    removeAllComponentsOfType(mainPanel, movedLabel.class);
                    ByteArrayInputStream xmlIn = new ByteArrayInputStream(val.getBytes());
                    XMLDecoder decoder = new XMLDecoder(xmlIn);
                    movedLabel label;
                    try {
                        while ((label = (movedLabel) decoder.readObject()) != null) {
                            label = new movedLabel(mainPanel, label.getText(), label.x, label.y);
                            mainPanel.add(label);
                            label.startThread();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        get.setFont(new Font("Arial", Font.BOLD, 10));
        get.setPreferredSize(new Dimension(75, 25));
        get.setSize(new Dimension(75, 25));
        get.setLayout(null);
        add(get);

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
    public String decompressGzipBase64ToString(String compressedBase64) throws IOException {
        if (compressedBase64 == null || compressedBase64.length() == 0) {
            return null;
        }
        byte[] compressedBytes = Base64.getMimeDecoder().decode(compressedBase64);
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
    public String compressStringToGzipBase64(String str) throws IOException {
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

    public HttpResponse<String> send(Request request) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        var r = HttpRequest.newBuilder()
                .uri(request.getURL())
                .POST(HttpRequest.BodyPublishers.ofString(request.getBody()));
        for(var line : request.getHeaders()) {
            r.header(line.getFirst().toString(), line.getSecond().toString());
        }
        var req = r.build();
        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
        return response;
    }
    public <T extends Component> void removeAllComponentsOfType(Container container, Class<T> type) {
        Component[] components = container.getComponents();
        List<Component> componentsToRemove = new ArrayList<>();

        for (Component component : components) {
            if (type.isInstance(component)) {
                componentsToRemove.add(component);
            }
        }

        for (Component component : componentsToRemove) {
            container.remove(component);
        }

        container.revalidate();
        container.repaint();
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
