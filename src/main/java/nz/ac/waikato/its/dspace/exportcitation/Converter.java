package nz.ac.waikato.its.dspace.exportcitation;

import org.dspace.content.DCValue;
import org.dspace.content.Item;

import java.util.Properties;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public interface Converter {
	public void appendOutput(StringBuilder builder, String risFieldName, String mdFieldName, DCValue[] values, Item item);

	public void init(Properties properties, String converterName);
}
