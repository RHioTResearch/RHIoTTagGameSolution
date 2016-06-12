import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import org.jboss.rhiot.game.CloudClient;

/**
 * Created by starksm on 6/10/16.
 */
public class TestGetCloudPassword {

    public static void main(String[] args) throws Exception {
        String gwIP = CloudClient.calcGWIP();
        URL getPassURL = new URL("http://"+gwIP+":8080/rhiot/cloud-password");
        try(InputStream is = getPassURL.openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String password = reader.readLine();
            System.out.printf("Retrieved password: %s\n", password);
        }
    }
}
