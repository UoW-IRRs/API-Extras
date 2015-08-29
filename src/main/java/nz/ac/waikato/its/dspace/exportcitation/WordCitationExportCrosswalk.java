package nz.ac.waikato.its.dspace.exportcitation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.dspace.authorize.AuthorizeException;
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
 * Created by andrea on 29/08/15.
 */
public class WordCitationExportCrosswalk implements CitationDisseminationCrosswalk {
    private static final Logger log = Logger.getLogger(WordCitationExportCrosswalk.class);

    private boolean includeAbstract = false;

    public WordCitationExportCrosswalk(boolean includeAbstract) {
        this.includeAbstract = includeAbstract;
    }

    @Override
    public boolean canDisseminate(Context context, DSpaceObject dso) {
        return dso != null && dso.getType() == Constants.ITEM;
    }

    @Override
    public void disseminate(Context context, DSpaceObject dso, OutputStream out) throws CrosswalkException, IOException, SQLException, AuthorizeException {
        XWPFDocument document = new XWPFDocument();

        processSingleItem((Item) dso, document);

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
    public void disseminateList(Context context, List<DSpaceObject> dsos, OutputStream out) throws CrosswalkException, IOException, SQLException, AuthorizeException {
        XWPFDocument document = new XWPFDocument();

        for (DSpaceObject dso : dsos) {
            if (canDisseminate(context, dso)) {
                processSingleItem((Item)dso, document);
            } else {
                log.warn("Cannot disseminate " + dso.getTypeText() + " id=" + dso.getID() + ", skipping");
            }
        }

        document.write(out);
        out.flush();
    }
}
