package utils;

import java.awt.Color;

import space.render;
import space.shade;
import transformers.transformer;

import java.awt.image.BufferedImage;

public class Triangle implements render {
    public Vertex v1;
    public Vertex v2;
    public Vertex v3;
    public Color color;

    public Triangle(Vertex v1, Vertex v2, Vertex v3, Color color) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.color = color;
    }

    public void renderFace(BufferedImage img, double[] zBuffer, transformer... transforms) {

        shade shader = new shade();
   
        // create duplicate that are transforms to
        // prevent permanently changing the class values
        Vertex tV1 = new Vertex(v1);
        Vertex tV2 = new Vertex(v2);
        Vertex tV3 = new Vertex(v3);
     
        // run through all the transforms on the new 
        // vertexes
        for (transformer t : transforms) {
            tV1 = t.transform(tV1);
            tV2 = t.transform(tV2);
            tV3 = t.transform(tV3);
        }
   
        // calculate the normal vector for a triangle
        Vertex ab = new Vertex(tV2.x - tV1.x, tV2.y - tV1.y, tV2.z - tV1.z);
        Vertex ac = new Vertex(tV3.x - tV1.x, tV3.y - tV1.y, tV3.z - tV1.z);
        Vertex norm = new Vertex(
                ab.y * ac.z - ab.z * ac.y,
                ab.z * ac.x - ab.x * ac.z,
                ab.x * ac.y - ab.y * ac.x);
        double normalLength = Math.sqrt(norm.x * norm.x + norm.y * norm.y + norm.z * norm.z);
        norm.x /= normalLength;
        norm.y /= normalLength;
        norm.z /= normalLength;

        double angleCos = Math.abs(norm.z);

        int minX = (int) Math.max(0, Math.ceil(Math.min(tV1.x, Math.min(tV2.x, tV3.x))));
        int maxX = (int) Math.min(img.getWidth() - 1, Math.floor(Math.max(tV1.x, Math.max(tV2.x, tV3.x))));
        int minY = (int) Math.max(0, Math.ceil(Math.min(tV1.y, Math.min(tV2.y, tV3.y))));
        int maxY = (int) Math.min(img.getHeight() - 1, Math.floor(Math.max(tV1.y, Math.max(tV2.y, tV3.y))));

        double triangleArea = (tV1.y - tV3.y) * (tV2.x - tV3.x) + (tV2.y - tV3.y) * (tV3.x - tV1.x);

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                double b1 = ((y - tV3.y) * (tV2.x - tV3.x) + (tV2.y - tV3.y) * (tV3.x - x)) / triangleArea;
                double b2 = ((y - tV1.y) * (tV3.x - tV1.x) + (tV3.y - tV1.y) * (tV1.x - x)) / triangleArea;
                double b3 = ((y - tV2.y) * (tV1.x - tV2.x) + (tV1.y - tV2.y) * (tV2.x - x)) / triangleArea;
                if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
                    double depth = b1 * tV1.z + b2 * tV2.z + b3 * tV3.z;
                    int zIndex = y * img.getWidth() + x;
                    if (zBuffer[zIndex] < depth) {
                        img.setRGB(x, y, shader.getShade(this.color, angleCos).getRGB());
                        zBuffer[zIndex] = depth;
                    }
                }
            }
        }

    };
}
