package com.hriday.zip.service;

import com.hriday.zip.zip_generation.CreateZipFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.zip.ZipOutputStream;

@Service
public class FileStorageService {

    private final Path fileStoragePath;
    private final String fileStorageLocation;

    private final Path tempStoragePath;
    private final String tempStorageLocation;

    public FileStorageService(@Value("${file.storage.location1}") String fileStorageLocation,
                              @Value("${file.storage.location2}") String tempStorageLocation) {

        this.fileStorageLocation = fileStorageLocation;
        fileStoragePath = Paths.get(fileStorageLocation).toAbsolutePath().normalize();

        this.tempStorageLocation = tempStorageLocation;
        tempStoragePath = Paths.get(tempStorageLocation).toAbsolutePath().normalize();

        try {
            Files.createDirectories(fileStoragePath);
            Files.createDirectories(tempStoragePath);
        } catch (IOException e) {
            throw new RuntimeException("Issue in creating file directory");
        }
    }


    public String storeFile(MultipartFile file) throws IOException {

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        File tempFile = convertMultipartFileToFile(file);


        Path tempFilePath = Paths.get(tempStoragePath + "\\" + tempFile.getName());

        try {
            Files.copy(file.getInputStream(), tempFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Issue in storing the file", e);
        }
        String zipFileName = fileStoragePath + "\\" + "demo.zip";
        File zipFile = new File(zipFileName);

        System.out.println(zipFile.getName());

        FileOutputStream fileOutputStream = new FileOutputStream(zipFile);

        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

        CreateZipFile.generatingZipFile(fileName, tempFilePath.toString(), zipOutputStream);

        zipOutputStream.close();

        return zipFile.getName();
    }

    private File convertMultipartFileToFile(MultipartFile file) throws IOException {
        File convFile = File.createTempFile("temp", ".txt");
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    public Resource downloadFile(String fileName) {

        Path path = Paths.get(fileStorageLocation).toAbsolutePath().resolve(fileName);

        Resource resource;
        try {
            resource = new UrlResource(path.toUri());
            System.out.println(resource);

        } catch (MalformedURLException e) {
            throw new RuntimeException("Issue in reading the file", e);
        }

        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("the file doesn't exist or not readable");
        }
    }
}
