# PDF Word Counter

Takes in a **PDF** or group of **PDFs** and returns a CSV of the words in it, ranked by frequency.

## Terminology and Glossary

This text uses subtle terminology to distinguish things. Here is a legend so you know *exactly* what each term means:

- If the term "PDF" is in a code block (i.e. `PDF`), it is referring to the **FOLDER** named "PDF". Because it is defined  here exactly what `PDF` refers to, it is redundant to specify the folder afterward. Therefore, the word "folder" will be omitted. The same logic applies to `Images`.
- If the term "PDF" is in bold, it is referring to the **FILE TYPE** named "PDF". This also applies to "CSV".
- If a word is in quotes, it is referring to the **CONCEPT**, or, if paired with  a code block definition of `PDF`, the **LITERAL**. This same logic applies to `Images`.
- If a word is referring to a proper noun, it will be in **BOLD** (i.e. **GitHub**).
- If a word is referring to another file format such as ".jar", it will also be in `code blocks`.
- If a word is in italics, this is simply emphasis, same as normal **English**.
- The terms "lemmatize" and "lemmatizers" are talked about here. Definitions are presented for the just-discussed words and a few others since this is technical and semi-custom terminology:
  - To *lemmatize* is to get the dictionary form of a word (The infinitive tense in most cases), such that things like "*ran*" become "*run*". This is powered by [**Stanford CoreNLP**.](https://stanfordnlp.github.io/CoreNLP/)
  - A *lemmatizer* is, in this case, a thread (the **CS** concept kind) that takes a "chunk" of text from a **PDF**, converts it to the lemmatized forms of the word(s)*, and keeps count of them in a `ConcurrentHashMap`.
  - A "chunk" is a block of text in the `BlockingQueue` used for lemmatization due to threading. It is *50* words in length, except for the dump at the end. Details on the dump in this section's footnote.
  - A dependency is a library from a third party imported for our use. A *sub*dependency is a dependency of a dependency. If we want a:
  >dependency of a dependency of a dependency of a dependency

  We would call it a subsubsubdependency. The number of subs can be determined by the number of times "dependency" is mentioned, minus 1.
- If you're like me and feel strongly about the consistency of texts, like this one here, then I would explain that the reason I keep changing the terminology for how the words are sorted in the **CSV** is to *emphasize* the order of the words.
- If present, footnotes will be presented at the bottom of a header section, compared to the bottom of the text to save on superscript characters used.
- **BOLD** text on "verbs" like "*will*" and "*and*" can act as emphasis too, such as: "A **AND** B".
- The `PDF extractor` is the folder in this same repository. [Linked here.](https://github.com/cheeesed123/PDF-Word-Counter/tree/main/src/main/java/org/ChiefGuy)
- In the [**Benchmarks**](https://github.com/cheeesed123/PDF-Word-Counter/edit/main/README.md#benchmarks) section, "_" is used in the same way it is used in Java and TOML, to replace a comma when representing big numbers.

### T&G† Footnotes

**\***: It's technically possible a chunk could be one singular word, therefore making words incorrect. This is because, if the last remaining words of the text do not fit into a chunk, they're simply dumped into the queue to ensure they're processed.\
**†**: Despite being unnecessary, I chose to shorten this footnote name down through an abbreviation for succinctness. You may see this occur in other places in this text as well.

## Abstract and Process

This program works in the following manner:

1. **PDFs** you want to process are placed into the `PDF`.
2. When run, the program will cycle through each PDF in `PDF` one by one. If you only want to do one **PDF**, just put the **PDF** in its own folder.
3. The program will take the **PDF** currently in the queue from `PDF`, and then run several threads, which will:

    - Return the count of the most common lemmatized form of the words in the **PDF**.
    - Download all* of the images in the **PDF** into `Images` as `.png`s.
4. The program will then move on to the next **PDF** and repeat step 3 until all **PDF**s in `PDF` have been processed.
5. It will then make a **CSV** of all of the words, with their respective counts.


### A&P Footnotes

**\***: Gathering images from **PDF**(s) is not perfected due to the seemingly random hexadecimal encoding. It is possible for `NullPointerException`s to occur while gathering images, and some images to not show up at all. The `NullPointerException`s will not terminate the program, by the way; they are only logged to the `logs.yaml`. It must also be made clear, though, that this is not a frequent occurrence, but merely a possibility made present for awareness.

## Installation

The installation of this is extremely easy.

1. *Download*\* the files from **GitHub**. Any manner works as long as you get the `PDF extractor` onto your local system.
2. *Dedicate* a folder on your computer to it; the name doesn't matter, or simply use the provided folder.
3. *Package* the **Maven** project to a `.jar`, or just `compile` it there.
4. *Run* the `.jar` through the terminal of your choice. (I assume you know how to use the terminal well enough to run a `.jar` if you're on **GitHub**.)

**Upon first run in a new parent folder, the program will generate its folders for use and exit.**

The items generated are as follows:

- The `Images` folder, which will be the source of all of the images extracted from the **PDFs** in `PDF`.
- The `PDF` folder, which will contain the **PDFs** you want to extract information from. It also contains the following:
  - The `logs.yaml` file, which records log information involving exceptions, thread initialization **AND** termination.
  - The `wordAmounts.csv` file, which will record the count of words by most common first (descending order).

### Installation Footnotes

**\***: The beginning word of each enumerated item is italicized for appearance and emphasis, but primarily for appearance.

## Benchmarks

The benchmarks were conducted in the following procedure:

1. The `wordAmounts.csv` data was gathered from one copy of the linked **PowerShell** **PDF** up above.
2. A second **Java** script, located in the other branch, took the data, parsed it, and generated **PDFs** that were completely random. The generator accounts for the weight of the words and decreases the weight as time progresses, similar to a "bag of marbles" scenario. This was done with a "Fenwick Tree" and **iText**.
3. The program was run on these **PDFs** in the categories of: 50, 100, 500, 1_000, 5_000, 50_000 pages, and all of these together sequentially. The **PDFs** were the same for every trial.
4. The time taken was calculated by **PowerShell**. Here is the full program's code:

      ``` (PowerShell)
      $time = Measure-Command { 
          mvn clean compile exec:java "-Dexec.mainClass=org.ChiefGuy.Main" -e | Out-Host
      }
      
      # Display time performance
      $time
      
      ```

5. The results were put into this table of 5 datapoints, with an extra average column beside it.\
The "Time per page" is calculated by the average amount of time divided by the number of pages.
Here is the table:

|Number of Pages | Trial 1 | Trial 2 | Trial 3 | Trial 4 | Trial 5 | Average | Time per page |
| ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 50 | 5846.1 | 5519.6 | 5548.6 | 5690.0 | 5662.6 | 5653.38 | 113.07 |
| 100 | 6562.4 | 6772.0 | 6469.5 | 6550.8 | 6286.1 | 6528.16 | 65.28 |
| 500 | 11719.3 | 13096.0 | 12763.8 | 13022.5 | 12136.0 | 12547.52 | 25.10 |
| 1,000 | 19563.8 | 20149.0 | 19158.2 | 19735.2 | 18995.3 | 19520.3 | 19.52 |
| 5,000 | 71970.0 | 70482.4 | 72101.8 | 72821.3 | 71268.3 | 71728.76 | 14.35 |
| 50,000 | 645452.4 | 666337.0 | 742344.1 | 668428.9 | 792007.0 | 702913.88 | 14.06 |
| All | 715822.9 | 710414.9 | 746100.5 | 771115.9 | 774651.9 | 743621.22 | 13.13 |

I would also like to note that, while the code for generating the benchmark PDFs is attached here, I did not bother to comment it. I do not plan to take the time to, but if you need it, just email me, and I will give you a breakdown of it. Here is a chart of the number of pages vs. the time per page, with a power trendline as well.

<img width="2966" height="1599" alt="Chart showing relationship between length of PDF and time taken per page." src="https://github.com/user-attachments/assets/062c849a-85a7-4ad4-986d-d44fd51ce7b5" />

## Methodology and Implementation

This section is dedicated to explaining the inner workings of the code and some answers to some choices I made in writing it. This section will be broken down into sections I thought would be of interest to talk about in detail, as of course I can't simply talk about every line; that would be too verbose.

### Section 1: Dependencies

This program has 4 dependencies, but 2 of them are subdependencies. The other two are **PDFBox** and **Stanford CoreNLP**. Now that this program is finished, I largely regret choosing *these* libraries for my tasks, but it's too late to change easily. You see, both of these libraries have significant problems:

- **PDFBox** is not thread-safe. I was able to find a workaround that allowed me to not duplicate *anything*, but it would still have been better had **PDFBox** been:

  - Faster
  - Lighter
  - Thread-safe
- **Stanford CoreNLP** is excessive. I needed a tool that could do lemmatization. Anything less than **CoreNLP** used "stemming" instead. I managed to minimize the amount of processing spent loading **CoreNLP**, but it was still suboptimal compared to a tool designed *specifically* for lemmatization.

Another interesting thing about these two dependencies is that they are also the largest bottlenecks in the process. Remember this, as it will be significant imminently.

### Section 2: Threads

The number of threads is determined by the number of CPU cores available on the user's computer. I used a 16-core AMD Ryzen 7, and so I have 17 total threads running during the program. 17 is due to the following display:

- The total cores (16 for me) - 1 is dedicated to lemmatizers, which are by far the slowest part and, therefore, get the most allocated resources.
- 1 core is dedicated to an image ripper, which usually finishes at around 30~% of the way through the **PDF** (tested on a [3000 page **PDF** about PowerShell primarily.](https://learn.microsoft.com/pdf?url=https%3A%2F%2Flearn.microsoft.com%2Fen-us%2Fpowershell%2Fscripting%2Ftoc.json%3Fview%3Dpowershell-7.6))
- Lastly, a thread is made to log thread status, errors, and exceptions to the log. It's very low priority that the logging happens, so it can share resources with other threads.

### Section 3: Getting Information and Processing it

An interesting thing about **PDFBox** is that there's no stream mechanism for getting lines of text. Now, waiting for **PDFBox** to process all of the text in advance would be slow. To counter this, I made a class extend `TextStripper`, and overwrote a method that got frequently called under the hood of **PDFBox** to instead get the lines through there. The lines were then collected with a `StringBuilder`, and passed into a `LinkedBlockingQueue` (Linked specifically since it's the fastest) for processing. Each lemmatizer would then be waiting for the queue to have a chunk, when it would then pull it, process it, and add it to the count. The TextStripper **IS** a bottleneck, but there isn't anything I can do about it, as **PDFBox** is not fully thread-safe. Once the text is fully counted, we write it to the **CSV** with a `BufferWriter` (the fastest way to write) all in one go.

## Thank You

Thank you for trying this out!!!
I've put quite a bit of effort into this, so I hope you're able to enjoy this and use it well.
Have fun!

If you need to contact me, my email is <olliessmith2910@gmail.com>.
