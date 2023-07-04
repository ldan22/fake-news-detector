package ro.utcn.danlupu.poc;


import com.articulate.nlp.RelExtract;
import com.articulate.nlp.TimeSUMOAnnotator;
import com.articulate.nlp.WNMultiWordAnnotator;
import com.articulate.nlp.WSDAnnotator;
import com.articulate.nlp.brat.BratAnnotationUtil;
import com.articulate.nlp.corpora.TimeBank;
import com.articulate.nlp.pipeline.Pipeline;
import com.articulate.nlp.pipeline.SentenceUtil;
import com.articulate.nlp.semRewrite.Interpreter;
import com.articulate.nlp.semRewrite.Literal;
import com.articulate.nlp.semRewrite.RHS;
import com.articulate.sigma.*;
import com.articulate.sigma.tp.EProver;
import com.articulate.sigma.tp.Vampire;
import com.articulate.sigma.trans.OWLtranslator;
import com.articulate.sigma.trans.TPTP3ProofProcessor;
import com.articulate.sigma.utils.StringUtil;
import com.articulate.sigma.wordNet.WordNet;
import com.articulate.sigma.wordNet.WordNetUtilities;
import com.google.common.collect.ImmutableList;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.Timex;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.*;

import edu.stanford.nlp.pipeline.*;

import static java.lang.System.out;


@Deprecated
public class ProofOfConceptController {

    public static KB kb = null;
    public static Pipeline p = null;
    public static Interpreter interp = null;

    private final Logger LOGGER = LoggerFactory.getLogger(ProofOfConceptController.class);

    @GetMapping("/ontology/init")
    public ResponseEntity<String> initOntology() {
        LOGGER.info("Ontology init request received.");

        KBmanager.debug = true;
        WordNet.debug = true;
        KB.debug = true;
//        Formula.debug = true;
        KBmanager.getMgr().initializeOnce();
        kb = KBmanager.getMgr().getKB("SUMO");
        LOGGER.info("Cache is null: {}", kb.kbCache == null);
        if (kb == null) {
            return ResponseEntity.status(500).body("Server error: Sigma init failed.");
        }
        if (kb.kbCache == null) {
            return ResponseEntity.status(500).body("Server error: KBCache init failed.");
        }

        return ResponseEntity.status(200).body("Sigma init completed");
    }

    @PostMapping("/ontology/check")
    public ResponseEntity<String> check(
            @RequestParam(value = "system", defaultValue = "Local", required = false) String system,
            @RequestParam(value = "location", defaultValue = "Local", required = false) String location,
            @RequestParam(value = "engine", defaultValue = "EProver", required = false) String engine
    ) {
        LOGGER.info("Ontology consistency check request received.");
        if (kb.kbCache == null) {
            LOGGER.info("KB Cache is null. Cannot start consistency check");
            return ResponseEntity.status(500).body("CCheck failed");
        }
        KBmanager.initiateCCheck(kb, engine, system, location, "EnglishLanguage", 1200);
        return ResponseEntity.status(200).body("CCheck initiated");
    }

    @GetMapping("/ontology/check/results")
    public ResponseEntity<String> cCheckResults(@RequestParam("kbName") String kbName) {
        LOGGER.info("Ontology consistency check results request received for kb: {}.", kbName);

        String results = KBmanager.ccheckResults(kbName);
        return ResponseEntity.status(200).body(results);
    }

    @GetMapping("/ontology/check/status")
    public ResponseEntity<String> cCheckStatus(@RequestParam("kbName") String kbName) {
        LOGGER.info("Ontology consistency check requestStatus request received for kb: {}.", kbName);

        CCheckManager.CCheckStatus status = KBmanager.ccheckStatus(kbName);
        return ResponseEntity.status(200).body(status.toString());
    }

    @PostMapping("/ontology/query/eprover")
    public ResponseEntity<String> query(@RequestParam("query") String query) {
        LOGGER.info("Query request received with eprover.");

        com.articulate.sigma.trans.TPTP3ProofProcessor tpp = null;
        kb.loadEProver();
        EProver eProver = kb.askEProver(query, 100, 1);
        if (eProver == null) {
            return ResponseEntity.status(200).body("No proof or error");
        } else if (eProver.output.contains("Syntax error detected")) {
            return ResponseEntity.status(400).body("A syntax error was detected in your input.");
        } else {
            tpp = new TPTP3ProofProcessor();
            tpp.parseProofOutput(eProver.output, query, kb, eProver.qlist);
            return ResponseEntity.status(200).body(tpp.bindings + "\n\n" + tpp.proof);
        }
    }

