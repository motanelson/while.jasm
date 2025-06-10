import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class VoxelViewer3D extends JPanel implements ActionListener {
    private final int[][][] voxelData;
    private final int sizeX, sizeY, sizeZ,sizeXX;
    private final Color[] vgaColors;
    private double rotation = 0;

    public VoxelViewer3D(File file) throws IOException {
        // Leitura do ficheiro
        byte[] raw = Files.readAllBytes(file.toPath());
        if (raw[0] != '3' || raw[1] != 'D') throw new IOException("Formato inválido");

        sizeX = Byte.toUnsignedInt(raw[2]);
        sizeXX = Byte.toUnsignedInt(raw[3]);
        sizeY = Byte.toUnsignedInt(raw[4]);
        sizeZ = Byte.toUnsignedInt(raw[5]);

        voxelData = new int[sizeZ][sizeY][sizeX];

        int index = 6;
        for (int z = 0; z < sizeZ; z++)
            for (int y = 0; y < sizeY; y++)
                for (int x = 0; x < sizeX; x++)
                    voxelData[z][y][x] = Byte.toUnsignedInt(raw[index++]);

        vgaColors = new Color[] {
                new Color(0x000000), new Color(0x0000AA), new Color(0x00AA00), new Color(0x00AAAA),
                new Color(0xAA0000), new Color(0xAA00AA), new Color(0xAA5500), new Color(0xAAAAAA),
                new Color(0x555555), new Color(0x5555FF), new Color(0x55FF55), new Color(0x55FFFF),
                new Color(0xFF5555), new Color(0xFF55FF), new Color(0xFFFF55), new Color(0xFFFFFF)
        };

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                rotation += Math.PI / 8; // 45 graus a cada 2s
                repaint();
            }
        }, 600, 600);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
    
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        g2.setColor(Color.YELLOW);
        g2.fillRect(0, 0, w, h);

        double scale = 10;
        int ox = w / 2;
        int oy = h / 2;

        for (int z = 0; z < sizeZ; z++) {
            double depthScale = 1.0 + (z / (double) sizeZ);
            for (int y = 0; y < sizeY; y++) {
                for (int x = 0; x < sizeX; x++) {
                    int colorIndex = voxelData[z][y][x];
                    if (colorIndex == 0) continue;
                    Color color = vgaColors[colorIndex % 16];

                    double[] pos = rotate3D(x - sizeX / 2.0, y - sizeY / 2.0, z - sizeZ / 2.0);
                    int bx = (int) (ox + pos[0] * scale * depthScale);
                    int by = (int) (oy + pos[1] * scale * depthScale);
                    g2.setColor(color);
                    g2.fillRect(bx, by, (int) (scale * depthScale), (int) (scale * depthScale));
                }
            }
        }
    }

    private double[] rotate3D(double x, double y, double z) {
        double cos = Math.cos(rotation);
        double sin = Math.sin(rotation);
        double x2 = x * cos - z * sin;
        double z2 = x * sin + z * cos;
        return new double[] { x2, y, z2 };
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("Visualizador .3D tipo Minecraft");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(Color.YELLOW);
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;

        VoxelViewer3D panel = new VoxelViewer3D(chooser.getSelectedFile());
        frame.add(panel);
        
        frame.setSize(800, 600);
        
        frame.setVisible(true);
    }
}

