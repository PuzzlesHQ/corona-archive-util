package dev.puzzleshq.solarflare.carutil.io;

import dev.puzzleshq.solarflare.carutil.CoronaArchive;
import dev.puzzleshq.solarflare.carutil.CoronaArchiveEntry;
import dev.puzzleshq.solarflare.carutil.exception.InvalidCoronaArchiveException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CoronaArchiveReader {

    public static CoronaArchive fromFile(File file) throws InvalidCoronaArchiveException, IOException {
        FileInputStream stream = new FileInputStream(file);
        CoronaArchive coronaArchive = fromStream(stream);
        stream.close();

        return coronaArchive;
    }

    public static CoronaArchive fromBytes(byte[] bytes) throws InvalidCoronaArchiveException, IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        CoronaArchive coronaArchive = fromStream(byteArrayInputStream);
        byteArrayInputStream.close();

        return coronaArchive;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static CoronaArchive fromStream(InputStream stream) throws InvalidCoronaArchiveException, IOException {
        CoronaArchive coronaArchive = new CoronaArchive();

        DataInputStream dataInputStream = new DataInputStream(stream);

        int magic = dataInputStream.readInt();
        if (magic != CoronaArchive.MAGIC_NUMBER)
            throw new InvalidCoronaArchiveException("Incorrect file type. Must be a *.car (Corona Archive) file type.");

        int revision = Integer.reverseBytes((dataInputStream.readInt()));
        if (revision != 1)
            throw new InvalidCoronaArchiveException("This unpacker is intended for use on Corona Revision 1, it may not work on revision " + revision + ".");

        dataInputStream.skipBytes(4); // skip data offset

        int entryCount = Integer.reverseBytes((dataInputStream.readInt()));

        List<CoronaArchiveEntry> entries = new ArrayList<>();

        for (int i = 0; i < entryCount; i++) {
            CoronaArchiveEntry entry = new CoronaArchiveEntry();
            dataInputStream.skipBytes(8); // skip magic number & index
            int nameLen = Integer.reverseBytes((dataInputStream.readInt()));
            byte[] nameBytes = new byte[nameLen];
            dataInputStream.read(nameBytes, 0, nameLen);
            entry.setName(new String(nameBytes).replaceAll("ඞ", "/"));

            dataInputStream.skip(entry.getIndexPad());
            entries.add(entry);
        }

        for (CoronaArchiveEntry entry : entries) {
            dataInputStream.skip(8); // skip magic number & nxt
            int contentLength = Integer.reverseBytes(dataInputStream.readInt());
            byte[] contentBytes = new byte[contentLength];
            dataInputStream.read(contentBytes, 0, contentLength);
            entry.setContents(contentBytes);

            dataInputStream.skip(entry.getDataPad());

            coronaArchive.addEntry(entry);
        }

        dataInputStream.close();

        return coronaArchive;
    }

}
