package com.hriday.zip.controller;

import com.hriday.zip.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@RestController
public class FileSystemController {

    @Autowired
    protected FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<?> fileUpload(@RequestParam("file") MultipartFile file) throws IOException {

        String fileName = fileStorageService.storeFile(file);

        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/download/")
                .path(fileName)
                .toUriString();

        String contentType = file.getContentType();

        return ResponseEntity.ok().body(url);
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<?> download(@PathVariable String fileName, HttpServletResponse response) throws IOException {

        Resource resource = fileStorageService.downloadFile(fileName);

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {

            ZipEntry zipEntry = new ZipEntry(Objects.requireNonNull(resource.getFilename()));

            try {
                zipEntry.setSize(resource.contentLength());
                zos.putNextEntry(zipEntry);

                StreamUtils.copy(resource.getInputStream(), zos);

                zos.closeEntry();

            } catch (IOException e) {
                System.out.println("some exception while ziping");
            }

            zos.finish();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok().body("zip file downloading");

    }
}
