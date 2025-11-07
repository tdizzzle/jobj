package utils;

public class Vertex {
    public double x;
    public double y;
    public double z;

    public Vertex(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Copy constructor
    public Vertex(Vertex other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

}