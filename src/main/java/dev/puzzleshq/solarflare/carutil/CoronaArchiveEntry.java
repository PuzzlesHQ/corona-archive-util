package dev.puzzleshq.solarflare.carutil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CoronaArchiveEntry {

    private String name;
    private byte[] contents;
    private int indexPad;
    private int dataPad;
    private int nxt;
    private byte[] bytes;

    public String getName() {
        return name;
    }

    public byte[] getCoronaNameBytes() {
        return this.bytes;
    }

    public byte[] getContents() {
        return contents;
    }

    public String asString() {
        return new String(contents);
    }

    public void export(File file) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(contents);
        fileOutputStream.close();
    }

    private static int getDataPaddingLength(int baseLength) {
        int basePad = getIndexPaddingLength(baseLength);
        return basePad < 4 ? basePad : 0;
    }

    private static int getIndexPaddingLength(int baseLength) {
        return 4 - (baseLength % 4);
    }

    public int getNxt() {
        return this.nxt;
    }

    public void setName(String name) {
        this.name = name;
        this.indexPad = getIndexPaddingLength(name.length());
        this.bytes = name.replaceAll("/", "ඞ").getBytes(StandardCharsets.UTF_8);
    }

    public void setContents(byte[] contents) {
        this.contents = contents;
        this.dataPad = getDataPaddingLength(contents.length);
        this.nxt = contents.length + 4 + this.dataPad;
    }

    public int getIndexPad() {
        return indexPad;
    }

    public int getDataPad() {
        return dataPad;
    }

    @Override
    public String toString() {
        return "{ Name: \"" + name + "\", Size: " + contents.length + " }";
    }

}
