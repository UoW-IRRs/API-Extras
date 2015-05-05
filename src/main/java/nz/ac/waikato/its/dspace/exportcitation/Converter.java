package nz.ac.waikato.its.dspace.exportcitation;

import org.dspace.content.Item;
import org.dspace.content.Metadatum;

import java.util.Properties;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for AgResearch and the LCoNZ Institutional Research Repositories
 */
public interface Converter {
	public void appendOutput(StringBuilder builder, String risFieldName, String mdFieldName, Metadatum[] values, Item item);

	public void init(Properties properties, String converterName);
}
