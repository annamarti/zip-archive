package com.example.zipper.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipService {

    /**
     * Zips the contents of the sources into the zip file.
     *
     * @param sources the absolute path of directories and files  to zip
     * @param zipPath the path for zip
     */
    public void zip(List<String> sources, String zipPath) throws IOException {
        Files.deleteIfExists(Paths.get(zipPath));
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath))) {
            for (String src : sources) {
                if (Files.exists(Paths.get(src))) {
                    String zipEntryName;
                    if (src.contains(File.separator)) {
                        int index = src.lastIndexOf(File.separator);
                        zipEntryName = src.substring(index + 1);
                    } else {
                        zipEntryName = src;
                    }
                    addFileToZip(zos, src, zipEntryName);
                } else {
                    throw new FileNotFoundException(String.format("File %s does not exist", src));
                }
            }
        } catch (IOException e) {
            Files.deleteIfExists(Paths.get(zipPath));
            throw e;
        }
    }

    private void addFileToZip(ZipOutputStream zipOutputStream, String src, String zipEntryName) throws IOException {
        Path path = Paths.get(src);
        if (Files.isDirectory(path)) {
            zipOutputStream.putNextEntry(new ZipEntry(zipEntryName + File.separator));
            DirectoryStream<Path> subFiles = Files.newDirectoryStream(path);
            for (Path subFile : subFiles) {
                addFileToZip(zipOutputStream, subFile.toAbsolutePath().toString(), zipEntryName + File.separator + subFile.getFileName());
            }
        } else {
            try (FileInputStream fis = new FileInputStream(path.toFile())) {
                ZipEntry zipEntry = new ZipEntry(zipEntryName);
                zipOutputStream.putNextEntry(zipEntry);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zipOutputStream.write(bytes, 0, length);
                }
            }
        }
    }

    public void unzip(String zipLocation, String unzipLocation) throws IOException {
        Path zipPath = Paths.get(zipLocation);
        Path unzipPath = Paths.get(unzipLocation);
        if (!(Files.exists(zipPath))) {
            throw new FileNotFoundException(String.format("Zip file %s does not exist", zipPath.toAbsolutePath()));
        }
        if (!Files.exists(unzipPath)) {
            Files.createDirectories(unzipPath);
        }
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipPath.toFile()))) {
            ZipEntry entry = zipInputStream.getNextEntry();
            while (entry != null) {
                Path filePath = unzipPath.resolve(Paths.get(entry.getName())).toAbsolutePath();
                if (!entry.getName().endsWith(File.separator)) {
                    unzipFile(zipInputStream, filePath);
                } else {
                    deleteIfExist(filePath.toAbsolutePath());
                    Files.createDirectories(filePath);
                }
                zipInputStream.closeEntry();
                entry = zipInputStream.getNextEntry();
            }
        }
    }

    private void unzipFile(ZipInputStream zipInputStream, Path toUnzip) throws IOException {
        Files.deleteIfExists(toUnzip);
        Files.createFile(toUnzip);
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(toUnzip.toAbsolutePath().toString()))) {
            byte[] bytesIn = new byte[1024];
            int read = 0;
            while ((read = zipInputStream.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

    public void deleteIfExist(Path pathToBeDeleted) throws IOException {
        if (Files.exists(pathToBeDeleted)) {
            if (Files.isDirectory(pathToBeDeleted)) {
                try (Stream<Path> stream = Files.list(pathToBeDeleted)) {
                    List<Path> pathList = stream.collect(Collectors.toList());
                    for (Path path : pathList) {
                        deleteIfExist(path);
                    }
                }
            } else {
                Files.delete(pathToBeDeleted);
            }
        }
    }
}
