/**
 * Created by starksm on 6/11/16.
 */
public class TestTheta {
    public static void main(String[] args) {
        for(int n = 0; n < 100; n ++) {
            double theta = Math.random() * 2*Math.PI;
            System.out.printf("[%d]: %.0f, x=%.0f\n", n, 180*theta/Math.PI, 100*Math.cos(theta));
        }
    }
}
