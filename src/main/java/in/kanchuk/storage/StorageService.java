package in.kanchuk.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Swap implementations to move from local disk to S3:
 *   1. Set storage.type=s3 in application.yml
 *   2. Create S3StorageService implementing this interface
 *   3. Remove LocalStorageService or keep both with @ConditionalOnProperty
 */
public interface StorageService {
    /**
     * Stores the file and returns its public URL.
     * Path: {category}/{subcategory}/{uuid}.{ext}
     */
    String store(MultipartFile file, String category, String subcategory) throws IOException;

    /** Deletes the file identified by its full public URL. */
    void delete(String url) throws IOException;
}
