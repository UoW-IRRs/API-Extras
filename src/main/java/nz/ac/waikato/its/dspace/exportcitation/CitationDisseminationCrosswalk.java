package nz.ac.waikato.its.dspace.exportcitation;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.crosswalk.CrosswalkException;
import org.dspace.content.crosswalk.StreamDisseminationCrosswalk;
import org.dspace.core.Context;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for Waikato University ITS
 */
public interface CitationDisseminationCrosswalk {
    boolean canDisseminate(Context context, Item item);

    void disseminate(Context context, Item item, OutputStream outputStream) throws CrosswalkException, IOException, SQLException, AuthorizeException;
    void disseminateList(Context context, List<Item> items, OutputStream outputStream) throws CrosswalkException, IOException, SQLException, AuthorizeException;

    String getMIMEType();
}
