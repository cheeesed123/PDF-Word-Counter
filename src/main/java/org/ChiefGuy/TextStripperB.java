package org.ChiefGuy;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

import org.apache.pdfbox.text.PDFTextStripper;

public class TextStripperB extends PDFTextStripper {
    final private StringBuilder paragraphBuilder = new StringBuilder();
    private int pageCount;
    private BlockingQueue<PString> queue;
    //constructor creates threads and sets up properties for lemmatization.
    public TextStripperB() {
        super();
        /*
        1. Seperate into words
        2. Seperate into sentences
        3. Get the tense
        4. Get the lemma tense (dict tense)
        */
    }
    public TextStripperB(int pageCount, BlockingQueue<PString> queue) {
        this.pageCount = pageCount;
        this.queue = queue;
        this();
    }
    //writeString()
    static int currentPage = 0, lastPage = -1;
    @Override
    protected void writeString(String text) throws IOException {
        //add a line to the queue, update the progress bar, then call collector.
        paragraphBuilder.append(text);
        currentPage = this.getCurrentPageNo();
        if (currentPage != lastPage)
            progress(currentPage);
        lastPage = currentPage;
        collector();
    }
    final private static DecimalFormat formatter = new DecimalFormat("0.00%");
    private void progress(int currentPage) {
        System.out.println(formatter.format((double) currentPage / pageCount) + " done!");
    }
    //is called each time text is added by writeString(), if the text is enough words it adds it to blockingQueue and clears builder.
    private void collector() {
        try {
            //if theres 50 words, get a chunk, and put it into the queue.
            if (countWords(paragraphBuilder) >= 50) {
                String chunk = extractChunk(paragraphBuilder, 50);
                if (!chunk.isBlank()) {
                    queue.put(new PString(chunk, false));
                }
            }
        } catch (InterruptedException e) {
            Main.log("Something went wrong with adding to the blockingQueue!", e);
        }
    }

    static String extractChunk(StringBuilder builder, int wordThreshold) {
        String text = builder.toString().trim();
        if (text.isEmpty()) {
            builder.delete(0, builder.length());
            return "";
        }
        //get text, if its blank return blank, else return the amount of words
        //word count gotten from the split length
        //if its less than 50, just return the text.
        String[] words = text.split("\\s+");
        if (words.length <= wordThreshold) {
            builder.delete(0, builder.length());
            return text;
        }
        //else if its more, return the chunk of 50, then put the remainder back into the String Builder.
        String chunk = String.join(" ", Arrays.copyOf(words, wordThreshold));
        String remainder = String.join(" ", Arrays.copyOfRange(words, wordThreshold, words.length));
        builder.delete(0, builder.length());
        builder.append(remainder);
        return chunk;
    }

    public void flushPendingText() {
        //if theres any text IN the paragraphBuilder when this is called, which is at the end of a PDF, then put it in as a chunk.
        String pending = paragraphBuilder.toString().trim();
        if (!pending.isBlank()) {
            try {
                queue.put(new PString(pending, false));
            } catch (InterruptedException e) {
                Main.log("Something went wrong with flushing the final text chunk!", e);
            }
            paragraphBuilder.delete(0, paragraphBuilder.length());
        }
    }

    private int countWords(StringBuilder b) {
        //returns the count of words by length of the split of the text
        //\\s+ means all whitespace, and if its many like "  ", count it as one.
        String text = b.toString().trim();
        if (text.isEmpty()) {
            return 0;
        }
        return text.split("\\s+").length;
    }
}
