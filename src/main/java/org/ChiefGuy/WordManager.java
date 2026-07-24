package org.ChiefGuy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

public class WordManager {
    //custom tree object
    final private FenWickTree tree;
    private Word[] words;
    public WordManager(Path CSV) {
        try {
            //get lines
            final List<String> lines = Files.readAllLines(CSV);
            this.words = getArray(lines);
            System.out.println("Word[] array done.");
        } catch (IOException e) {
            System.err.println("There was an issue reading lines from the CSV.");
        }
        this.tree = new FenWickTree(words);

    }
    public long getTotal() {
        return tree.getTotal();
    }
    //gets words
    final private static StringBuilder writer = new StringBuilder();
    public String getWords(int amount) {
        for (int i = 0; i < amount; i++)
            writer.append(getWord()).append(' ');
        final String result = writer.toString();
        writer.delete(0, writer.length());
        return result;
    }
    //gets a word
    final private Random random = new Random();
    private String getWord() {
        if (tree.getTotal() == 0)
            tree.reset();
        int index = tree.getWord(random);
        tree.updateTree(index, -1L);
        words[index] = new Word(words[index].word(), words[index].count() - 1);
        return words[index].word();
    }
    //takes lines, parses into Word array. words[0] is total.
    private Word[] getArray(List<String> lines) {
        long total = 0L;
        if (!lines.isEmpty())
            lines.remove(0);

        final Word[] wordsLimited = new Word[lines.size() + 1];
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank())
                continue;
            String[] split = line.split(",", 2);
            if (split.length < 2)
                continue;
            String word = split[0].trim();
            long count = Long.parseLong(split[1].trim());
            total += count;
            wordsLimited[i + 1] = new Word(word, count);
            //System.out.println("Added word: " + word);
        }
        wordsLimited[0] = new Word("", total);
        return wordsLimited;
    }
}
