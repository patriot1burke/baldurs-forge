package org.baldurs.archivist.LS;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import javax.xml.stream.XMLStreamException;

import org.baldurs.archivist.LS.Localization.LocaReader;
import org.baldurs.archivist.LS.Localization.LocaXmlWriter;
import org.baldurs.archivist.LS.Resources.LSF.LSFReader;
import org.baldurs.archivist.LS.Resources.LSX.LSXWriter;

public class Converter {
    public static void lsfToLsx(Path lsfPath) throws Exception {
        Path lsxPath = lsfPath.resolveSibling(lsfPath.getFileName().toString().replace(".lsf", ".lsx"));
        lsfToLsx(lsfPath, lsxPath);
    }

    public static void lsfToLsx(Path lsfPath, Path lsxPath)
            throws Exception {
        LSFReader reader = new LSFReader(lsfPath);
        LSXWriter writer = new LSXWriter(new FileOutputStream(lsxPath.toFile()));
        writer.write(reader.read());
    }

    public static void locaToXml(Path locaPath, Path xmlPath) throws Exception {
        LocaReader reader = new LocaReader(new FileInputStream(locaPath.toFile()));
        LocaXmlWriter writer = new LocaXmlWriter(new FileOutputStream(xmlPath.toFile()));
        writer.write(reader.read());
    }
}
