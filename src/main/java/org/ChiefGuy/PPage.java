package org.ChiefGuy;
//a record document representing a PDFBox document, paired with the possibility of being a pill.
//this is used by the image queue. The text stripper uses Files.
import org.apache.pdfbox.pdmodel.PDDocument;
public record PPage(PDDocument doc, boolean pill){};
