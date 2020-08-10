package com.example.zipper.service;

import com.sun.javaws.exceptions.InvalidArgumentException;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ZipServiceTest {
    private static ZipService zipService;
    private static Path zipPath;
    private static Path zipPath1;
    private static Path zipPath2;

    @BeforeClass
    public static void oneTimeSetup() throws IOException {
        zipService = new ZipService();
        zipPath = Paths.get("", "src", "test", "resources", "zip.zip");
        zipPath1 = Paths.get("", "src", "test", "resources", "zip1.zip");
        zipPath2 = Paths.get("", "src", "test", "resources", "zip2.zip");
    }

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void zipFile() throws IOException {
        String txt = tempFolder.newFile("txt.txt").getAbsolutePath();
        Files.write(Paths.get(txt), "121212".getBytes());
        zipService.zip(Arrays.asList(txt), zipPath.toAbsolutePath().toString());
        assertTrue(Files.exists(zipPath));
        byte[] bytes = new byte[128];
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipPath.toString()))) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            zipInputStream.read(bytes);
        }
        assertEquals(new String(bytes).trim(), "121212");
    }

    @Test
    public void zipEmptyDirectory() throws IOException {
        String dir1 = tempFolder.newFolder("dir1").getAbsolutePath();
        zipService.zip(Arrays.asList(dir1), zipPath1.toAbsolutePath().toString());
        assertTrue(Files.exists(zipPath1));
        ZipFile zipFile = new ZipFile(zipPath1.toString());
        long filesInZip = zipFile.stream().count();
        assertEquals(1, filesInZip);
    }

    @Test
    public void zipNotEmptyDirectory() throws IOException {
        String dir2 = tempFolder.newFolder("dir1", "dir2").getAbsolutePath();
        String txt2 = tempFolder.newFile("dir1" + File.separator + "dir2" + File.separator + "txt2.txt").getAbsolutePath();

        zipService.zip(Arrays.asList(dir2), zipPath2.toAbsolutePath().toString());
        assertTrue(Files.exists(zipPath2));
        ZipFile zipFile = new ZipFile(zipPath2.toString());
        long fileInZip = zipFile.stream().count();
        assertEquals(2, fileInZip);
    }

    @Test(expected = IOException.class)
    public void zipNonExistingFile() throws IOException {
        String txt = tempFolder.newFile("txt.txt").getAbsoluteFile().toString();
        Files.deleteIfExists(Paths.get(txt));
        zipService.zip(Arrays.asList(txt), zipPath.toAbsolutePath().toString());
    }

    @Test
    public void unzipZipWithFile() throws IOException, InvalidArgumentException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
            zipOutputStream.putNextEntry(new ZipEntry("aa.txt"));
            zipOutputStream.write("121212".getBytes());
        }
        String folder = tempFolder.newFolder("folder").getAbsolutePath();
        Files.deleteIfExists(Paths.get(folder + File.separator + "aa.txt"));

        zipService.unzip(zipPath.toAbsolutePath().toString(), folder);
        assertTrue(Files.exists(Paths.get(folder + File.separator + "aa.txt")));
        List<String> strings = Files.readAllLines(Paths.get(folder + File.separator + "aa.txt"));
        assertFalse(strings.isEmpty());
        assertEquals(strings.get(0), "121212");
    }

    @Test
    public void unzipZipWithEmptyDirectory() throws IOException, InvalidArgumentException {
        String dirName = "dir3";
        Path dir3 = tempFolder.newFolder(dirName).toPath();
        if (!Files.exists(dir3)) {
            Files.createDirectories(dir3);
        }
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
            ZipEntry zipEntry = new ZipEntry(dirName + File.separator);
            zipOutputStream.putNextEntry(zipEntry);
        }
        String folder = tempFolder.newFolder("folder").getAbsolutePath();
        Files.deleteIfExists(Paths.get(folder + File.separator + dirName));

        zipService.unzip(zipPath.toAbsolutePath().toString(), folder);
        assertTrue(Files.exists(Paths.get(folder + File.separator + dirName)));
        assertTrue(Files.isDirectory(Paths.get(folder + File.separator + dirName)));
    }

    @Test
    public void unzipZipWithNotEmptyDirectory() throws IOException, InvalidArgumentException {
        String dirName = "dir3";
        String fileName = "txt.txt";

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
            ZipEntry zipEntry = new ZipEntry(dirName + File.separator);
            zipOutputStream.putNextEntry(zipEntry);

            zipEntry = new ZipEntry(dirName + File.separator + fileName);
            zipOutputStream.putNextEntry(zipEntry);
        }
        String folder = tempFolder.newFolder("folder").getAbsolutePath();
        Files.deleteIfExists(Paths.get(folder + File.separator + dirName));

        zipService.unzip(zipPath.toAbsolutePath().toString(), folder);
        assertTrue(Files.exists(Paths.get(folder + File.separator + dirName)));
        assertTrue(Files.isDirectory(Paths.get(folder + File.separator + dirName)));
        assertTrue(Files.exists(Paths.get(folder + File.separator + dirName + File.separator + fileName)));
    }


    @Test(expected = InvalidArgumentException.class)
    public void unzipZipInvalidOutputLocation() throws IOException, InvalidArgumentException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
            ZipEntry zipEntry = new ZipEntry("abc");
            zipOutputStream.putNextEntry(zipEntry);
        }

        zipService.unzip(zipPath.toAbsolutePath().toString(), zipPath.toAbsolutePath().toString());
    }

    @Test(expected = InvalidArgumentException.class)
    public void unzipNonExistingZip() throws IOException, InvalidArgumentException {
        Files.deleteIfExists(zipPath);
        String folder = tempFolder.newFolder("folder").getAbsolutePath();
        zipService.unzip(zipPath.toAbsolutePath().toString(), folder);
    }

}