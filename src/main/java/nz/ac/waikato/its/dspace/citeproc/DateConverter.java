package nz.ac.waikato.its.dspace.citeproc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.dspace.content.DCDate;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the UoW ITS Institutional Research Repositories
 */
public class DateConverter implements Converter {
	@Override
	public void insertValue(ObjectNode rootNode, String field, Item item, Metadatum[] mdValue, ObjectMapper mapper) {
		if (mdValue == null || mdValue.length < 1 || mdValue[0] == null || mdValue[0].value == null) {
			return;
		}
		DCDate date = new DCDate(mdValue[0].value);
		ArrayNode partsArray = mapper.createArrayNode();

		partsArray.add(date.getYear());
		if (date.getMonth() > 0) {
			partsArray.add(date.getMonth());
		}
		if (date.getDay() > 0) {
			partsArray.add(date.getDay());
		}

		ObjectNode datePartsNode = mapper.createObjectNode();
		ArrayNode anonymousArray = mapper.createArrayNode();
		anonymousArray.add(partsArray);
		datePartsNode.put("date-parts", anonymousArray);

		rootNode.put("issued", datePartsNode);
	}
}
