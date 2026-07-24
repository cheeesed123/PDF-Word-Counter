package org.ChiefGuy;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class Lemmatizer {
    final private Properties props = new Properties(1);
    final private LongAdder iteration = new LongAdder();
    final private ConcurrentHashMap<String, Long> words = new ConcurrentHashMap<>();
    private final BlockingQueue<PString> queue;
    public Lemmatizer(BlockingQueue<PString> queue) {
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        this.queue = queue;
    }
    public void lemmatizer(int threadNum) throws InterruptedException {
        Main.log(new LLong(iteration.sum()), new LLong(threadNum), "Starting lemmatizer thread " + threadNum, new Exception("None"), true);
        StanfordCoreNLP pipe = new StanfordCoreNLP(props);
        CoreDocument cDoc;
        try {
            while (true) {
                PString current = queue.take();
                if (current.pill())
                    break;
                iteration.increment();

                cDoc = new CoreDocument(current.text());
                pipe.annotate(cDoc); //apply properties from earlier
                for (CoreLabel token : cDoc.tokens()) {
                    String finalW;
                    //if its a proper noun, just add that, else use dictionary form.
                    final String tokenT = token.tag();
                    final String tokenW = token.word();
                    final String tokenL = token.lemma();
                    if ((tokenW.length() == 1 && !Character.isLetterOrDigit(tokenW.charAt(0))) || isAllSpecial(tokenW) || tokenW.contains(","))
                        continue;
                    if (tokenT != null && tokenT.equals("NNP"))
                        finalW = tokenW;
                    else
                        finalW = tokenL;
                    words.merge(finalW, 1L, (oldValue, newValue) -> Long.sum(oldValue, newValue));
                }
            }
        } catch (InterruptedException e) {
            Main.log(new LLong(iteration.sum()), new LLong(threadNum), "An interrupted exception occurred during the lemmatizer.", e, false);
        } finally {
            Main.log(new LLong(iteration.sum()), new LLong(threadNum), "Lemmatizer thread " + threadNum + " exiting.", new Exception("None"), true);
        }
    }
    private boolean isAllSpecial(String token) {
        for (char c : token.toCharArray()) {
            if (Character.isLetterOrDigit(c))
                return false;
        }
        return true;
    }
    //returns map.
    public List<Entry<String, Long>> getMapAsList() {
        return words.entrySet()
                    .stream()
                    .sorted(
                        Map.Entry.comparingByValue(Comparator.reverseOrder())
                    ).toList();
    }
}
