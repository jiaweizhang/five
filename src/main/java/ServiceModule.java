import com.google.inject.AbstractModule;

/**
 * Created by jiaweizhang on 1/29/2017.
 */
public class ServiceModule extends AbstractModule {

    @Override
    public void configure() {
        bind(KeyValueService.class).to(RedisService.class);
    }
}
