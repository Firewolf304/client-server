import javax.swing.*;
import java.awt.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                var window = new JFrame();
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                window.setTitle("Hello world!");

                //this.pack();

                //window.setSize(500,500);
                var frameWindow = new panel(500, 500);
                window.add(frameWindow);


                window.pack();
                window.setResizable(true);
                window.setLayout(null);
                window.setIgnoreRepaint(false);
                window.setVisible(true);
                //frameWindow.startThread();
            }
        });

    }
}