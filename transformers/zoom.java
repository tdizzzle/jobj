package transformers;

import utils.Vertex;

public class zoom implements transformer {
    double scaleFactor;

    public zoom(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    @Override
    public Vertex transform(Vertex v) {
        v.x *= scaleFactor;
        v.y *= scaleFactor;
        v.z *= scaleFactor;
        return v;
    }
}
