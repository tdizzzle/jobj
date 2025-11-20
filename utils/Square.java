package utils;

import java.awt.Color;

import space.render;
import space.shade;
import transformers.transformer;

import java.awt.image.BufferedImage;

public class Square implements render {
    public Vertex v1;
    public Vertex v2;
    public Vertex v3;
    public Vertex v4;
    public Color color;

    public Square(Vertex v1, Vertex v2, Vertex v3, Vertex v4, Color color) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.v4 = v4;
        this.color = color;
    }

    public void renderFace(BufferedImage img, double[] zBuffer, transformer... transforms) {

        shade shader = new shade();
        // create duplicate that are transforms to
        // prevent permanently changing the class values
        Vertex tV1 = new Vertex(v1);
        Vertex tV2 = new Vertex(v2);
        Vertex tV3 = new Vertex(v3);
        Vertex tV4 = new Vertex(v4);

        // run through all the transforms on the new
        // vertexes
        for (transformer t : transforms) {
            tV1 = t.transform(tV1);
            tV2 = t.transform(tV2);
            tV3 = t.transform(tV3);
            tV4 = t.transform(tV4);
        }

        // calculate the normal vector for a square
        // crossing between the corners to get the vertex normal at the intersection
        Vertex ac = new Vertex(tV3.x - tV1.x, tV3.y - tV1.y, tV3.z - tV1.z);
        Vertex bd = new Vertex(tV4.x - tV1.x, tV4.y - tV2.y, tV4.z - tV2.z);

        Vertex norm = new Vertex(
                ac.y * bd.z - ac.z * bd.y,
                ac.z * bd.x - ac.x * bd.z,
                ac.x * bd.y - ac.y * bd.x);
        double normalLength = Math.sqrt(norm.x * norm.x + norm.y * norm.y + norm.z * norm.z);
        norm.x /= normalLength;
        norm.y /= normalLength;
        norm.z /= normalLength;

        double angleCos = Math.abs(norm.z);
        
        int minX = (int) Math.max(0, Math.ceil(Math.min(tV4.x, Math.min(tV1.x, Math.min(tV2.x, tV3.x)))));
        int maxX = (int) Math.min(img.getWidth() - 1,
                Math.floor(Math.max(tV4.x, Math.max(tV1.x, Math.max(tV2.x, tV3.x)))));
        int minY = (int) Math.max(0, Math.ceil(Math.min(tV4.y, Math.min(tV1.y, Math.min(tV2.y, tV3.y)))));
        int maxY = (int) Math.min(img.getHeight() - 1,
                Math.floor(Math.max(tV4.y, Math.max(tV1.y, Math.max(tV2.y, tV3.y)))));

        // get the area of the two triangles of the square
        double lefTriangleArea = (tV4.y - tV2.y) * (tV1.x - tV2.x) + (tV1.y - tV2.y) * (tV2.x - tV4.x);
        double rightTriangleArea = (tV2.y - tV4.y) * (tV3.x - tV4.x) + (tV3.y - tV4.y) * (tV4.x - tV2.x);

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                // todo fix as soon as the angles change from a flat plane, half the square dissapear
                Vertex inside =  insideSquare(tV1, tV2, tV3, tV4, x, y, lefTriangleArea, rightTriangleArea);
                if (inside != null) {
                    double depth = inside.x * tV1.z + inside.y * tV2.z + inside.z * tV3.z;
                    int zIndex = y * img.getWidth() + x;
                    if (zBuffer[zIndex] < depth) {
                        img.setRGB(x, y, shader.getShade(this.color, angleCos).getRGB());
                        zBuffer[zIndex] = depth;
                    }
                }
            }
        }

    };

    public Vertex insideSquare(Vertex tV1, Vertex tV2, Vertex tV3, Vertex tV4, int x, int y, double leftTriangleArea,
            double rightTriangleArea) {

        Vertex center = insideTriangle(tV4, tV1, tV2, x, y, leftTriangleArea);

        if (center != null) {
            return center;
        }

        center = insideTriangle(tV2, tV3, tV4, x, y, rightTriangleArea);
        if (center != null) {
            return center;
        }

        return null;
    }

    public Vertex insideTriangle(Vertex tV1, Vertex tV2, Vertex tV3, int x, int y, double triangleArea) {
        double b1 = ((y - tV3.y) * (tV2.x - tV3.x) + (tV2.y - tV3.y) * (tV3.x - x)) / triangleArea;
        double b2 = ((y - tV1.y) * (tV3.x - tV1.x) + (tV3.y - tV1.y) * (tV1.x - x)) / triangleArea;
        double b3 = ((y - tV2.y) * (tV1.x - tV2.x) + (tV1.y - tV2.y) * (tV2.x - x)) / triangleArea;

        if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1){
            return new Vertex(b1, b2, b3);
        }


        return null;
    }
}
