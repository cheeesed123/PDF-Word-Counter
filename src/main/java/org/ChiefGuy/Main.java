package org.ChiefGuy;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class Main {
    //paths
    final private static Path PDFPath = Paths.get("PDF"); //the pdf, this system will be changed to cycle through PDFs in a folder.
    final private static Path ImagePath = Paths.get("Images"); //the images directory
    @SuppressWarnings("FieldMayBeFinal")
    private static String CSVPath = "wordAmounts.csv"; //the csv that will have the word data.
    @SuppressWarnings("FieldMayBeFinal")
    private static String Logs = "logs.yaml"; //the logs yaml for errors.
    final private static BlockingQueue<Log> logQueue = new LinkedBlockingQueue<>(30); //the logQueue for logs
    final private static BlockingQueue<PPage> imageQueue = new LinkedBlockingQueue<>(5);
    //main, manages the files to ensure existence and correct startup state, and calls other methods.
    public static void main(String[] args) throws InvalidArgumentException {
        final BlockingQueue<PString> queue = new LinkedBlockingQueue<>(100);
        final int threadCount = Runtime.getRuntime().availableProcessors();
        //reset path contents for csv, logs, and images.
        try {
            Files.createDirectories(PDFPath);
        } catch (IOException e) {
            log("Issue ensuring PDF.", e);
        }
        try {
            Files.deleteIfExists(getCSVPath());
            Files.write(getCSVPath(), "Word,Count\n".getBytes(), StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            log("Issue making file: " + getCSVPath().toString(), e);
            System.exit(1);
        }
        try {
            if (Files.exists(ImagePath, LinkOption.NOFOLLOW_LINKS)) {
                try (Stream<Path> paths = Files.walk(ImagePath)) {
                    paths.forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log("Issue deleting file in Image path.", e);
                        }
                    });
                }
                Files.delete(ImagePath);
            }
            Files.createDirectory(ImagePath);
        } catch (DirectoryNotEmptyException e) {
            log("The directory is not empty, so cannot be deleted.", e);
            System.exit(1);
        } catch (IOException e) {
            log("Issue making file: " + ImagePath.toString(), e);
            System.exit(1);
        }
        try {
            Files.deleteIfExists(getLogs());
            Files.write(getLogs(), "Logs: # \"None\" represents an empty field for sections like thread num or iteration num\n".getBytes(), StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            log("Issue making file: " + getLogs().toString(), e);
            System.exit(1);
        }
        System.out.println("This program will take a PDF and return the most used words, and an extraction of the text and images.\nMade with love by Chief Guy, 2026.\n");
        //get a File[] of PDFs.
        final FileFilter PDFileFilter = (file) -> file.getName().endsWith(".pdf");
        final File[] pdfs = PDFPath.toFile().listFiles(PDFileFilter);
        //initialization of lemmatizers and threads.
        if (pdfs.length == 0) {
            System.err.println("There was an error trying to run the pdf for loop. Perhaps its empty?");
            System.exit(0);
        }
        final Lemmatizer lemma = new Lemmatizer(queue);
        final Thread[] threads = new Thread[threadCount - 1];
        for (int i = 0; i < threads.length; i++) {
            final int temp = i + 1;
                threads[i] = new Thread(() -> {
                    try {
                        lemma.lemmatizer(temp);
                    } catch (InterruptedException e) {
                        Main.log("Issue starting up lemmatizer thread " + temp + ":(, fatal error!", e);
                        System.exit(1);
                    }
                });
        }
        for (Thread current : threads)
            current.start();
        //logger thread
        Thread logThread = new Thread(() -> {
            try {
                logWriter(threadCount + 1);
            } catch (InterruptedException e) {
                System.err.println("The logThread failed, or it failed trying to take a log from the logQueue! Therefore cannot write logs. Fatal error!");
                while (!logQueue.isEmpty()) {
                    try {
                        System.err.println(logQueue.take());
                    } catch (InterruptedException f) {
                        System.err.println("Issue trying to dismiss logQueue! Fatal error!");
                        System.exit(1);
                    } finally {
                        System.exit(1);
                    }
                }
            }
        });
        //image thread
        Thread images = new Thread(() -> {
            try {
                getImages(threadCount);
            } catch (InterruptedException e) {
                log("Images thread fatal error!", e);
                System.exit(1);
            }
            });
        images.start();
        logThread.start();
        //cycle through pdfs.
        for (File currentPDF : pdfs) {
            //load PDF with PDFBOX
            System.out.println("Currently working on \"" + currentPDF.getName() + "\"");
            try {
                final byte[] bytePath = Files.readAllBytes(currentPDF.toPath());
                @SuppressWarnings("ConvertToTryWithResources")
                final PDDocument PSDoc = Loader.loadPDF(bytePath);
                final PDDocument ImageDoc = Loader.loadPDF(bytePath);
                imageQueue.put(new PPage(ImageDoc, false));
                //strip text with custom stripper
                TextStripperB a = new TextStripperB(PSDoc.getNumberOfPages(), queue);
                a.getText(PSDoc);
                a.flushPendingText();
                PSDoc.close();
            } catch (InterruptedException e) {
                log("There was an issue putting a doc into the image queue", e);
            } catch (IOException e) {
                log("Error trying to load PDF! :(", e);
            }
        }
        //lemmatizers kill
        kill(threads, queue);
        try {
            //images kill
            imageQueue.put(new PPage(new PDDocument(), true));
            for (Thread t : threads)
                t.join();
            images.join();
            //logs kill
            logQueue.put(
                new Log(new LLong(0),
                new LLong(0),
                Instant.now(Clock.systemUTC()).toString(),
                "Kill yourself.",
                true,
                true,
                new Exception("Ignore")));
        } catch (InterruptedException e) {
            log("The poison pill failed. Fatal error!", e);
            System.exit(1);
        }
        List<Entry<String, Long>> words = lemma.getMapAsList();
        //put in CSV
        toCSV(words);
        System.out.println("Success! :)");
    }

    private static Path getLogs() {
        return PDFPath.resolve(Logs);
    }
    private static Path getCSVPath() {
        return PDFPath.resolve(CSVPath);
    }
    final private static LongAdder logNum = new LongAdder(); //thread safe long adder so that the log num works.
    // this one is for if its not supposed to be an error. notError is overriden for safety, as it should always be true if this method is used anyway.
    public static void log(String message, boolean notError) {
        log(new LLong("\"None\""), new LLong("\"None\""), message, new Exception("Ignore."), true);
    }
    //a shorthand for the main method if the iteration num and thread num arent known.
    public static void log(String message, Exception e) {
        log(new LLong("\"None\""), new LLong("\"None\""), message, e, false);
    }
    //the main log method, the other two call it and it actaully puts the logs into the block.
    public static void log(LLong iterationNum, LLong threadNum, String message, Exception e, boolean notError) {
        Instant now = Instant.now(Clock.systemUTC());
        try {
            logQueue.offer(new Log(threadNum, iterationNum, now.toString(), message, notError, false, e),1500,TimeUnit.MILLISECONDS);
        } catch (InterruptedException f) {
            log("Issue trying to put a log into queue!", f);
        }
    }
    private static void logWriter(int threadNum) throws InterruptedException {
        final StringBuilder logBuilder = new StringBuilder(); //a log builder
        try (final BufferedWriter writer = Files.newBufferedWriter(getLogs(), StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            while (true) {
                Log current = logQueue.take();
                if (current.poisonPill())
                    break;
                logNum.increment();  
                logBuilder.append("\n  - Log num: ").append(logNum.sum())
                          .append("\n    Thread num: ").append(current.threadNum().returnMe())
                          .append("\n    Iteration num: ").append(current.iterationNum().returnMe())
                          .append("\n    Time: ").append(current.time())
                          .append("\n    Message: \"").append(current.message());
                if (!current.notError()) {
                    logBuilder.append("\n    Error Details:")
                              .append("\n      Message: \"").append(current.e().toString().replace("\"", "'"))
                              .append("\"\n      Stack Trace: |");
                    for (StackTraceElement f : current.e().getStackTrace())
                        logBuilder.append("\n        ").append(f);
                }  
                String message = logBuilder.toString();
                writer.write(message);
                logBuilder.delete(0, logBuilder.length());
            }
        } catch (IOException e) {
            log(new LLong(logNum.longValue()), new LLong(threadNum), "There was an issue trying to write to the log YAML.", e, false);
        }
    }
    //creates a list of lines to add with a stream(), then writes them all in one go with Files.write()
    private static <K, V> void toCSV(List<Entry<K, V>> entries) {
        final StringBuilder keyConcat = new StringBuilder();
        char[] lines = entries.stream()
                                .map(entry -> {
                                    keyConcat.append(entry.getKey())
                                             .append(',')
                                             .append(entry.getValue());
                                    final String temp = keyConcat.toString();
                                    keyConcat.delete(0, keyConcat.length());
                                    return temp;
                                })
                                .collect(
                                    Collectors.joining("\n"))
                                .toCharArray();
        try (final BufferedWriter CSVWriter = Files.newBufferedWriter(getCSVPath(), StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            CSVWriter.write(lines);
        } catch (IOException e) {
            log("Issue writing to CSV!", e);
        }
    }
    //kills threads
    public static void kill(Thread[] threads, BlockingQueue<PString> queue) {
        try {
            for (Thread _ : threads) {
                queue.put(new PString("Pill", true));
            }
        } catch (InterruptedException e) {
            Main.log("Error trying to kill threads with poison pills.", e);
        }
    }
    //get images
    private static void getImages(int threadNum) throws InterruptedException {
        final StringBuilder imageNamer = new StringBuilder();
        int imageCount = 0;
        try {
            while (true) {
                PPage taken = imageQueue.take();
                if (taken.pill())
                    break;
                PDDocument doc = taken.doc();
                imageCount = 1; //count of files
                for (PDPage CPage : doc.getPages()) { //iterate pages
                    PDResources CResources = CPage.getResources(); //all resources on page
                    for (COSName Cname : CResources.getXObjectNames()) { //all objects in resources, so no text, fonts or anything
                        PDXObject xobject = CResources.getXObject(Cname); //current object found by the name.
                        if (xobject instanceof PDImageXObject Cimage) { //if its an image, continue.
                            BufferedImage image = Cimage.getImage(); //convert to a BufferedImage for exporting
                            imageNamer.append("Image") //file info, will be Image 1 WxH.png
                                      .append(imageCount)
                                      .append(' ')
                                      .append(image.getWidth())
                                      .append('x')
                                      .append(image.getHeight())
                                      .append(".png");
                            String title = imageNamer.toString();
                            imageNamer.delete(0, imageNamer.length());
                            Path CPath = Paths.get(ImagePath.toString(), title);
                            if (!Files.exists(CPath))
                                Files.createFile(CPath);
                            ImageIO.write(image, "png", CPath.toFile()); //make the file
                            imageCount++; //iterate the count
                        }
                    }
                }
            }
        } catch (InterruptedException e) {

        } catch (IOException e) {
            log(new LLong("\"None\""), new LLong(threadNum), "An error has occurred while trying to get Images", e, false);
                                System.exit(1);
        } catch (NullPointerException e) {
            log(new LLong(imageCount), new LLong(threadNum), "A NullPointerException occurred with gathering the XObjects from CResources. This is a threading issue.", e, false);
        } finally {
            log(new LLong(imageCount), new LLong(threadNum), "Image thread " + threadNum + " exiting", new Exception("None"), true);
        }
    }
}