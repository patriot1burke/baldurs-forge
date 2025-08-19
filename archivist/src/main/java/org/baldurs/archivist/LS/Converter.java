package org.baldurs.archivist.LS;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import javax.xml.stream.XMLStreamException;

import org.baldurs.archivist.LS.Localization.LocaReader;
import org.baldurs.archivist.LS.Localization.LocaWriter;
import org.baldurs.archivist.LS.Localization.LocaXmlReader;
import org.baldurs.archivist.LS.Localization.LocaXmlWriter;
import org.baldurs.archivist.LS.Resources.LSF.LSFReader;
import org.baldurs.archivist.LS.Resources.LSF.LSFWriter;
import org.baldurs.archivist.LS.Resources.LSX.LSXReader;
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

    public static void lsxToLsf(Path lsxPath, Path lsfPath)
            throws Exception {
        LSXReader reader = new LSXReader(new FileInputStream(lsxPath.toFile()));
        LSFWriter writer = new LSFWriter(new FileOutputStream(lsfPath.toFile()));
        writer.write(reader.read());
    }

    public static void locaToXml(Path locaPath, Path xmlPath) throws Exception {
        LocaReader reader = new LocaReader(new FileInputStream(locaPath.toFile()));
        LocaXmlWriter writer = new LocaXmlWriter(new FileOutputStream(xmlPath.toFile()));
        writer.write(reader.read());
    }

    public static void xmlToLoca(Path xmlPath, Path locaPath) throws Exception {
        LocaXmlReader reader = new LocaXmlReader(new FileInputStream(xmlPath.toFile()));
        LocaWriter writer = new LocaWriter(new FileOutputStream(locaPath.toFile()));
        writer.write(reader.read());
    }
}
