package org.ChiefGuy;

import java.io.IOException;
import java.nio.file.Path;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

public class PDF {
    private PdfWriter writer;
    private PdfDocument pdf;
    private Document document;
    private final PdfFont font;

    public PDF(Path PDF) {
        try {
            WriterProperties writerProperties = new WriterProperties()
                .setFullCompressionMode(true)
                .setCompressionLevel(9)
                .useSmartMode();
            this.writer = new PdfWriter(PDF.toString(), writerProperties);
            this.pdf = new PdfDocument(writer);
            this.pdf.setFlushUnusedObjects(true);
            this.document = new Document(pdf, PageSize.LETTER, true);
            this.font = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
        } catch (IOException e) {
            System.err.println("We weren't able to setup the PDF object.");
            System.exit(1);
            throw new IllegalStateException(e);
        }
    }
    public void writeParagraph(WordManager words, int fontSize) {
        System.out.println("New paragraph!");
        Paragraph para = new Paragraph(
            words.getWords(
                Constants.ParagraphSize.value
            ))
            .setFont(font)
            .setFontSize(fontSize)
            .setFirstLineIndent(Constants.DefaultFirstLineIndent.value)
            .setMarginTop(Constants.DefaultMarginTop.value)
            .setMarginBottom(Constants.DefaultMarginBottom.value)
            .setMultipliedLeading(1.0f);
        document.add(para);
    }
    public int getPageNo() {
        return pdf.getNumberOfPages();
    }
    public void writeParagraph(WordManager words) {
        writeParagraph(words, Constants.DefaultFontSize.value);
    }
    public void close() {
        document.close();
    }
}
