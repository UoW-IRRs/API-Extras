package nz.ac.waikato.its.dspace.exportcitation;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz
 *         for the University of Waikato's Institutional Research Repositories
 */
public class TestWordCitationExport {
    //@Test
    public void makeWordDoc() {
        String citation = "citation";
        String abstractText = "abstract";
        String handleURL = "link";

        try {
            XWPFDocument document = new XWPFDocument();

            new WordCitationExportCrosswalk(true).processSingleItem(document, citation, abstractText, handleURL);

            File tempFile = File.createTempFile("word", ".docx");
            System.out.println(tempFile.getCanonicalPath());
            FileOutputStream out = new FileOutputStream(tempFile);

            document.write(out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
