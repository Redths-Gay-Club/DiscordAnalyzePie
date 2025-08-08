package me.rtx4090.utils;

import net.dv8tion.jda.api.entities.User;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class Drawer extends JPanel {
    private User[] keys;
    private int[] values;
    private Color[] colors;
    private Path outputPath;
    private String title;

    public Drawer(Map<User, Integer> data, Path outputPath, String title) {
        System.out.println("Initializing a new Drawer");
        this.keys = data.keySet().toArray(new User[0]);
        this.values = data.values().stream().mapToInt(i -> i).toArray();
        this.outputPath = outputPath;
        this.title = title;
        this.colors = new Color[values.length];
        for (int i = 0; i < values.length; i++) {
            float hue = (float) i / values.length;
            colors[i] = Color.getHSBColor(hue, 0.7f, 0.9f);
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int total = 0;
        for (int value : values) total += value;

        // Draw title
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g.drawString(title, (getWidth() - titleWidth) / 2, 30);

        int startAngle = 0;
        int cx = 150, cy = 150, r = 100;
        for (int i = 0; i < values.length; i++) {
            int arcAngle = (int) Math.round(360.0 * values[i] / total);
            g.setColor(colors[i]);
            g.fillArc(50, 50, 200, 200, startAngle, arcAngle);

            // Draw key label
            double theta = Math.toRadians(startAngle + arcAngle / 2.0);
            int labelX = cx + (int) (r * 0.7 * Math.cos(theta));
            int labelY = cy + (int) (r * 0.7 * Math.sin(theta));
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString(keys[i].getName(), labelX, labelY);

            startAngle += arcAngle;
        }
    }

    public void exportToPNG(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        this.setSize(width, height);
        this.paint(g2);
        g2.dispose();
        ImageIO.write(image, "png", outputPath.toFile());
    }
}