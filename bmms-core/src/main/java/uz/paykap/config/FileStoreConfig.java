package uz.paykap.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.content.fs.config.EnableFilesystemStores;
import org.springframework.content.fs.io.FileSystemResourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Configuration
@EnableFilesystemStores
@AllArgsConstructor
public class FileStoreConfig {

    private final ApplicationProperties properties;

    @Bean
    public File fileSystemRoot() {
        log.warn("File content path for storing files : {}", properties.getFileStorageBasePath());

        final Path path = Paths.get(properties.getFileStorageBasePath());

        if (!Files.exists(path)) {
            mkdir(path);
        }

        return new File(properties.getFileStorageBasePath());
    }

    private void mkdir(Path path) {
        log.warn("Path not exists, mkdir all : {}", properties.getFileStorageBasePath());

        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            Set<PosixFilePermission> fullPermission = new HashSet<>();
            fullPermission.add(PosixFilePermission.OWNER_EXECUTE);
            fullPermission.add(PosixFilePermission.OWNER_READ);
            fullPermission.add(PosixFilePermission.OWNER_WRITE);

            fullPermission.add(PosixFilePermission.GROUP_EXECUTE);
            fullPermission.add(PosixFilePermission.GROUP_READ);
            fullPermission.add(PosixFilePermission.GROUP_WRITE);

            fullPermission.add(PosixFilePermission.OTHERS_EXECUTE);
            fullPermission.add(PosixFilePermission.OTHERS_READ);
            fullPermission.add(PosixFilePermission.OTHERS_WRITE);

            try {
                Files.createDirectories(path, PosixFilePermissions.asFileAttribute(fullPermission));
                outputLS(path);
                Files.setPosixFilePermissions(path, fullPermission);
                outputLS(path);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Could not create path : " + e.getLocalizedMessage());
            }
        } else {
            new File(properties.getFileStorageBasePath()).mkdir();
        }
    }

    private static void outputLS(Path path) throws IOException {
        System.out.println(new java.util.Scanner(Runtime.getRuntime().exec("ls -alt " + path.toAbsolutePath() + "/..").getInputStream()).useDelimiter("\\A").next());
    }

    @Bean
    public FileSystemResourceLoader fileSystemResourceLoader() {
        log.warn("File content absolute root path : {}", fileSystemRoot().getAbsolutePath());
        return new FileSystemResourceLoader(fileSystemRoot().getAbsolutePath());
    }

}
