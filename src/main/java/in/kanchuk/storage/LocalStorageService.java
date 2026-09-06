package in.kanchuk.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    @Value("${storage.local.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${storage.local.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    @Override
    public String store(MultipartFile file, String category, String subcategory) throws IOException {
        Path dir = Paths.get(uploadDir, category, subcategory).toAbsolutePath().normalize();
        Files.createDirectories(dir);

        String ext = extension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);

        Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

        return baseUrl + "/" + category + "/" + subcategory + "/" + filename;
    }

    @Override
    public void delete(String url) throws IOException {
        if (!url.startsWith(baseUrl)) return;
        String relative = url.substring(baseUrl.length()).replaceFirst("^/", "");
        Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(relative);
        Files.deleteIfExists(filePath);
    }

    private String extension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase() : "";
    }
}