    @PostMapping("/ontology/query/vampire")
    public ResponseEntity<String> queryVampire(@RequestParam("query") String query) {
        LOGGER.info("Query request received with vampire.");

        com.articulate.sigma.trans.TPTP3ProofProcessor tpp;
        kb.loadVampire();
        Vampire vampire = kb.askVampire(query, 600, 1);
        if (vampire == null || vampire.output == null) {
            return ResponseEntity.status(200).body("No proof or error");
        } else if (vampire.output.contains("Syntax error detected")) {
            return ResponseEntity.status(400).body("A syntax error was detected in your input.");
        } else {
            tpp = new TPTP3ProofProcessor();
            tpp.parseProofOutput(vampire.output, query, kb, vampire.qlist);
            return ResponseEntity.status(200).body(tpp.bindings + "\n\n" + tpp.proof);
        }
    }

    @GetMapping("/ontology/nlp/init")
    public ResponseEntity<String> initNLP() throws IOException {
        LOGGER.info("NLP init request received.");

        TimeBank.init();
        interp = new Interpreter();
        interp.initialize();
        RelExtract.initOnce();

        String propString = "tokenize, ssplit, pos, lemma, parse, depparse, ner, wsd, wnmw, tsumo, sentiment";
        p = new Pipeline(true, propString);

        return ResponseEntity.status(200).body("Sigma nlp init completed");
    }

    @GetMapping("/ontology/nlp/interpreter")
    public ResponseEntity<String> interpret(@RequestParam String theText) {
        LOGGER.info("NLP interpreter request received for text: '{}'.", theText);

        List<String> forms = interp.interpret(theText);
        StringBuilder stringBuilder = new StringBuilder();
        if (forms != null) {
            for (String s : forms) {
                Formula theForm = new Formula(s);
                stringBuilder.append(theForm.htmlFormat(kb, HTMLformatter.createHrefStart())).append("<P>");
            }
        }
        return ResponseEntity.status(200).body(stringBuilder.toString());
    }


    @GetMapping("/ontology/terms")
    public Set<String> healthCheckOntology(@RequestParam String term) {
        LOGGER.info("Instances request received for term: '{}'.", term);

        if (kb == null) {
            return Set.of("KB is NULL");
        }
        return kb.instances(term);
    }

