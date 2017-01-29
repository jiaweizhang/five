/**
 * Created by jiaweizhang on 1/29/2017.
 */

import com.google.inject.Inject;
import model.UrlMetadata;

import java.util.Date;

import static spark.Spark.get;
import static spark.Spark.port;

public class SparkEndpoints {

    private KeyValueService keyValueService;

    @Inject
    public SparkEndpoints(KeyValueService keyValueService) {
        port(8080);
        this.keyValueService = keyValueService;
    }

    public void createEndpoints() {
        get("/timestamp", (request, response) -> new Date().getTime() / 1000);

        get("/:url/:filename", (request, response) -> {
            if (keyValueService.urlExists(request.params(":url"))) {
                // add 1 to download count
                keyValueService.addDownloadCount(request.params(":url"));
                // download file
                return "File";
            }

            return "File not found";
        });

        get("/:url", (request, response) -> {
            UrlMetadata urlMetadata = keyValueService.accessUrl(request.params(":url"));
            if (urlMetadata.isExists()) {
                return urlMetadata.getFileName();
            }
            return "Option to upload new file";
        });
    }
}
