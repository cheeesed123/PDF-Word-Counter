package org.ChiefGuy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

public class PDF {
    private PdfWriter writer;
    private PdfDocument pdf;
    private Document document;
    public PDF(Path PDF) {
        try {
            this.writer = new PdfWriter(PDF.toFile());
            this.pdf = new PdfDocument(writer);
            this.document = new Document(pdf, PageSize.LETTER, true);
        } catch (FileNotFoundException e) {
            System.err.println("We weren't able to setup the PDF object.");
            System.exit(1);
        }
    }
    public void writeParagraph(WordManager words, PdfFont font, int fontSize) {
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
        try {
            PdfFont font = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
            writeParagraph(words, font, Constants.DefaultFontSize.value);
        } catch (IOException e) {
            System.err.println("Issue loading font family.");
        }
    }
    public void close() {
        document.close();
    }
}
