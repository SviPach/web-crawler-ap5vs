package utb.fai;

import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Třída ParserCallback je používána parserem DocumentParser,
 * je implementován přímo v JDK a umí parsovat HTML do verze 3.0.
 * Při parsování (analýze) HTML stránky volá tento parser
 * jednotlivé metody třídy ParserCallback, co nám umožuje
 * provádět s částmi HTML stránky naše vlastní akce.
 * 
 * @author Tomá Dulík
 */
class ParserCallback {

    /**
     * pageURI bude obsahovat URI aktuální parsované stránky. Budeme
     * jej vyuívat pro resolving všech URL, které v kódu stránky najdeme
     * - předtím, než nalezené URL uložíme do foundURLs, musíme z něj udělat
     * absolutní URL!
     */
    URI pageURI;

    /**
     * depth bude obsahovat aktuální hloubku zanoření
     */
    int depth = 0;
    int maxDepth;

    /**
     * visitedURLs je množina všech URL, které jsme již navtívili
     * (parsovali). Pokud najdeme na stránce URL, který je v této množině,
     * nebudeme jej u dále parsovat
     */
    Set<URI> visitedURIs;

    /**
     * foundURLs jsou všechna nová (zatím nenavštívená) URL, která na stránce
     * najdeme. Poté, co projdeme celou stránku, budeme z tohoto seznamu
     * jednotlivá URL brát a zpracovávat.
     */
    LinkedList<URIinfo> foundURIs;

    /** pokud debugLevel>1, budeme vypisovat debugovací hlášky na std. error */
    int debugLevel = 0;
    HashMap<String, Integer> wordCounts = new HashMap<>();

    ParserCallback(Set<URI> visitedURIs, LinkedList<URIinfo> foundURIs, int maxDepth, int debugLevel) {
    this.foundURIs = foundURIs;
    this.visitedURIs = visitedURIs;
    this.maxDepth = maxDepth;
    this.debugLevel = debugLevel;
    }

    public void parseDocument(Document doc) {
        Elements links = doc.select("a[href]");

        String text = doc.body().text();
        String[] words = text
            .toLowerCase()
            .replaceAll("(?<!\\p{L})['’]|['’](?!\\p{L})", " ")
            .replaceAll("[^\\p{L}\\p{N}'’]+", " ")
            .trim()
            .split("\\s+");
        System.out.println("Number of words: " + words.length);

        for (String word : words) {
            wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
        }

        for (Element link : links) {
            String href = link.attr("href");

            try {
                URI uri = pageURI.resolve(href);

                if (depth < maxDepth && !uri.isOpaque() && !visitedURIs.contains(uri)) {
                    visitedURIs.add(uri);
                    foundURIs.add(new URIinfo(uri, depth + 1));

                    if (debugLevel > 0) {
                        System.err.println("Adding URI: " + uri);
                    }
                }
            } catch (Exception e) {
                System.err.println("Incorrect URI: " + href);
            }
        }
    }
}
