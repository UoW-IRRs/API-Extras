package nz.ac.waikato.its.dspace.citeproc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the UoW ITS Institutional Research Repositories
 */
public interface Converter {
	void insertValue(ObjectNode rootNode, String field, Item item, Metadatum[] mdValue, ObjectMapper mapper);
}
