package nz.ac.waikato.its.dspace.exportcitation;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.content.crosswalk.CrosswalkException;
import org.dspace.content.crosswalk.StreamDisseminationCrosswalk;
import org.dspace.core.Context;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by andrea on 29/08/15.
 */
public interface CitationDisseminationCrosswalk extends StreamDisseminationCrosswalk {

    void disseminateList(Context context, List<DSpaceObject> dsos, OutputStream outputStream) throws CrosswalkException, IOException, SQLException, AuthorizeException;

}