    @GetMapping("/tokens")
    public Map<String, List<String>> tokenize(@RequestParam String inputText) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation document = new Annotation(inputText);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        Map<String, List<String>> result = new HashMap<>();
        List<String> trees = new ArrayList<>();
        for (CoreMap sentence : sentences) {
            Tree parseTree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            trees.add(parseTree.toString());
        }
        result.put("trees", trees);
        return result;
    }




    @GetMapping("/ontology/nlp/full")
    public ResponseEntity<String> fullNlp(@RequestParam String theText) {
        StringBuilder stringBuilder = new StringBuilder();
        String kbHref = HTMLformatter.createKBHref("SUMO", "EnglishLanguage");
        String wnHref = kbHref.replace("Browse.jsp", "WordNet.jsp");
        List<String> forms = interp.interpret(theText);
        System.out.println("NLP.jsp: Running relation extraction");
        ArrayList<RHS> kifClauses = RelExtract.sentenceExtract(theText);
        if (!StringUtil.emptyString(theText)) {
            Annotation wholeDocument = new Annotation(theText);
            wholeDocument.set(CoreAnnotations.DocDateAnnotation.class, "2017-05-08");
            p.pipeline.annotate(wholeDocument);
            List<CoreMap> timexAnnsAll = wholeDocument.get(TimeAnnotations.TimexAnnotations.class);
            if (timexAnnsAll != null && timexAnnsAll.size() > 0) {
                stringBuilder.append("<h2>Time</h2>\n"); // -----------------------------------------------------------
                for (CoreMap token : timexAnnsAll) {
                    Timex time = token.get(TimeAnnotations.TimexAnnotation.class);
                    stringBuilder.append("time token and value: <pre>" + token + ":" + time.value() + "</pre>\n");
                    String tsumo = token.get(TimeSUMOAnnotator.TimeSUMOAnnotation.class);
                    Formula tf = new Formula(tsumo);
                    stringBuilder.append(tf.htmlFormat(kb, HTMLformatter.createHrefStart()) + "<P>\n");
                }
            }

            stringBuilder.append("<P>\n");
            stringBuilder.append("<h2>Tokens</h2>\n"); // -------------------------------------------------------------
            List<String> senses = new ArrayList<String>();
            List<CoreMap> sentences = wholeDocument.get(CoreAnnotations.SentencesAnnotation.class);
            for (CoreMap sentence : sentences) {
                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                for (CoreLabel token : tokens) {
                    String orig = token.originalText();
                    String lemma = token.lemma();
                    String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    String poslink = "<a href=\"https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html\">" +
                            pos + "</a>";
                    String sense = token.get(WSDAnnotator.WSDAnnotation.class);
                    if (!StringUtil.emptyString(sense))
                        senses.add(sense);
                    String sumo = token.get(WSDAnnotator.SUMOAnnotation.class);
                    String multi = token.get(WNMultiWordAnnotator.WNMultiWordAnnotation.class);
                    out.print(orig);
                    if (!StringUtil.emptyString(lemma))
                        out.print("/" + lemma);
                    if (!StringUtil.emptyString(pos))
                        out.print("/" + poslink);
                    if (!StringUtil.emptyString(sense)) {
                        String keylink = "<a href=\"" + wnHref + "&synset=" + sense + "\">" + sense + "</a>";
                        out.print("/" + keylink);
                    }
                    if (!StringUtil.emptyString(sumo)) {
                        String SUMOlink = "<a href=\"" + kbHref + "&term=" + sumo + "\">" + sumo + "</a>";
                        out.print("/" + SUMOlink);
                    }
                    if (!StringUtil.emptyString(multi))
                        out.print("/" + multi);
                    stringBuilder.append("&nbsp;&nbsp; ");
                }
                stringBuilder.append("<P>\n");
            }

            Iterator<String> it = senses.iterator();
            stringBuilder.append("<table>");
            while (it.hasNext()) {
                String key = it.next();
                String keylink = "<a href=\"" + wnHref + "&synset=" + key + "\">" + key + "</a>";
                String SUMO = WordNetUtilities.getBareSUMOTerm(WordNet.wn.getSUMOMapping(key));
                String SUMOlink = "<a href=\"" + kbHref + "&term=" + SUMO + "\">" + SUMO + "</a>";
                ArrayList<String> words = WordNet.wn.synsetsToWords.get(key);
                String wordstr = "";
                if (words != null)
                    wordstr = words.toString();
                stringBuilder.append("<tr><td>" + keylink + "</td><td>" +
                        SUMOlink + "</td><td>" + wordstr + "</td></tr><P>\n");
            }
            stringBuilder.append("</table><P>");

            stringBuilder.append("<h2>Visualization</h2>\n"); // ------------------------------------------------------
            stringBuilder.append("<div id=\"bratVizDiv\" style=\"\"></div><P>\n");

            stringBuilder.append("<h2>Dependencies</h2>\n"); // -------------------------------------------------------
            stringBuilder.append("<table><tr><th>original</th><th>augmented</th><th>substitutors</th></tr><tr><td>\n");
            for (CoreMap sentence : sentences) {
                stringBuilder.append("<pre>");
                List<Literal> dependencies = SentenceUtil.toDependenciesList(ImmutableList.of(sentence));
                for (Literal l : dependencies)
                    stringBuilder.append(l);
                stringBuilder.append("</pre><P>");
            }
            stringBuilder.append("</td>\n");
            stringBuilder.append("<td><pre>\n");
            for (Literal l : interp.augmentedClauses)
                stringBuilder.append(l);
            stringBuilder.append("</pre>");

            stringBuilder.append("</td>");
            stringBuilder.append("<td><pre>\n");
//            stringBuilder.append(interp.substitutor.toString());
            stringBuilder.append("</pre>");
            stringBuilder.append("</td>");
            stringBuilder.append("</tr></table>\n");
            stringBuilder.append("<P>");

            stringBuilder.append("<h2>Relations</h2>\n"); // ----------------------------------------------------------
            stringBuilder.append("<pre>");
            stringBuilder.append(kifClauses);
            stringBuilder.append("</pre>");
            stringBuilder.append("<P>");

            stringBuilder.append("<h2>Interpretation</h2>\n"); // -----------------------------------------------------
            if (forms != null) {
                for (String s : forms) {
                    Formula theForm = new Formula(s);
                    stringBuilder.append(theForm.htmlFormat(kb, HTMLformatter.createHrefStart()) + "<P>");
                }
            }

            stringBuilder.append("<h2>Sentiment</h2>\n"); // ----------------------------------------------------------
            stringBuilder.append("<table>");
            for (CoreMap sentence : sentences) {
                String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                for (CoreLabel token : tokens) {
                    String sumo = token.get(WSDAnnotator.SUMOAnnotation.class);
                    if (!StringUtil.emptyString(sumo)) {
                        String SUMOlink = "<a href=\"" + kbHref + "&term=" + sumo + "\">" + sumo + "</a>";
                        stringBuilder.append("<tr><td>" + SUMOlink + "</td><td>" + sentiment + "</td></tr><P>\n");
                    }
                }
            }
            stringBuilder.append("</table><P>\n");

            stringBuilder.append("<b>Sigma sentiment score:</b> " + DB.computeSentiment(theText) + "</P>\n");
            BratAnnotationUtil bratAnnotationUtil = new BratAnnotationUtil();
            stringBuilder.append("<script type=\"text/javascript\">");
            stringBuilder.append("var docData=" + bratAnnotationUtil.getBratAnnotations(theText, wholeDocument) + ";</script>");
            stringBuilder.append("<script type=\"text/javascript\" src=\"js/sigmanlpViz.js\"></script>");
        } else
            stringBuilder.append("Empty input<P>\n");

        return ResponseEntity.status(200).body(stringBuilder.toString());
    }

    public static void test() throws IOException {

    }

    public static void main(String[] args) throws IOException {
    }
}
