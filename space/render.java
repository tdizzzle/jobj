package space;

import java.awt.image.BufferedImage;
import transformers.transformer;

public interface render {

    public void renderFace(BufferedImage img, double[] Zbuffer, transformer ...transforms);
}