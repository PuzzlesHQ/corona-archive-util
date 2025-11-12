package dev.puzzleshq.solarflare.carutil.io;

import dev.puzzleshq.solarflare.carutil.CoronaArchive;
import dev.puzzleshq.solarflare.carutil.CoronaArchiveEntry;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CoronaArchiveZipConverter {

    public static void toZip(CoronaArchive archive, File out) throws IOException {
        byte[] bytes = CoronaArchiveZipConverter.toZipBytes(archive);

        FileOutputStream outputStream = new FileOutputStream(out);
        outputStream.write(bytes);
        outputStream.close();
    }

    public static byte[] toZipBytes(CoronaArchive archive) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

        for (CoronaArchiveEntry entry : archive.getEntries()) {
            ZipEntry zipEntry = new ZipEntry(entry.getName());
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(entry.getContents());
            zipOutputStream.closeEntry();
        }

        zipOutputStream.close();
        byte[] bytes = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();

        return bytes;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static CoronaArchive fromZipBytes(byte[] bytes) throws IOException {
        CoronaArchive archive = new CoronaArchive();

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream);

        ZipEntry zipEntry;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            if (zipEntry.isDirectory()) continue;

            byte[] contents = new byte[Math.toIntExact(zipEntry.getSize())];
            zipInputStream.read(contents, 0, contents.length);

            CoronaArchiveEntry entry = new CoronaArchiveEntry();
            entry.setName(zipEntry.getName());
            entry.setContents(contents);

            archive.addEntry(entry);
        }

        zipInputStream.close();
        byteArrayInputStream.close();

        return archive;
    }

    public static CoronaArchive fromZip(ZipFile zipFile) throws IOException {
        Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

        CoronaArchive archive = new CoronaArchive();

        while (zipEntries.hasMoreElements()) {
            ZipEntry zipEntry = zipEntries.nextElement();

            if (zipEntry.isDirectory()) continue;

            byte[] contents = new byte[Math.toIntExact(zipEntry.getSize())];
            InputStream stream = zipFile.getInputStream(zipEntry);
            DataInputStream dataInputStream = new DataInputStream(stream);
            dataInputStream.readFully(contents);
            dataInputStream.close();
            stream.close();

            CoronaArchiveEntry entry = new CoronaArchiveEntry();
            entry.setName(zipEntry.getName());
            entry.setContents(contents);

            archive.addEntry(entry);
        }

        return archive;
    }

}
