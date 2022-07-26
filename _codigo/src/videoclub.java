import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class videoclub {
    private static String INDEX_DIR;
    private static String file;
    private static ArrayList<pelicula> peliculas = new ArrayList<>();

    public static class pelicula{

        int idPelicula;
        int Release_Year;
        String Title;
        String Origin_Ethnicity;
        String Director;
        String Cast;
        String Genre;
        String Wiki_Page;
        String Plot;

        public pelicula(int idpelicula, int release_year, String title, String origin_Ethnicity, String director, String cast, String genre, String wiki_Page, String plot) {

            idPelicula = idpelicula;
            Release_Year = release_year;
            Title = title;
            Origin_Ethnicity = origin_Ethnicity;
            Director = director;
            Cast = cast;
            Genre = genre;
            Wiki_Page = wiki_Page;
            Plot = plot;

        }

        public String getTitle() {
            return Title;
        }

        public int getRelease_Year() {
            return Release_Year;
        }

        public String getOrigin_Ethnicity() {
            return Origin_Ethnicity;
        }

        public String getDirector() {
            return Director;
        }

        public String getCast() {
            return Cast;
        }

        public String getGenre() {
            return Genre;
        }

        public String getWiki_Page() {
            return Wiki_Page;
        }

        public String getPlot() {
            return Plot;
        }

        public int getIdPelicula(){return idPelicula;}


    }
    private static Document createDocument(Integer idPelicula, Integer release_year, String title, String origin_Ethnicity, String director, String cast, String genre, String wiki_Page, String plot)
    {
        Document document = new Document();
        document.add(new StringField("id", idPelicula.toString() , Field.Store.YES));
        document.add(new IntPoint("release_yearUnicYear", release_year));
        document.add(new StoredField("release_yearUnicYear",release_year));
        document.add(new NumericDocValuesField("release_year", release_year));
        document.add(new StoredField("release_year",release_year));
        document.add(new TextField("title", title , Field.Store.YES));
        document.add(new TextField("origin_Ethnicity", origin_Ethnicity , Field.Store.YES));
        document.add(new TextField("director", director , Field.Store.YES));
        document.add(new TextField("cast", cast , Field.Store.YES));
        document.add(new TextField("genre", genre , Field.Store.YES));
        document.add(new TextField("wiki_Page", wiki_Page , Field.Store.YES));
        document.add(new TextField("plot", plot , Field.Store.YES));

        return document;
    }

    public static IndexWriter createWriter() throws IOException
    {
        FSDirectory dir = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        IndexWriter writer = new IndexWriter(dir, config);

        return writer;
    }
    public static String normalizeDirector(String director){
        /*Usamos una expresion regular para tener en cuenta las diferentes opciones
          director*
          *director
          *director*
          Director*
          *Director
          *Director*
          Siendo '*' cualquier simbolo que no sea una letra ()!¡:...
        */
        Pattern p = Pattern.compile("\\W*d*D*irector\\W*",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        //System.out.println("Sustituimos "+ director+ " por "+ p.matcher(director).replaceAll(""));

        return p.matcher(director).replaceAll("");

    }
    public static String normalizeCast(String cast){
        /*
        En este caso, la expresion regular es necesaria ya que en el campo de cast, en ocasiones aparece tambien el director
        es por esto por lo que tenemos que eliminarlo, hay que tener en cuenta que algunos apellidos estan separados por '-' E.j (Jaume Collet-Serra),
        si no quitamos este guion se solapara el apellido con el nombre del primer actor.
        Gracias a la expresion regular conseguimos eliminar los campos de cast con la siguiente estructura:
        director: <nombre director + apellidos (si tiene, separados con espacios o '-')> Cast:
        Dandonos como resultado directamente el cast de la película.
        'director' se escoge al igual que en la normalizacion de director, mientras que Cast: siempre sigue la misma estructura.
         */

        Pattern p = Pattern.compile("(\\W*d*D*irector\\W*(\\w*[äöüïëÄÖÜ]*\s*-*\\w*[äöüëïÄÖÜ]*)*)*(Cast: )*",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        String salida = p.matcher(cast).replaceAll("");
        //System.out.println("Sustituimos "+ cast+ " por "+ p.matcher(cast).replaceAll(""));

        Pattern p2 = Pattern.compile("(see Cast)+",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

        return p2.matcher(salida).replaceAll("Uknown");

    }
    private static void setFile(String ruta){
        file = ruta;
    }
    private static void setIndex(String rutaIndex){
        INDEX_DIR = rutaIndex;
    }
    public static void main(String[] args) throws IOException, CsvException, ParseException {

        Properties prop = new Properties();
        String fileName = "conf.config",director_norm, cast_norm;
        String[] nextRecord;

        int id = 1;

        try (FileInputStream fis = new FileInputStream(fileName)) {
            prop.load(fis);
        } catch (FileNotFoundException ex) {System.out.println("No se ha encontrado");}

        setFile(prop.getProperty("conf.csv_route"));
        setIndex(prop.getProperty("conf.index_route"));

        IndexWriter writer = createWriter();
        List<Document> documents = new ArrayList<>();

        Reader reader = Files.newBufferedReader(Paths.get(file));
        CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();

        while ((nextRecord = csvReader.readNext()) != null) {

            director_norm = normalizeDirector(nextRecord[3]);
            cast_norm = normalizeCast(nextRecord[4]);

            peliculas.add(new pelicula(id, Integer.parseInt(nextRecord[0]),nextRecord[1], nextRecord[2], director_norm, cast_norm, nextRecord[5], nextRecord[6], nextRecord[7]));

            Document document1 = createDocument(id, Integer.parseInt(nextRecord[0]),nextRecord[1], nextRecord[2], director_norm, cast_norm, nextRecord[5], nextRecord[6], nextRecord[7]);
            documents.add(document1);

            id++;
        }

        writer.deleteAll();
        writer.addDocuments(documents);
        writer.commit();
        writer.close();



    }


}
