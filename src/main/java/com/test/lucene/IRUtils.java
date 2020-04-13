package com.test.lucene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.AfterEffectB;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BasicModelIn;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.MultiSimilarity;
import org.apache.lucene.search.similarities.NormalizationH1;
import org.apache.lucene.search.similarities.DFRSimilarity;
import java.util.ArrayList;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.jsoup.Jsoup;
public class IRUtils {
    private static final String CRAN_DELIMITER = ".I";
    public final static Path currentRelativePath = Paths.get("").toAbsolutePath();
	public final static String absPathFedRegister = String.format("%s/Docs/fr94",currentRelativePath);
    public static String QUERIES_PATH=String.format("%s/Docs/topics",currentRelativePath);
    private static ArrayList<Document> DocList = new ArrayList<>();
    public final static String absPathFB = String.format("%s/Docs/fbis",currentRelativePath);
    public final static String absPathLA = String.format("%s/Docs/latimes",currentRelativePath);
	public final static String absPathFT = String.format("%s/Docs/ft",currentRelativePath);
    public final static String absPathpro = String.format("%s/prolog/wn_s.pl",currentRelativePath);
    public final static String absPathstop = String.format("%s/Docs/stopWords",currentRelativePath);


    public static ArrayList<Document> loadDocs(){
        try {
            loadFedRegisterDocs(absPathFedRegister,"DOCNO","DOCTITLE","TEXT");
            loadFedRegisterDocs(absPathFT,"DOCNO","HEADLINE","TEXT");
            loadFedRegisterDocs(absPathFB,"DOCNO","TI","TEXT");
            loadFedRegisterDocs(absPathLA,"DOCNO","HEADLINE","TEXT");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
         return DocList;

    }
    public static ArrayList<Document> loadFedRegisterDocs(String pathToFedRegister, String id, String setitle,
            String setext) throws IOException {
        
        File path = new File(pathToFedRegister);
        File[] directories=path.listFiles(File::isDirectory);
        String docno,text,title;
        if (pathToFedRegister==absPathFB || pathToFedRegister==absPathLA){
            directories=new File[1];
            directories[0]=path;
        }
        for (File directory : directories) {
            File[] files = directory.listFiles();
            for (File file : files) {
                org.jsoup.nodes.Document d = Jsoup.parse(file, null, "");
                org.jsoup.select.Elements documents = d.select("DOC");
                for (org.jsoup.nodes.Element document : documents) {
                    title = document.select(setitle).text();
                    if (pathToFedRegister==absPathFT){
                        String[] arr = title.split("/");
                        if (arr.length>1){
                            title="";
                            for (int i=1;i<arr.length;i++)
                            {
                                title+=arr[i].trim();
                            }
                        }
                    }
                    docno = document.select(id).text();
                    text = document.select(setext).text();
                    Document doc=addDoc(docno, text, title);
                    DocList.add(doc);
                }
            }
        }
        return DocList;
    }

    private static Document addDoc(String docno, String text, String title) {
        Document doc = new Document();
        doc.add(new StringField("docno", docno, Field.Store.YES));
        doc.add(new TextField("text", text, Field.Store.NO));
        doc.add(new TextField("title", title, Field.Store.NO));
        // fedRegisterDocList.add(doc);
        return doc;
    }
    
    
    
    
    public static ArrayList<ArrayList<String>> getArrQueries() throws Exception {
        
        ArrayList<ArrayList<String>> arr=new ArrayList<>();
        File path = new File(IRUtils.QUERIES_PATH);
        org.jsoup.nodes.Document d = Jsoup.parse(path, null, "");

        org.jsoup.select.Elements documents = d.select("Top");

        for (org.jsoup.nodes.Element document : documents) {
            ArrayList<String> content=new ArrayList<>();
            String title=document.select("title").text();
            String num=document.select("num").text();
            String desc=document.select("desc").text();
            desc=desc.substring(0, desc.indexOf("Narrative"));
            String narr=document.select("narr").text();
            num=num.substring(8,11);
            // System.out.println(num);
            String[] narrarr;
            StringBuilder relarr=new StringBuilder();
            StringBuilder negarr=new StringBuilder();
            StringBuilder mustarr=new StringBuilder();
            narrarr=narr.split("\\.|\\?|\\;");
            for (int i=0;i<narrarr.length;i++){
                String sentence=narrarr[i];
                if (sentence.contains("not relevant") || sentence.contains("irrelevant")) {
                    String tmps=sentence.replaceAll("are also not relevant|are not relevant|are irrelevant|is not relevant|not|NOT", "");
                    if (sentence.contains("unrelated to"))
                    {
                        mustarr.append(tmps);
                        continue;    
                    }
                    else{                    
                        negarr.append(tmps);
                    }
                }
                else{
                    relarr.append(sentence.replaceAll(
                        "a relevant document identifies|a relevant document could|a relevant document may|a relevant document must|a relevant document will|a document will|to be relevant|relevant documents|a document must|relevant|will contain|will discuss|will provide|must cite",
                        ""));
                }
            }
            content.add(title);
            content.add(desc);
            content.add(relarr.toString().trim());
            content.add(negarr.toString().trim());
            content.add(mustarr.toString().trim());
            content.add(num);
            arr.add(content);

        }
        return arr;
    }


    
    public static void WriteResults(ArrayList<QueryResult> allResults, String resultPath) throws IOException {
        StringBuilder resultSet = new StringBuilder();
        for (QueryResult qres : allResults) {
            resultSet.append(qres.getCranQueryNumber());
            resultSet.append("\t" + "0");
            resultSet.append("\t" + qres.getContent());
            resultSet.append("\t" + qres.getDocumentRank());
            resultSet.append("\t" + qres.getDocumentScore());
            resultSet.append("\t" + "tag" + "\n");
        }
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("trec_eval/results.out")));
        writer.write(resultSet.toString());
        writer.close();
    }

    
    private String InputStreamToString(InputStreamReader ip) throws IOException {
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        for (; ; ) {
            int rsz = ip.read(buffer, 0, buffer.length);
            if (rsz < 0)
                break;
            out.append(buffer, 0, rsz);
        }
        return out.toString();
    }
    public  String fileToString(String filePath) throws IOException {
            String data = "";
            InputStreamReader inputStreamReader = new InputStreamReader(this.getClass().getResourceAsStream(filePath));
            //BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            //String doc = bufferedReader.readLine();
            data=this.InputStreamToString(inputStreamReader);
            //data = new String(Files.readAllBytes(Paths.get(filePath)));
            return data;
        }

     public String[] splitDocs(String path) throws Exception {

            String cranDocFile = this.fileToString(path);
    
            String[] cranDocFileSplits = cranDocFile.split(CRAN_DELIMITER);
            cranDocFileSplits = Arrays.copyOfRange(cranDocFileSplits, 1, cranDocFileSplits.length);
    
            return cranDocFileSplits;
        }
    public CharArraySet getStopWordSet() throws IOException {
            InputStreamReader inputStreamReader = new InputStreamReader(getClass().getResourceAsStream("stopWords"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            CharArraySet stopWordSet = new CharArraySet(1000, true);
            stopWordSet =CharArraySet.copy(StopAnalyzer.ENGLISH_STOP_WORDS_SET);
            while (bufferedReader.ready()) {
                stopWordSet.add(bufferedReader.readLine());
            }
            bufferedReader.close();
            return stopWordSet;
        }

    public static Similarity getSimilarity(){
        Similarity similarity[] = {
            new BM25Similarity(2, (float) 0.89),
            new DFRSimilarity(new BasicModelIn(), new AfterEffectB(), new NormalizationH1()),
            new LMDirichletSimilarity(1500)
        };
        return (new MultiSimilarity(similarity));
    }
    
}