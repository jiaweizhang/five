import com.google.inject.Guice;
import com.google.inject.Injector;
import services.KeyValueService;

import java.io.File;

import static spark.Spark.externalStaticFileLocation;
import static spark.Spark.port;

/**
 * Created by jiaweizhang on 1/28/2017.
 */
public class Application {
    public static void main(String args[]) {

        Injector injector = Guice.createInjector(new ServiceModule());
        KeyValueService keyValueService = injector.getInstance(KeyValueService.class);

        String staticFileLocation = System.getProperty("user.dir") + "/src/main/webapp";
        externalStaticFileLocation(staticFileLocation);

        File uploadDir = new File("upload");
        uploadDir.mkdir(); // create the upload directory if it doesn't exist

        port(8080);

        new SparkEndpoints(keyValueService).createEndpoints();
        new AsyncThreads(keyValueService).start();
    }
}
