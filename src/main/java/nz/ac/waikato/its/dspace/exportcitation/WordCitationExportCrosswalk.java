package nz.ac.waikato.its.dspace.exportcitation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.content.crosswalk.CrosswalkException;
import org.dspace.core.Constants;
import org.dspace.core.Context;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for Waikato University ITS
 */
public class WordCitationExportCrosswalk implements CitationDisseminationCrosswalk {
    private static final Logger log = Logger.getLogger(WordCitationExportCrosswalk.class);

    private boolean includeAbstract = false;

    public WordCitationExportCrosswalk(boolean includeAbstract) {
        this.includeAbstract = includeAbstract;
    }

    @Override
    public boolean canDisseminate(Context context, Item item) {
        try {
            return item != null && AuthorizeManager.authorizeActionBoolean(context, item, Constants.READ, true);
        } catch (SQLException e) {
            log.warn("Cannot determine whether item id=" + item.getID() + " is readable by current user, assuming no", e);
            return false;
        }
    }

    @Override
    public void disseminate(Context context, Item item, OutputStream out) throws CrosswalkException, IOException, SQLException, AuthorizeException {
        XWPFDocument document = new XWPFDocument();

        processSingleItem(item, document);

        document.write(out);
        out.flush();
    }

    private void processSingleItem(Item item, XWPFDocument document) {
        String citationText = getFirstNonBlankValue(item, "dc", "identifier", "citation");
        String abstractText = getFirstNonBlankValue(item, "dc", "description", "abstract");

        XWPFParagraph para = document.createParagraph();
        XWPFRun run = para.createRun();
        run.setText(citationText);
        para.setSpacingAfter(200);

        if (includeAbstract && StringUtils.isNotBlank(abstractText)) {
            // split md string at double newline so we can create actual paragraphs
            String[] abstractParas = abstractText.split("\r?\n\r?\n");

            for (String abstractPara : abstractParas) {
                para = document.createParagraph();
                run = para.createRun();
                run.setText(abstractPara);
                para.setSpacingAfter(200);
            }
        }
        para.setSpacingAfter(600); // override spacing for final para
    }

    private String getFirstNonBlankValue(Item item, String schema, String element, String qualifier) {
        Metadatum[] citationMds = item.getMetadata(schema, element, qualifier, Item.ANY);
        for (Metadatum citationMd : citationMds) {
            if (citationMd != null && StringUtils.isNotBlank(citationMd.value)) {
                return citationMd.value;
            }
        }
        return null;
    }

    @Override
    public String getMIMEType() {
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    }

    @Override
    public void disseminateList(Context context, List<Item> items, OutputStream out) throws CrosswalkException, IOException, SQLException, AuthorizeException {
        XWPFDocument document = new XWPFDocument();

        for (Item item : items) {
            if (canDisseminate(context, item)) {
                processSingleItem(item, document);
            } else {
                log.warn("Cannot disseminate " + item.getTypeText() + " id=" + item.getID() + ", skipping");
            }
        }

        document.write(out);
        out.flush();
    }
}
