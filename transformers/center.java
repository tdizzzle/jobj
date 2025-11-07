package transformers;

import utils.Vertex;

public class center implements transformer {
    double width;
    double height;

    public center(double height, double width) {
        this.height = height;
        this.width = width;
    }

    // center the image in the middle of the frame
    public Vertex transform(Vertex v) {

        return new Vertex(v.x + (width / 2), v.y + (height / 2), v.z);
    }

}
