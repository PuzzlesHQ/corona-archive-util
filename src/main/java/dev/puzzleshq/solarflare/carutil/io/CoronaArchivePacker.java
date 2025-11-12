package dev.puzzleshq.solarflare.carutil.io;

import dev.puzzleshq.solarflare.carutil.CoronaArchive;
import dev.puzzleshq.solarflare.carutil.CoronaArchiveEntry;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class CoronaArchivePacker {

    private static byte[] writeDataSegments(CoronaArchive archive) throws IOException {
        ByteArrayOutputStream dataSegmentByteStream = new ByteArrayOutputStream();
        DataOutputStream dataSegmentDataStream = new DataOutputStream(dataSegmentByteStream);

        /* ********************************************************************** */

        for (CoronaArchiveEntry entry : archive.getEntries()) {
            int nxt = entry.getNxt();
            int contentLength = entry.getContents().length;

            dataSegmentDataStream.writeInt(Integer.reverseBytes(CoronaArchive.MAGIC_NUMBER_DATA));
            dataSegmentDataStream.writeInt(Integer.reverseBytes(nxt));
            dataSegmentDataStream.writeInt(Integer.reverseBytes(contentLength));
            dataSegmentDataStream.write(entry.getContents());
            for (int i = 0; i < entry.getDataPad(); i++) dataSegmentByteStream.write(0);
        }

        /* ********************************************************************** */

        byte[] bytes = dataSegmentByteStream.toByteArray();

        dataSegmentDataStream.close();
        dataSegmentByteStream.close();

        return bytes;
    }

    private static int getIndexSegmentSize(CoronaArchive archive, Map<String, Integer> idxMap) {
        int idx = 0;
        int size = 0;

        for (CoronaArchiveEntry entry : archive.getEntries()) {
            int contentLength = entry.getContents().length;

            idxMap.put(entry.getName(), CoronaArchive.HEADER_SIZE + idx);
            idx += /* MAGIC_NUMBER_DATA SIZE */ 4 +
                    /* NXT SIZE */ + 4 +
                    /* FILE_LENGTH SIZE */ + 4 +
                    /* FILE_LENGTH */ contentLength +
                    /* PADDING */ + entry.getDataPad();

            size += /* MAGIC_NUMBER_INDEX SIZE */ 4 +
                    /* OFFSET SIZE */ + 4 +
                    /* FILE_NAME LENGTH */ + 4 +
                    /* FILE_NAME SIZE */ entry.getCoronaNameBytes().length +
                    /* PADDING */ + entry.getIndexPad();
        }

        return size;
    }

    private static byte[] writeIndexSegments(CoronaArchive archive, int indexSegmentSize, Map<String, Integer> idxMap) throws IOException {
        ByteArrayOutputStream indexSegmentByteStream = new ByteArrayOutputStream();
        DataOutputStream indexSegmentDataStream = new DataOutputStream(indexSegmentByteStream);

        /* ********************************************************************** */

        for (CoronaArchiveEntry entry : archive.getEntries()) {
            indexSegmentDataStream.writeInt(Integer.reverseBytes(CoronaArchive.MAGIC_NUMBER_INDEX));
            indexSegmentDataStream.writeInt(Integer.reverseBytes(idxMap.get(entry.getName()) + indexSegmentSize));
            indexSegmentDataStream.writeInt(Integer.reverseBytes(entry.getCoronaNameBytes().length));
            indexSegmentDataStream.write(entry.getCoronaNameBytes());
            for (int i = 0; i < entry.getIndexPad(); i++) indexSegmentByteStream.write(0);
        }

        /* ********************************************************************** */

        byte[] bytes = indexSegmentByteStream.toByteArray();

        indexSegmentDataStream.close();
        indexSegmentByteStream.close();

        return bytes;
    }

    public static void pack(CoronaArchive archive, File outputCar) throws IOException {
        ByteArrayOutputStream archiveByteStream = new ByteArrayOutputStream();
        DataOutputStream archiveDataStream = new DataOutputStream(archiveByteStream);

        Map<String, Integer> indexMap = new HashMap<>();

        int indexSegmentSize = getIndexSegmentSize(archive, indexMap);

        archiveDataStream.writeInt(CoronaArchive.MAGIC_NUMBER);
        archiveDataStream.writeInt(Integer.reverseBytes(CoronaArchive.FORMAT_VERSION));
        archiveDataStream.writeInt(Integer.reverseBytes(indexSegmentSize));
        archiveDataStream.writeInt(Integer.reverseBytes(archive.getEntryCount()));

        byte[] indexBytes = writeIndexSegments(archive, indexSegmentSize, indexMap);
        byte[] dataBytes = writeDataSegments(archive);

        archiveDataStream.write(indexBytes);
        archiveDataStream.write(dataBytes);

        archiveDataStream.writeInt(CoronaArchive.MAGIC_END);
        archiveDataStream.writeInt(0);

        Files.write(outputCar.toPath(), archiveByteStream.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        archiveDataStream.close();
        archiveByteStream.close();
    }

}
