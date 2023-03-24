package com.hriday.zip.zip_generation;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class CreateZipFile {

    public static void generatingZipFile(String fileName, String path, ZipOutputStream zipOutputStream) throws FileNotFoundException {

        final int buffer = 1024;

        BufferedInputStream bufferedInputStream = null;

        try {
            File file = new File(path);

            FileInputStream fileInputStream = new FileInputStream(file);
            bufferedInputStream = new BufferedInputStream(fileInputStream, buffer);


            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOutputStream.putNextEntry(zipEntry);
            byte[] data = new byte[buffer];
            System.out.println(zipEntry.getSize());
            int count;

            while ((count = bufferedInputStream.read(data, 0, buffer)) != -1) {

                zipOutputStream.write(data, 0, count);

            }
            zipOutputStream.closeEntry();

        } catch (IOException e) {
            System.out.println("Error while zipping" + e.getMessage());
        }


    }
}
