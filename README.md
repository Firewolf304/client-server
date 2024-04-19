Анализ и изучение java-приложений на основе использования GUI и системы сокетов на JAVA.
# Usings:
## Laba1:
```text
javax.swing.*
java.awt.*
java.util.*
``` 
По-простому: создание двух классов, где 1 - JPanel, другая - JLabel. Нужно их объединить по parent и по ```addMouseListener``` отслеживать нажатия.
Остальное тупо создать кнопку, которая через Array.toList::forEach отсматривать все ```movedLabel```, которые вызывают либо ```mutex.lock```, либо ```mutex.unlock```, в зависимости от ```paused```, который также не дает создать объект через JPanel по условию:
```js
if(e.getButton() == MouseEvent.BUTTON1 && !paused) {
    var label = new movedLabel(mainPanel, "123", e.getX(), e.getY());
    add(label);
    label.startThread();
    repaint();
} else if(e.getButton() == MouseEvent.BUTTON3) { // заглушка
    System.out.println( "Found: " + e.getComponent().getName() );
}
```

## Laba2:
```text
javax.swing.*
java.awt.*
import java.awt.event.*;
java.util.*
import java.net.*; // для сервака и Socket и ServerSocket
import java.io.*;
import java.beans.*;
import java.net.*;  
```

По-простому: на основе laba1 передать сериализированный xml данные о последнем состоянии (сериализировать компоненты movedLabel, остальное сложнее из-за ивентов, но легче передавать просто сами классы):
```js
try {
    while ((label = (movedLabel) decoder.readObject()) != null) {
        label = new movedLabel(frameWindow, label.getText(), label.x, label.y);
        frameWindow.add(label);
        label.startThread();
    }
} catch (Exception e) {
    e.printStackTrace();
}
```



