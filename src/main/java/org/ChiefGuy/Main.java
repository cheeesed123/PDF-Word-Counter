package org.ChiefGuy;
import java.nio.file.Path;
import java.nio.file.Paths;
public class Main {
    final private static Path CSV = Paths.get("CSV", "wordAmounts.csv");
    public static void main(String[] args) {
        final int[] pageOps = new int[]{50, 100, 500, 1000, 5000, 50000};
        StringBuilder name = new StringBuilder();
        //dont use total in Word[], only one in tree.
        //at long last we can do the actual PDF!
        for (int numPages : pageOps) {
            WordManager words = new WordManager(CSV);
            name.append("testPDF for ")
                .append(numPages)
                .append(".pdf");
            Path PDF = Paths.get("PDF", name.toString());
            name.delete(0, name.length());
            PDF pdf = new PDF(PDF);
            while (pdf.getPageNo() < numPages) {
                pdf.writeParagraph(words);
            }
            pdf.close();
        }
        
    }
    
    //TOC:
    //Finish this for benchmarks
    //Finish ReadMe
    //Put files on Github with .zip for source code
    //this project can prob be in there too but only shortly mentioned.
    //Now, with all code then done, begin work on imminent scary homework.
}
