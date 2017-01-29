import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Created by jiaweizhang on 1/28/2017.
 */
public class Application {
    public static void main(String args[]) {

        Injector injector = Guice.createInjector(new ServiceModule());
        KeyValueService keyValueService = injector.getInstance(KeyValueService.class);

        new SparkEndpoints(keyValueService).createEndpoints();
    }
}
