package me.rtx4090.utils;

import net.dv8tion.jda.api.entities.User;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class Drawer extends JPanel {
    private String[] labels;
    private int[] values;
    private Color[] colors;
    private Path outputPath;
    private String title;
    private Font fontCJK;

    public Drawer(Map<User, Integer> data, Path outputPath, String title) {
        System.out.println("Initializing a new Drawer");

        int total = data.values().stream().mapToInt(i -> i).sum();
        Map<String, Integer> aggregated = new LinkedHashMap<>();
        int others = 0;
        for (var e : data.entrySet()) {
            if (total > 0 && e.getValue() / (double) total < 0.02) {
                others += e.getValue();
            } else {
                aggregated.put(e.getKey().getName(), e.getValue());
            }
        }
        if (others > 0) aggregated.put("其他", others);

        this.labels = aggregated.keySet().toArray(new String[0]);
        this.values = aggregated.values().stream().mapToInt(i -> i).toArray();
        this.outputPath = outputPath;
        this.title = title;
        this.colors = new Color[values.length];

        for (int i = 0; i < values.length; i++) {
            float hue = (float) i / values.length;
            colors[i] = Color.getHSBColor(hue, 0.7f, 0.9f);
        }

        try {
            fontCJK = new Font("Noto Sans CJK TC", Font.PLAIN, 14);
            if (!fontCJK.canDisplay('測')) {
                fontCJK = new Font("Dialog", Font.PLAIN, 14);
            }
        } catch (Exception e) {
            fontCJK = new Font("Dialog", Font.PLAIN, 14);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int total = Arrays.stream(values).sum();
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 標題
        g2.setColor(Color.BLACK);
        g2.setFont(fontCJK.deriveFont(Font.BOLD, 22f));
        FontMetrics fm = g2.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g2.drawString(title, (getWidth() - titleWidth) / 2, 40);

        // 圓餅圖範圍（留右邊畫圖例）
        int diameter = 300;
        int centerX = getWidth() / 2 - 100; // 左移一點給圖例
        int centerY = getHeight() / 2 + 30;
        int pieX = centerX - diameter / 2;
        int pieY = centerY - diameter / 2;

        double startAngle = 0.0;
        double sumAngles = 0.0;

        for (int i = 0; i < values.length; i++) {
            double ratio = total == 0 ? 0 : (double) values[i] / total;
            double angle = (i == values.length - 1) ? 360 - sumAngles : ratio * 360;
            sumAngles += angle;

            g2.setColor(colors[i]);
            g2.fillArc(pieX, pieY, diameter, diameter, (int) Math.round(startAngle), (int) Math.round(angle));

            startAngle += angle;
        }

        // 圖例位置與樣式
        int legendX = centerX + diameter / 2 + 30;
        int legendY = pieY;
        int rectSize = 14;
        int lineHeight = 20;

        g2.setFont(fontCJK.deriveFont(14f));
        for (int i = 0; i < labels.length; i++) {
            g2.setColor(colors[i]);
            g2.fillRect(legendX, legendY + i * lineHeight, rectSize, rectSize);

            g2.setColor(Color.BLACK);
            String text = String.format("%s (%d)", labels[i], values[i]);
            g2.drawString(text, legendX + rectSize + 6, legendY + i * lineHeight + rectSize - 2);
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
