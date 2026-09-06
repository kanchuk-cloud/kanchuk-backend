package in.kanchuk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${storage.local.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path absPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String base = "file:" + absPath + "/";

        // Primary path: /uploads/**  → ./uploads/
        registry.addResourceHandler("/uploads/**").addResourceLocations(base);

        // Legacy paths for images stored without the /uploads/ prefix in the DB
        registry.addResourceHandler("/catalogue/**").addResourceLocations(base + "catalogue/");
        registry.addResourceHandler("/marketing/**").addResourceLocations(base + "marketing/");
    }
}
