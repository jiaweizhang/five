import com.google.inject.AbstractModule;
import services.KeyValueService;
import services.PostgresService;
import services.RedisService;
import services.SQLService;

/**
 * Created by jiaweizhang on 1/29/2017.
 */
public class ServiceModule extends AbstractModule {

    @Override
    public void configure() {
        bind(KeyValueService.class).to(RedisService.class);
        // use Postgres until MariaDB is looked into more
        bind(SQLService.class).to(PostgresService.class);
    }
}
