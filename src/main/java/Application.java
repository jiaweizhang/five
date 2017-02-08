import com.google.inject.Guice;
import com.google.inject.Injector;
import services.KeyValueService;
import services.SQLService;

import java.io.File;

import static spark.Spark.externalStaticFileLocation;

/**
 * Created by jiaweizhang on 1/28/2017.
 */
public class Application {
    public static void main(String args[]) {

        Injector injector = Guice.createInjector(new ServiceModule());
        KeyValueService keyValueService = injector.getInstance(KeyValueService.class);
        SQLService sqlService = injector.getInstance(SQLService.class);

        String staticFileLocation = System.getProperty("user.dir") + "/src/main/webapp";
        externalStaticFileLocation(staticFileLocation);

        File uploadDir = new File("upload");
        uploadDir.mkdir(); // create the upload directory if it doesn't exist

        new SparkEndpoints(keyValueService, sqlService).createEndpoints();
        new AsyncThreads(keyValueService).start();
    }
}
