package nz.ac.waikato.its.dspace.citeproc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the UoW ITS Institutional Research Repositories
 */
public class NameConverter implements Converter {
	private static final Logger log = Logger.getLogger(NameConverter.class);

	@Override
	public void insertValue(ObjectNode rootNode, String field, Item item, Metadatum[] mdValues, ObjectMapper mapper) {
		if (mdValues == null || mdValues.length < 1 || mdValues[0] == null || mdValues[0].value == null) {
			return;
		}
		ArrayNode namesNode = mapper.createArrayNode();
		for (Metadatum mdValue : mdValues) {
			if (mdValue == null || mdValue.value == null) {
				continue;
			}
			String value = mdValue.value;

			String lastName, firstName = null;
			if (value.contains(", ")) {
				lastName = value.substring(0, value.indexOf(", "));
				firstName = value.substring(value.indexOf(", ") + ", ".length());
			} else {
				lastName = value;
				log.warn("Name " + value + " not in format \"lastname, firstname\", falling back to using whole name as lastname");
			}
			ObjectNode valueNode = mapper.createObjectNode();
			valueNode.put("family", lastName);
			if (StringUtils.isNotBlank(firstName)) {
				valueNode.put("given", firstName);
			}
			namesNode.add(valueNode);
		}
		rootNode.put(field, namesNode);
	}
}
