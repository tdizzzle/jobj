package obj;

import java.util.ArrayList;
import java.util.List;

import utils.Triangle;
import utils.Vertex;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class importer {

    public importer() {

    }
    /*
     * 
     * Here is where the importer will run
     * 
     * Need to read a file
     * parse the vertexes
     * have some simple error handling
     * return an array of triangles
     * Then update to handle 4 vertexes
     * 
     * Nead to decode is the only function for the moment
     * 
     * each key does something
     */

    public List<Triangle> decode(String filename) {

        // read file
        // map of vertexes
        // add some other bits like dimensions

        // @TODO read through the file and see how many vaces /vertices are used
        List<Vertex> vertices = new ArrayList<>();
        List<Triangle> faces = new ArrayList<>();

        try (Reader reader = new FileReader(filename);
                BufferedReader bufferedReader = new BufferedReader(reader)) {

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line); // Process each line as needed

                if (line.length() == 0) {
                    continue;
                }

                switch (line.charAt(0)) {
                    case 'v':
                        String vertex = line.substring(2);
                        String[] subVertexes = vertex.split(" ");
                        System.out.println(subVertexes.length);
                        if (subVertexes.length != 3) {
                            continue;
                        }

                        vertices.add(
                                new Vertex(
                                        Double.parseDouble(subVertexes[0]),
                                        Double.parseDouble(subVertexes[1]),
                                        Double.parseDouble(subVertexes[2])));

                        System.out.println(vertices.size());

                        break;
                    case 'f':

                        String face = line.substring(2);
                        String[] faceVertexes = face.split(" ");

                        List<String> cleanFaceVertexes = new ArrayList<>();
                        for (String fv : faceVertexes) {
                            // ignore the spaces
                            if (fv != "") {
                                cleanFaceVertexes.add(fv);
                            }
                        }

                        // trim the spaces
                        if (cleanFaceVertexes.size() != 3) {
                            continue;
                        }

                        faces.add(new Triangle(
                                // -1 as obj arrays start at 1
                                vertices.get(Integer.parseInt(cleanFaceVertexes.get(0)) - 1),
                                vertices.get(Integer.parseInt(cleanFaceVertexes.get(1)) - 1),
                                vertices.get(Integer.parseInt(cleanFaceVertexes.get(2)) - 1),
                                Color.ORANGE));
                        System.err.println(faces.getLast().toString());
                        break;
                    default:
                        // skip lines we do not know how to handle
                        break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(vertices.toString());
        System.out.println(faces.toString());
       
        return faces;
    }

}
