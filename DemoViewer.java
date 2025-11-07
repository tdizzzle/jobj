import javax.swing.*;

import utils.Vertex;
import utils.Triangle;
import utils.Matrix3;

import obj.importer;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.image.BufferedImage;

public class DemoViewer {

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // slider to control horizontal rotation
        JSlider headingSlider = new JSlider(-180, 180, 0);
        pane.add(headingSlider, BorderLayout.SOUTH);

        // slider to control vertical rotation
        JSlider pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        pane.add(pitchSlider, BorderLayout.EAST);

        // slider to control zoom
        JSlider zoomSlider = new JSlider(SwingConstants.VERTICAL, -100, 100, 1);
        pane.add(zoomSlider, BorderLayout.WEST);

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false); // Prevent toolbar from being dragged

        // Add customizable buttons
        JButton importButton = new JButton("Import OBJ");
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();

                    JOptionPane.showMessageDialog(frame,
                            "Imported: " + selectedFile.getAbsolutePath(),
                            "File Imported",
                            JOptionPane.INFORMATION_MESSAGE);

                            
                    // process the file after popping the message
                    // run the importer here
                    importer i = new importer();
                    currentShape = i.decode(selectedFile.getAbsolutePath());
                }
            }
        });
        toolbar.add(importButton);

        pane.add(toolbar, BorderLayout.NORTH);

        // panel to display render results
        JPanel renderPanel = new JPanel() {
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // for (int i = 0; i < 4; i++) {
                // tris = inflate(tris);
                // }

                double zoom = zoomSlider.getValue();

                if (zoom == 0) {
                    zoom = 1;
                }
                if (zoom < 0) {
                    zoom = -1 / zoom;
                }

                double heading = Math.toRadians(headingSlider.getValue());
                Matrix3 headingTransform = new Matrix3(new double[] {
                        Math.cos(heading), 0, -Math.sin(heading),
                        0, 1, 0,
                        Math.sin(heading), 0, Math.cos(heading)
                });
                double pitch = Math.toRadians(pitchSlider.getValue());
                Matrix3 pitchTransform = new Matrix3(new double[] {
                        1, 0, 0,
                        0, Math.cos(pitch), Math.sin(pitch),
                        0, -Math.sin(pitch), Math.cos(pitch)
                });
                Matrix3 transform = headingTransform.multiply(pitchTransform);

                BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

                double[] zBuffer = new double[img.getWidth() * img.getHeight()];
                // initialize array with extremely far away depths
                for (int q = 0; q < zBuffer.length; q++) {
                    zBuffer[q] = Double.NEGATIVE_INFINITY;
                }

                for (Triangle t : currentShape) {
                    Vertex v1 = transform.transform(t.v1);
                    v1.zoom(zoom);
                    v1.x += getWidth() / 2;
                    v1.y += getHeight() / 2;
                    Vertex v2 = transform.transform(t.v2);
                    v2.zoom(zoom);
                    v2.x += getWidth() / 2;
                    v2.y += getHeight() / 2;
                    Vertex v3 = transform.transform(t.v3);
                    v3.zoom(zoom);
                    v3.x += getWidth() / 2;
                    v3.y += getHeight() / 2;

                    Vertex ab = new Vertex(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);
                    Vertex ac = new Vertex(v3.x - v1.x, v3.y - v1.y, v3.z - v1.z);
                    Vertex norm = new Vertex(
                            ab.y * ac.z - ab.z * ac.y,
                            ab.z * ac.x - ab.x * ac.z,
                            ab.x * ac.y - ab.y * ac.x);
                    double normalLength = Math.sqrt(norm.x * norm.x + norm.y * norm.y + norm.z * norm.z);
                    norm.x /= normalLength;
                    norm.y /= normalLength;
                    norm.z /= normalLength;

                    double angleCos = Math.abs(norm.z);

                    int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
                    int maxX = (int) Math.min(img.getWidth() - 1, Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
                    int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
                    int maxY = (int) Math.min(img.getHeight() - 1, Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

                    double triangleArea = (v1.y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - v1.x);

                    for (int y = minY; y <= maxY; y++) {
                        for (int x = minX; x <= maxX; x++) {
                            double b1 = ((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / triangleArea;
                            double b2 = ((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / triangleArea;
                            double b3 = ((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / triangleArea;
                            if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
                                double depth = b1 * v1.z + b2 * v2.z + b3 * v3.z;
                                int zIndex = y * img.getWidth() + x;
                                if (zBuffer[zIndex] < depth) {
                                    img.setRGB(x, y, getShade(t.color, angleCos).getRGB());
                                    zBuffer[zIndex] = depth;
                                }
                            }
                        }
                    }

                }

                g2.drawImage(img, 0, 0, null);
            }
        };
        pane.add(renderPanel, BorderLayout.CENTER);

        headingSlider.addChangeListener(e -> renderPanel.repaint());
        pitchSlider.addChangeListener(e -> renderPanel.repaint());
        zoomSlider.addChangeListener(e -> renderPanel.repaint());

        addToolbarDropdown(toolbar, renderPanel);

        frame.setSize(400, 400);
        frame.setVisible(true);
    }

    // simple shader
    public static Color getShade(Color color, double shade) {
        double redLinear = Math.pow(color.getRed(), 2.4) * shade;
        double greenLinear = Math.pow(color.getGreen(), 2.4) * shade;
        double blueLinear = Math.pow(color.getBlue(), 2.4) * shade;

        int red = (int) Math.pow(redLinear, 1 / 2.4);
        int green = (int) Math.pow(greenLinear, 1 / 2.4);
        int blue = (int) Math.pow(blueLinear, 1 / 2.4);

        return new Color(red, green, blue);
    }

    public static List<Triangle> inflate(List<Triangle> tris) {
        List<Triangle> result = new ArrayList<>();
        for (Triangle t : tris) {
            // calculate the mid point of each vertex
            Vertex m1 = new Vertex((t.v1.x + t.v2.x) / 2, (t.v1.y + t.v2.y) / 2, (t.v1.z + t.v2.z) / 2);
            Vertex m2 = new Vertex((t.v2.x + t.v3.x) / 2, (t.v2.y + t.v3.y) / 2, (t.v2.z + t.v3.z) / 2);
            Vertex m3 = new Vertex((t.v1.x + t.v3.x) / 2, (t.v1.y + t.v3.y) / 2, (t.v1.z + t.v3.z) / 2);
            // then create 4 subvertexes from it
            result.add(new Triangle(t.v1, m1, m3, t.color));
            result.add(new Triangle(t.v2, m1, m2, t.color));
            result.add(new Triangle(t.v3, m2, m3, t.color));
            result.add(new Triangle(m1, m2, m3, t.color));
        }

        // normalise it all
        for (Triangle t : result) {
            for (Vertex v : new Vertex[] { t.v1, t.v2, t.v3 }) {
                double l = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z) / Math.sqrt(30000);
                v.x /= l;
                v.y /= l;
                v.z /= l;
            }
        }
        return result;
    }

    private static void addToolbarDropdown(JToolBar toolbar, JPanel renderPanel) {

        // Add dropdown (combo box)
        String[] options = { "Pyramid", "Sphere", "Cube", "Hexagon" };
        JComboBox<String> dropdown = new JComboBox<>(options);
        dropdown.addActionListener(e -> {
            String selected = (String) dropdown.getSelectedItem();
            System.out.println("Selected: " + selected);
            switch (selected) {
                case "Cube":
                    currentShape = exampleCube;
                    break;
                case "Pyramid":
                    currentShape = exampleTriangle;
                    break;
                case "Sphere":
                    currentShape = exampleSphere;
                    break;
                case "Hexagon":
                    currentShape = exampleHexagon;
                    break;
            }
        });
        toolbar.add(dropdown);
        dropdown.addActionListener(e -> renderPanel.repaint());
    }

    // Add a bunch of constant shapes down here?

    public static final List<Triangle> exampleTriangle = Arrays.asList(

            new Triangle(
                    new Vertex(100, 100, 100),
                    new Vertex(-100, -100, 100),
                    new Vertex(-100, 100, -100),
                    Color.WHITE),
            new Triangle(
                    new Vertex(100, 100, 100),
                    new Vertex(-100, -100, 100),
                    new Vertex(100, -100, -100),
                    Color.RED),
            new Triangle(
                    new Vertex(-100, 100, -100),
                    new Vertex(100, -100, -100),
                    new Vertex(100, 100, 100),
                    Color.GREEN),
            new Triangle(
                    new Vertex(-100, 100, -100),
                    new Vertex(100, -100, -100),
                    new Vertex(-100, -100, 100),
                    Color.BLUE));

    static Vertex v0 = new Vertex(-100, -100, -100); // Bottom-back-left
    static Vertex v1 = new Vertex(100, -100, -100); // Bottom-back-right
    static Vertex v2 = new Vertex(100, 100, -100); // Top-back-right
    static Vertex v3 = new Vertex(-100, 100, -100); // Top-back-left
    static Vertex v4 = new Vertex(-100, -100, 100); // Bottom-front-left
    static Vertex v5 = new Vertex(100, -100, 100); // Bottom-front-right
    static Vertex v6 = new Vertex(100, 100, 100); // Top-front-right
    static Vertex v7 = new Vertex(-100, 100, 100); // Top-front-left

    public static final List<Triangle> exampleCube = Arrays.asList(

            // Top face (v3, v2, v6, v7)
            new Triangle(v3, v2, v6, Color.WHITE),
            new Triangle(v3, v6, v7, Color.WHITE),

            // Bottom face (v0, v1, v5, v4)
            new Triangle(v0, v1, v5, Color.GREEN),
            new Triangle(v0, v5, v4, Color.GREEN),

            // Front face (v4, v5, v6, v7)
            new Triangle(v4, v5, v6, Color.RED),
            new Triangle(v4, v6, v7, Color.RED),

            // Back face (v0, v1, v2, v3)
            new Triangle(v0, v1, v2, Color.BLUE),
            new Triangle(v0, v2, v3, Color.BLUE),

            // Right face (v1, v5, v6, v2)
            new Triangle(v1, v5, v6, Color.YELLOW),
            new Triangle(v1, v6, v2, Color.YELLOW),

            // Left face (v0, v4, v7, v3)
            new Triangle(v0, v4, v7, Color.MAGENTA),
            new Triangle(v0, v7, v3, Color.MAGENTA));

    public static final List<Triangle> exampleSphere = createExampleSphere();

    private static List<Triangle> createExampleSphere() {
        List<Triangle> tris = new ArrayList<>(exampleTriangle);
        for (int i = 0; i < 4; i++) {
            tris = inflate(tris);
        }
        return tris;
    }

    public static List<Triangle> generateHexagon(double radius, double height, Color[] faceColors) {
        Vertex[] top = new Vertex[6];
        Vertex[] bottom = new Vertex[6];

        // Create top and bottom vertices
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i);
            double x = radius * Math.cos(angle);
            double y = radius * Math.sin(angle);
            top[i] = new Vertex(x, y, height / 2);
            bottom[i] = new Vertex(x, y, -height / 2);
        }

        List<Triangle> hexagon = new ArrayList<>();

        // Top face (fan from top[0])
        for (int i = 1; i < 5; i++) {
            hexagon.add(new Triangle(top[0], top[i], top[i + 1], faceColors[0]));
        }

        // Bottom face (fan from bottom[0])
        for (int i = 1; i < 5; i++) {
            hexagon.add(new Triangle(bottom[0], bottom[i + 1], bottom[i], faceColors[1]));
        }

        // Side faces
        for (int i = 0; i < 6; i++) {
            int next = (i + 1) % 6;
            hexagon.add(new Triangle(top[i], top[next], bottom[next], faceColors[2 + i]));
            hexagon.add(new Triangle(top[i], bottom[next], bottom[i], faceColors[2 + i]));
        }

        return hexagon;
    }

    public static final List<Triangle> exampleHexagon = generateHexagon(
            100, // radius
            100, // height
            new Color[] {
                    Color.WHITE, Color.GREEN, // top and bottom
                    Color.RED, Color.BLUE, Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.ORANGE // sides
            });

    public static List<Triangle> currentShape = exampleTriangle;

}
