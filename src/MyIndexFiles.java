import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import static org.apache.lucene.util.Version.LUCENE_4_10_1;

public class MyIndexFiles {

	public MyIndexFiles() {
	}

    //Folder to save the indexed files in
	static File INDEX_DIR = new File("index");

	/** Index all text files under a directory. */
	public static void main(String[] args) throws UnsupportedEncodingException {
		String usage = "java org.apache.lucene.demo.IndexFiles <root_directory>";
        String filePath = "";
		if (args.length == 0) {
			System.err.println("Usage: " + usage);

            //Get the path do the files
            String path = System.getProperty("user.dir") + "/docs";
            System.out.println(path);
            filePath = path;

            //System.exit(1);

		} else {
            //Running the as CLI with parameters
            // java -jar MyIndexFiles.jar "path/to/files"
            filePath = args[0];
        }

		if (INDEX_DIR.exists()) {

			//System.out.println("Cannot save index to '" + INDEX_DIR
			//		+ "' directory, please delete it first");

            //If folder exsists, delete it
            deleteFolder(INDEX_DIR);

			//System.exit(1);
		}

		final File docDir = new File(filePath);
		if (!docDir.exists() || !docDir.canRead()) {
			System.out
					.println("Document directory '"
							+ docDir.getAbsolutePath()
							+ "' does not exist or is not readable, please check the path");
			System.exit(1);
		}

		Date start = new Date();
		try {
			Analyzer analyzer;
            analyzer = new StandardAnalyzer(LUCENE_4_10_1);
            IndexWriterConfig iwc = new IndexWriterConfig(LUCENE_4_10_1, analyzer);
		    IndexWriter writer = new IndexWriter(FSDirectory.open(INDEX_DIR), iwc); 
	
			System.out.println("Indexing to directory '" + INDEX_DIR + "'...");
			indexDocs(writer, docDir);
			writer.close();

			Date end = new Date();
			System.out.println(end.getTime() - start.getTime()
					+ " total milliseconds");

		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass()
					+ "\n with message: " + e.getMessage());
		}
	}

	static void indexDocs(IndexWriter writer, File file) throws IOException {
		// do not try to index files that cannot be read
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] files = file.list();
				// an IO error could occur
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						indexDocs(writer, new File(file, files[i]));
					}
				}
			} else {
				System.out.println("adding " + file);
				try {
					writer.addDocument(MyDocument.Document(file));
				}
				// at least on windows, some temporary files raise this
				// exception with an "access denied" message
				// checking if the file can be read doesn't help
				catch (FileNotFoundException fnfe) {
					;
				}
			}
		}
	}

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

}
