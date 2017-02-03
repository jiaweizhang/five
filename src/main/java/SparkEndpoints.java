/**
 * Created by jiaweizhang on 1/29/2017.
 */

import com.google.inject.Inject;
import model.FileSetMetadata;
import model.UrlMetadata;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static spark.Spark.*;

public class SparkEndpoints {

    private KeyValueService keyValueService;

    @Inject
    public SparkEndpoints(KeyValueService keyValueService) {
        port(8080);
        this.keyValueService = keyValueService;
    }

    // http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public void createEndpoints() {

        get("/:url/:fileName", (request, response) -> {
            if (keyValueService.fileExists(request.params(":url"), request.params(":fileName"))) {
                // add 1 to download count
                keyValueService.addDownloadCount(request.params(":url"), request.params(":fileName"));
                // download file

                Path path = Paths.get("upload/" + request.params(":url") + "/" + request.params(":fileName"));
                Files.copy(path, response.raw().getOutputStream());
                response.raw().getOutputStream().flush();
                response.raw().getOutputStream().close();
                return response.raw();
            }

            return "File not found";
        });

        get("/:url", (request, response) -> {
            if (keyValueService.urlExists(request.params(":url"))) {
                UrlMetadata urlMetadata = keyValueService.accessUrl(request.params(":url"));
                return "<p>Access count: " + urlMetadata.getAccessCount() + "</p>"
                        + "<p>Expiration: " + DateUtility.getTimestampString(urlMetadata.getExpirationTimestamp()) + " EST</p>"
                        + urlMetadata.getFiles()
                        .stream()
                        .map(f -> "<p>Downloaded " + f.getDownloadCount() + " times <a href='"
                                + request.params(":url") + "/" + f.getFileName() + "' download>" + f.getFileName()
                                + "</a></p>")
                        .collect(Collectors.joining(""));
            }
            return "<form method='post' enctype='multipart/form-data'>"
                    + "<input type='file' name='file[]' multiple>"
                    + "<input type='number' name='expiration'>"
                    + "<button>Upload file</button>"
                    + "</form>";
        });

        post("/:url", "multipart/form-data", (request, response) -> {

            String location = "upload";          // the directory location where files will be stored
            long maxFileSize = 100000000;       // the maximum size allowed for uploaded files
            long maxRequestSize = 100000000;    // the maximum size allowed for multipart/form-data requests
            int fileSizeThreshold = 0;       // the size threshold after which files will be written to disk

            MultipartConfigElement multipartConfigElement = new MultipartConfigElement(
                    location, maxFileSize, maxRequestSize, fileSizeThreshold);
            request.raw().setAttribute("org.eclipse.jetty.multipartConfig",
                    multipartConfigElement);

            int expiration = -1;

            Part expirationPart = request.raw().getPart("expiration");
            expiration = Integer.parseInt(convertStreamToString(expirationPart.getInputStream()));
            if (expiration == -1) {
                return "Invalid expiration";
            }

            if (keyValueService.urlExists(request.params(":url"))) {
                return "Url already exists";
            }

            List<FileSetMetadata> fileSetMetadataList = new ArrayList<>();

            Collection<Part> parts = request.raw().getParts();
            for (Part part : parts) {
                if (part.getName().equals("expiration")) {
                    continue;
                }

                System.out.println("Filename: " + part.getSubmittedFileName());

                FileSetMetadata fileSetMetadata = new FileSetMetadata();
                fileSetMetadata.setFileName(part.getSubmittedFileName());

                // create directory if not exist
                File directory = new File("upload/" + request.params(":url"));
                directory.mkdir();

                // actually write file
                Path out = Paths.get("upload/" + request.params(":url") + "/" + part.getSubmittedFileName());
                try (final InputStream in = part.getInputStream()) {
                    MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

                    // write file
                    Files.copy(in, out);

                    // TODO - eliminate double inputstream traverse
                    // calculate sha1
                    byte[] buffer = new byte[8192];
                    int len = in.read(buffer);

                    while (len != -1) {
                        sha1.update(buffer, 0, len);
                        len = in.read(buffer);
                    }

                    String hash = new HexBinaryAdapter().marshal(sha1.digest());
                    fileSetMetadata.setHash(hash);
                    fileSetMetadataList.add(fileSetMetadata);

                    part.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // cleanup
            multipartConfigElement = null;

            // store
            keyValueService.storeUrl(request.params(":url"), expiration, fileSetMetadataList);

            return "Files uploaded";
        });
    }
}
