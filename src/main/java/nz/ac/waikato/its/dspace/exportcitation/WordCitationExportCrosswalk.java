package nz.ac.waikato.its.dspace.exportcitation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xwpf.model.XWPFHyperlinkDecorator;
import org.apache.poi.xwpf.usermodel.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.content.crosswalk.CrosswalkException;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STUnderline;

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

        String citationText = getFirstNonBlankValue(item, "dc", "identifier", "citation");
        String abstractText = getFirstNonBlankValue(item, "dc", "description", "abstract");
        String handleUrl = HandleManager.getCanonicalForm(item.getHandle());

        processSingleItem(document, citationText, abstractText, handleUrl);

        document.write(out);
        out.flush();
    }

    void processSingleItem(XWPFDocument document, String citationText, String abstractText, String handleUrl) {
        XWPFParagraph para = document.createParagraph();
        XWPFRun run = para.createRun();
        run.setText(StringUtils.isNotBlank(citationText) ? citationText : "(no citation)");
        para.setSpacingAfter(200);

        if (StringUtils.isNotBlank(handleUrl)) {
            // from http://stackoverflow.com/a/22456273/72625
            String linkId = document.getPackagePart().addExternalRelationship(handleUrl, XWPFRelation.HYPERLINK.getRelation()).getId();

            para = document.createParagraph();
            para.setSpacingAfter(200);

            para.createRun().setText("AgScite record: ");

            CTHyperlink link = para.getCTP().addNewHyperlink();
            link.setId(linkId);

            CTText linkText = CTText.Factory.newInstance();
            linkText.setStringValue(handleUrl);

            CTR ctr = CTR.Factory.newInstance();
            ctr.setTArray(new CTText[]{linkText});

            ctr.addNewRPr().addNewColor().setVal("0000FF");
            ctr.addNewRPr().addNewU().setVal(STUnderline.SINGLE);

            link.setRArray(new CTR[]{ctr});
        }

        if (includeAbstract && StringUtils.isNotBlank(abstractText)) {
            // split md string at double newline so we can create actual paragraphs
            String[] abstractParas = abstractText.split("\r?\n\r?\n");

            for (String abstractPara : abstractParas) {
                para = document.createParagraph();
                run = para.createRun();
                run.setText(abstractPara);
                para.setSpacingAfter(200);
                para.setIndentationLeft(200);
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
                String citationText = getFirstNonBlankValue(item, "dc", "identifier", "citation");
                String abstractText = getFirstNonBlankValue(item, "dc", "description", "abstract");
                String handleUrl = HandleManager.getCanonicalForm(item.getHandle());

                processSingleItem(document, citationText, abstractText, handleUrl);
            } else {
                log.warn("Cannot disseminate " + item.getTypeText() + " id=" + item.getID() + ", skipping");
            }
        }

        document.write(out);
        out.flush();
    }
}
