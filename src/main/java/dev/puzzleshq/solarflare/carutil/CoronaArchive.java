package dev.puzzleshq.solarflare.carutil;

import dev.puzzleshq.solarflare.carutil.io.CoronaArchivePacker;
import dev.puzzleshq.solarflare.carutil.io.CoronaArchiveReader;

import java.io.*;
import java.util.*;

public class CoronaArchive {

    public static final int MAGIC_NUMBER = 0x72616301;
    public static final int MAGIC_END = 0xFFFFFFFF;
    public static final int MAGIC_NUMBER_INDEX = 1;
    public static final int MAGIC_NUMBER_DATA = 2;

    public static final int HEADER_SIZE = 16;

    public static final int FORMAT_VERSION = 1;

    private final Map<String, CoronaArchiveEntry> entryMap;
    private final List<CoronaArchiveEntry> entries;

    public CoronaArchive() {
        this.entries = new ArrayList<>();
        this.entryMap = new HashMap<>();
    }

    public void extractTo(File dir) throws IOException {
        for (CoronaArchiveEntry entry : this.getEntries()) {
            File file = new File(dir, entry.getName());
            file.getParentFile().mkdirs();

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(entry.getContents());
            fileOutputStream.close();
        }
    }

    public void exportAs(File file) throws IOException {
        CoronaArchivePacker.pack(this, file);
    }

    public static CoronaArchive importFrom(File file) throws IOException {
        return CoronaArchiveReader.fromFile(file);
    }

    public void addFileOrDir(File file) throws IOException {
        if (!file.exists())
            throw new IOException("File or Directory does not exist! -> \"" + file.getAbsolutePath() + "\"");

        Queue<Object[]> childFiles = new ArrayDeque<>();
        childFiles.add(new Object[]{"", file});
        while (!childFiles.isEmpty()) {
            Object[] childFile = childFiles.poll();
            String parent = ((String)childFile[0]);
            File cFile = ((File) childFile[1]);

            if (cFile.isDirectory()) {
                for (File listFile : Objects.requireNonNull(cFile.listFiles())) {
                    childFiles.add(new Object[]{parent + "/" + cFile.getName(), listFile});
                }
                continue;
            }

            FileInputStream fileInputStream = new FileInputStream(file);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);

            byte[] bytes = new byte[fileInputStream.available()];
            dataInputStream.readFully(bytes, 0, bytes.length);

            dataInputStream.close();
            fileInputStream.close();

            CoronaArchiveEntry entry = new CoronaArchiveEntry();
            entry.setName(parent + "/" + cFile.getName());
            entry.setContents(bytes);

            this.addEntry(entry);
            return;
        }
    }

    public void addEntry(CoronaArchiveEntry entry) {
        if (entry.getName() == null || entry.getContents() == null)
            throw new RuntimeException("Cannot add null entry to CoronaArchive!");

        this.entries.add(entry);
        this.entryMap.put(entry.getName(), entry);
    }

    public CoronaArchiveEntry getEntry(String name) {
        return this.entryMap.get(name);
    }

    public void removeEntry(String name) {
        CoronaArchiveEntry entry = this.entryMap.remove(name);
        this.entries.remove(entry);
    }

    public Collection<CoronaArchiveEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public int getEntryCount() {
        return this.entries.size();
    }
}
