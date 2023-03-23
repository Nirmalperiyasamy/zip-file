package com.hriday.zip.controller;

import com.hriday.zip.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;

@Slf4j
@RestController
public class FileSystemController {

    @Autowired
    protected FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<?> fileUpload(@RequestParam("file") MultipartFile file) throws IOException {
        log.info(file.getContentType());
        log.info(Arrays.toString(file.getBytes()));
        log.info(file.getInputStream().toString());
        log.info(String.valueOf(file.getResource()));
        String fileName = fileStorageService.storeFile(file);

        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/download/")
                .path(fileName)
                .toUriString();

        String contentType = file.getContentType();

        return ResponseEntity.ok().body(url);
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> download(@PathVariable String fileName, HttpServletRequest request) {

        Resource resource = fileStorageService.downloadFile(fileName);
        return ResponseEntity.ok().body(resource);

    }
}
