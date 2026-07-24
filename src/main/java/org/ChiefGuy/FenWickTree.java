package org.ChiefGuy;

import java.util.Arrays;
import java.util.Random;

public class FenWickTree {
    private long[] tree;
    final private Word[] words;
    private long total;
    public FenWickTree(Word[] words) {
        if (words == null || words.length < 1) {
            throw new IllegalArgumentException("Words array must contain at least one data word.");
        }
        this.tree = makeTree(words);
        this.words = Arrays.copyOf(words, words.length);
        this.total = words[0].count();
        if (this.total <= 0) {
            throw new IllegalStateException("Total word weight must be positive but was " + this.total);
        }
        System.out.println("Fenwick Tree done.");
    }
    //takes index, applies a change to the weighted val at that index. index is determined by binary magic
    public void updateTree(int index, long change) {
        while (index < tree.length) {
            tree[index] += change;
            index += index & -index;
        }
        // Update total once per logical change
        total += change;
    }
    public void reset() {
        this.tree = makeTree(words);
        long sum = 0;
        for (int i = 1; i < words.length; i++) {
            sum += words[i].count();
        }
        this.total = sum;
        if (this.total <= 0) {
            throw new IllegalStateException("Total word weight must be positive but was " + this.total);
        }
    }
    //gets a word, works similar to a binary search. Takes a value, and tries decreasingly smaller powers of 2 until that power is less than the random value.
    public int getWord(Random random) {
        final long randNum = random.nextLong(total);
        int index = 0, step = getStep(tree.length - 1);
        long sum = 0;
        while (step > 0) {
            int next = index + step;
            if (next < tree.length && sum + tree[next] <= randNum) {
                sum += tree[next];
                index = next;
            }
            step >>= 1;
        }
        if (index + 1 == tree.length) return index;
        return index + 1;
    }
    //gets a power of 2 less than the limit
    private int getStep(int limit) {
        int power = 1;
        //for 2^n, 2^{n+1} = 2^n*2
        while (power * 2 <= limit) {
            power *= 2;
        }
        return power;
    }
    public long getTotal() {
        return total;
    }
    private long[] makeTree(Word[] words) {
        int length = words.length;
        long[] NewTree = new long[length];
        for (int i = 1; i < length; i++) {
            NewTree[i] = makeTreeItem(words, i);
            //System.out.println("Made tree item " + i);
        }
        return NewTree;
    }
    private long makeTreeItem(Word[] words, int index) {
        int idxInclude = index - (index & -index);
        long sum = 0;
        for (int i = index; i > idxInclude; i--) {
            long count = words[i].count();
            sum += count;
        }
        return sum;
    }
}
