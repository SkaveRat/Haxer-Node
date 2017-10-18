package net.skaverat.haxlernode.util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.zip.GZIPInputStream;

public class Archiver {

    private final static Logger logger = LogManager.getLogger(Archiver.class);


    //https://java-tweets.blogspot.de/2012/07/untar-targz-file-with-apache-commons.html
    public static void decompressFile(File inputFile) {
        int BUFFER = 2048;
        TarArchiveInputStream tarInput = null;
        TarArchiveEntry currentEntry = null;
        TarArchiveEntry entry = null;
        try {
            tarInput = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(inputFile)));

            while ((entry = (TarArchiveEntry) tarInput.getNextEntry()) != null) {
                System.out.println("Extracting: " + entry.getName());
                /** If the entry is a directory, create the directory. **/
                if (entry.isDirectory()) {

                    File f = new File(entry.getName());
                    f.mkdirs();
                }
                /**
                 * If the entry is a file,write the decompressed file to the disk
                 * and close destination stream.
                 **/
                else {
                    int count;
                    byte data[] = new byte[BUFFER];

                    FileOutputStream fos = new FileOutputStream(entry.getName());
                    BufferedOutputStream dest = new BufferedOutputStream(fos,
                            BUFFER);
                    while ((count = tarInput.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.close();
                }
            }
        /** Close the input stream **/
        tarInput.close();

        } catch (IOException e) {
            logger.error(e);
        }
    }
}
