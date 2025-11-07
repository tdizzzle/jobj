package utils;

public class Matrix3 {
    public double[] values;

    public Matrix3(double[] var1) {
        this.values = var1;
    }

    public Matrix3 multiply(Matrix3 var1) {
        double[] var2 = new double[9];

        for (int var3 = 0; var3 < 3; ++var3) {
            for (int var4 = 0; var4 < 3; ++var4) {
                for (int var5 = 0; var5 < 3; ++var5) {
                    var2[var3 * 3 + var4] += this.values[var3 * 3 + var5] * var1.values[var5 * 3 + var4];
                }
            }
        }

        return new Matrix3(var2);
    }

    public Vertex transform(Vertex var1) {
        return new Vertex(var1.x * this.values[0] + var1.y * this.values[3] + var1.z * this.values[6],
                var1.x * this.values[1] + var1.y * this.values[4] + var1.z * this.values[7],
                var1.x * this.values[2] + var1.y * this.values[5] + var1.z * this.values[8]);
    }
}
