package Dispositivos;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
//Al tratar de usar las bibliotecas iniciadas en javax me da un error que no me dio tiempo de resolver
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.util.Random;

public class Proyector implements DispositivoCasa {
	private BufferedImage currentImage;

    public void showPhoto(String imagePath) {
        try {
            // Cargar la imagen desde el archivo
            currentImage = ImageIO.read(new File(imagePath));
            
            // Crear un marco para mostrar la imagen
            JFrame frame = new JFrame("Imagen en el Proyector");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            // Crear un panel para mostrar la imagen
            JPanel panel = new JPanel() {
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // Dibuja la imagen en el panel
                    g.drawImage(currentImage, 0, 0, this);
                }
            };
            
            frame.add(panel);
            frame.setSize(currentImage.getWidth(), currentImage.getHeight());
            frame.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeVolume(int volume) {
    	// Genera un número aleatorio de 0 a 100 para el volumen
    	Random rand = new Random();
        int volume1 = rand.nextInt(101);
        volume = volume1;
        System.out.println("Volumen cambiado a " + volume);
    }

    public void autoAdjustDisplaySize() {
        System.out.println("Tamaño de pantalla ajustado automáticamente");
    }
}