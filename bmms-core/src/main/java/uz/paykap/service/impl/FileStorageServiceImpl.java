package uz.paykap.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import uz.devops.bmcore.util.FileStorageService;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Logger log = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    private final Path fileStorageLocation = Paths.get("." + File.separator + "uploads");

    public FileStorageServiceImpl() {
//        this.fileStorageLocation = Paths.get(fileStorageProperties.getPaynet().getUploadDir()).toAbsolutePath().normalize();

//        try {
//            Files.createDirectories(this.fileStorageLocation);
//        } catch (Exception ex) {
//            throw new CodedException(ProblemEnum.INTERNAL_ERROR);
//        }
    }

    public String storeFile(byte[] file, String fileName) {
        // Normalize file name

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new CodedException("Sorry! Filename contains invalid path sequence " + fileName, fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(new ByteArrayInputStream(file), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            log.warn(ex.getLocalizedMessage());
//            throw new CodedException("Could not store file " + fileName + ". Please try again!", fileName);
            return "no-logo.png";
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new CodedException(ProblemEnum.OBJECT_NOT_FOUND);
            }
        } catch (MalformedURLException ex) {
            throw new CodedException(ProblemEnum.OBJECT_NOT_FOUND);
        }
    }
}
