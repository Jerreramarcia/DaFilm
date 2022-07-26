package juanmaherrera.interfaz;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import java.io.IOException;
import java.nio.file.Paths;
import javax.swing.table.DefaultTableModel;
/**
 *
 * 
 * @author Jerreramarcia
 * package juanmaherrera.interfaz;

 */

public class Dependiente {
    
    private static String INDEX_DIR;
    private static String file;

    public Dependiente(String INDEX_DIR, String file) {
        Dependiente.INDEX_DIR = INDEX_DIR;
        Dependiente.file = file;
    }
    

    private void vuelveConLasPeluculas(TopDocs documento, Document d, IndexSearcher searcher) throws IOException {

        System.out.println("Documentos encontrados " + documento.totalHits);

        for (ScoreDoc sd : documento.scoreDocs){

            d = searcher.doc(sd.doc);
            System.out.println(sd.score+  " " +d.get("release_year")+ " "+ d.get("title"));

        }
        
      
      
    }

    public TopDocs buscador(int Release_Year, String title, String Origin_Ethnicity, String Director, String Cast, String Genre, String Plot,int maxAnio, Boolean Cmust, Boolean Tmust, Boolean OEmust, Boolean Dmust, Boolean Gmust, Boolean Pmust, Boolean anioExacto, int numDocs) throws IOException, ParseException {
        
        Boolean Binary = false;
        int documentos = numDocs;
        Document d = null;
        
        FSDirectory dir = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexReader reader1 = DirectoryReader.open(dir);
        
        IndexSearcher searcher = new IndexSearcher(reader1);
        QueryParser parser = new QueryParser("text", new WhitespaceAnalyzer());
                
        
        if(Cmust == true || Tmust == true || OEmust == true || Dmust == true || Gmust == true || Pmust == true || anioExacto){
            
            SortField sf= new SortField("releas_year_UnicYear",SortField.Type.INT,true);    
            sf.setMissingValue(0);
            Sort orden = new Sort(sf);

            BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
            
            if (Cmust == true && !Cast.replace(" ","").isEmpty()){        
                Query q1 = new TermQuery(new Term("cast",Cast));
                System.out.println(q1);
                BooleanClause bc1 = new BooleanClause(q1,BooleanClause.Occur.MUST);
                bqBuilder.add(bc1);

            }else{
                if(!Cast.replace(" ","").isEmpty()){
                    Query q1 = new TermQuery(new Term("cast",Cast));
                    BooleanClause bc1 = new BooleanClause(q1,BooleanClause.Occur.SHOULD);
                    bqBuilder.add(bc1);
                }
            }
           
            if (Tmust == true && !title.replace(" ","").isEmpty()){
                Query q2 = new TermQuery(new Term("title",title));
                BooleanClause bc2 = new BooleanClause(q2,BooleanClause.Occur.MUST);
                bqBuilder.add(bc2);

            }else{
                if(!title.replace(" ","").isEmpty()){
                    Query q2 = new TermQuery(new Term("title",title));
                    BooleanClause bc2 = new BooleanClause(q2,BooleanClause.Occur.SHOULD);
                    bqBuilder.add(bc2);
                }
            }
            
            if (OEmust == true && !Origin_Ethnicity.replace(" ","").isEmpty()){
                Query q3 = new TermQuery(new Term("origin_Ethnicity",Origin_Ethnicity));
                BooleanClause bc3 = new BooleanClause(q3,BooleanClause.Occur.MUST);
                bqBuilder.add(bc3);
                
            }else{
                if(!Origin_Ethnicity.replace(" ","").isEmpty()){
                    Query q3 = new TermQuery(new Term("origin_Ethnicity",Origin_Ethnicity));
                    BooleanClause bc3 = new BooleanClause(q3,BooleanClause.Occur.SHOULD);
                    bqBuilder.add(bc3);

                }
            }
            
            if (Dmust == true && !Director.replace(" ","").isEmpty()){
                Query q4 = new TermQuery(new Term("director",Director));
                BooleanClause bc4 = new BooleanClause(q4,BooleanClause.Occur.MUST);
                bqBuilder.add(bc4);

            }else{
                if(!Director.replace(" ","").isEmpty()){
                    Query q4 = new TermQuery(new Term("director",Director));
                    BooleanClause bc4 = new BooleanClause(q4,BooleanClause.Occur.SHOULD);
                    bqBuilder.add(bc4);

                }
            }
            
            if (Gmust == true && !Genre.replace(" ","").isEmpty()){
                Query q5 = new TermQuery(new Term("genre",Genre));
                BooleanClause bc5 = new BooleanClause(q5,BooleanClause.Occur.MUST);
                bqBuilder.add(bc5);

            }else{
                if(!Genre.replace(" ","").isEmpty()){
                    Query q5 = new TermQuery(new Term("genre",Genre));
                    BooleanClause bc5 = new BooleanClause(q5,BooleanClause.Occur.SHOULD);
                    bqBuilder.add(bc5);

                }
            }
            
            if (Pmust == true && !Plot.replace(" ","").isEmpty()){
                Query q6 = new TermQuery(new Term("plot",Plot));
                BooleanClause bc6 = new BooleanClause(q6,BooleanClause.Occur.MUST);
                bqBuilder.add(bc6);

            }else{
                if(!Plot.replace(" ","").isEmpty()){
                    Query q6 = new TermQuery(new Term("plot",Plot));
                    BooleanClause bc6 = new BooleanClause(q6,BooleanClause.Occur.SHOULD);
                    bqBuilder.add(bc6);

                }
            }
            
            if(!anioExacto){
                int max,min;
                if(Release_Year <= maxAnio){ max = maxAnio; min = Release_Year;}else{min = maxAnio; max = Release_Year;}
                    Query q32 = IntPoint.newRangeQuery("release_yearUnicYear",min, max);
                    BooleanClause bc62 = new BooleanClause(q32,BooleanClause.Occur.MUST);
                    bqBuilder.add(bc62);
            
            
            }else{
                    maxAnio = Release_Year;
                    Query q32 = IntPoint.newRangeQuery("release_yearUnicYear", Release_Year, maxAnio);
                    BooleanClause bc62 = new BooleanClause(q32,BooleanClause.Occur.MUST);
                    bqBuilder.add(bc62);
            
            }
 
            Binary = true;
            BooleanQuery bq = bqBuilder.build();
            TopDocs docs = searcher.search(bq, documentos);
            vuelveConLasPeluculas(docs,d,searcher);
            return docs;

        }
  
        String queryTit = "", queryPlot = "", queryYear = "",
                queryOrigin = "", queryDirector = "", queryCast = "",
                queryGenre = "", queryWiki = "";
     
        if(!Binary){
            
            if (!title.isBlank()){queryTit = "title:\""+title+"\"";}
            if (!Plot.isBlank()){queryPlot = "plot:\""+Plot+"\"";}
            if (!Origin_Ethnicity.isBlank()){queryOrigin = "origin_ethnicity:\""+Origin_Ethnicity+"\"";}
            if (!Director.isBlank()){queryDirector = "director:\""+Director+"\"";}
            if (!Cast.isBlank()){queryCast= "cast:\""+Cast+"\"";}
            if (!Genre.isBlank()){queryGenre = "genre:\""+Genre+"\"";}

            
        
        String quest = queryTit + " " + queryPlot + " " + queryOrigin + " " + queryDirector + " " + queryCast+ " " + queryGenre;
        System.out.println(quest);

        if(!quest.replace(" ", "").isEmpty()){
            Query q1 = parser.parse(quest);
            TopDocs docs = searcher.search(q1, documentos);
            
            return docs;
        }
            System.out.println(anioExacto);
            if(!anioExacto && quest.replace(" ", "").isEmpty()){
                int max,min;
                if(Release_Year <= maxAnio){ max = maxAnio; min = Release_Year;}else{min = maxAnio; max = Release_Year;}
                    BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();

                    Query q32 = IntPoint.newRangeQuery("release_yearUnicYear",min, max);
                    BooleanClause bc62 = new BooleanClause(q32,BooleanClause.Occur.MUST);
                    bqBuilder.add(bc62);
                    
                    
                    BooleanQuery bq = bqBuilder.build();
                    TopDocs docs = searcher.search(bq, documentos);
                    vuelveConLasPeluculas(docs,d,searcher);
                    
                    return docs;
            }

        }
        
        System.out.println("NO DEBERIA LLEGAR");
        return null;
    }
}
