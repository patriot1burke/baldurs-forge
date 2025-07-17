package org.baldurs.forge.scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.nio.file.Path;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.io.IOException;

public record ModuleInfo(String name, String folder) {

    public static Path findFirstDirectory(Path directory) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    return entry;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan directory: " + directory, e);
        }
        return null; // Return null if no directory is found
    }

    public static ModuleInfo scan(Path mod) throws Exception {
        Path metaDir = findFirstDirectory(mod.resolve("Mods"));
        Path metaFile = metaDir.resolve("meta.lsx");
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(metaFile.toFile());

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        String xpathExpression = "//save/region[@id=\"Config\"]/node[@id=\"root\"]/children/node[@id=\"ModuleInfo\"]";
        NodeList nodeList = (NodeList) xPath.evaluate(xpathExpression, document, XPathConstants.NODESET);
        if (nodeList.getLength() == 0) {
            return null;
        }
        Node node = nodeList.item(0);
        Element element = (Element) node;
        List<Element> attributes = RootTemplateArchive.getAttributeElements(element);
        String name = null;
        String folder = null;
        for (Element attribute : attributes) {
            String attributeName = attribute.getAttribute("id");
            String attributeValue = attribute.getAttribute("value");
            if (attributeName.equals("Name")) {
                name = attributeValue;
            } else if (attributeName.equals("Folder")) {
                folder = attributeValue;
            }
        }
        return new ModuleInfo(name, folder);
    }
}
