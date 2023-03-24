package com.hriday.zip.service;

import com.hriday.zip.dao.ZipDetails;
import com.hriday.zip.repository.ZipRepo;
import com.hriday.zip.zip_generation.CreateZipFile;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Date;
import java.util.Objects;
import java.util.zip.ZipOutputStream;

@Service
public class FileStorageService {

    @Autowired
    protected ZipRepo zipRepo;

    private final Path fileStoragePath;
    private final String fileStorageLocation;

    private final Path tempStoragePath;
    private final String tempStorageLocation;

    public FileStorageService(@Value("${file.storage.location1}") String fileStorageLocation, @Value("${file.storage.location2}") String tempStorageLocation) {

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
        String zipName = fileName.replace("txt", "zip");
        String zipFileName = fileStoragePath + "\\" + zipName;
        File zipFile = new File(zipFileName);
        FileOutputStream fileOutputStream = new FileOutputStream(zipFile);

        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

        CreateZipFile.generatingZipFile(fileName, tempFilePath.toString(), zipOutputStream);

        zipOutputStream.close();

        StoreInDatabase(file.getSize(), zipFile.getName(), zipFile);


        return zipFile.getName();
    }

    public void StoreInDatabase(Long fileSize, String fileName, File zipFile) throws IOException {

        Path path = Paths.get(fileStorageLocation).toAbsolutePath().resolve(zipFile.getName());
        Resource resource = new UrlResource(path.toUri());

        ZipDetails zipDetails = new ZipDetails();
        zipDetails.setFileName(fileName);
        zipDetails.setStatus("Uploaded");
        zipDetails.setUploadedSize(fileSize);
        zipDetails.setCompressedSize(resource.contentLength());
        zipDetails.setUploadedTime(new Date(System.currentTimeMillis()).toString());
        zipRepo.save(zipDetails);
    }

    private File convertMultipartFileToFile(MultipartFile file) throws IOException {

        File convFile = File.createTempFile("temp", ".txt");
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();

        return convFile;
    }


    public Resource downloadFile(String fileName) throws IOException {

        Path path = Paths.get(fileStorageLocation).toAbsolutePath().resolve(String.valueOf(fileName));

        Resource resource;
        try {
            resource = new UrlResource(path.toUri());

        } catch (MalformedURLException e) {
            throw new RuntimeException("Issue in reading the file", e);
        }

        if (resource.exists() && resource.isReadable()) {

            ZipDetails zipDetails = zipRepo.findByFileName(fileName);
            zipDetails.setStatus("Downloaded");
            zipDetails.setDownloadedTime(new Date(System.currentTimeMillis()).toString());
            zipRepo.save(zipDetails);

            return resource;
        } else {
            throw new RuntimeException("the file doesn't exist or not readable");
        }
    }
}
