package nz.ac.waikato.its.dspace.exportcitation.converters;

import nz.ac.waikato.its.dspace.exportcitation.Converter;
import nz.ac.waikato.its.dspace.exportcitation.EndnoteExportCrosswalk;
import org.apache.commons.lang.StringUtils;
import org.dspace.content.DCValue;
import org.dspace.content.Item;

import java.util.Objects;
import java.util.Properties;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for AgResearch and the LCoNZ Institutional Research Repositories
 */
public class EndnoteDOIConverter implements Converter {

	public static final String DEFAULT_PREFIX = "DOI: ";
	private String prefix;

	@Override
	public void appendOutput(StringBuilder builder, String risFieldName, String mdFieldName, DCValue[] values, Item item) {
		for (DCValue value : values) {
			if (StringUtils.isNotBlank(value.value)) {
				String doiString = Objects.toString(prefix, DEFAULT_PREFIX) + value.value;
				EndnoteExportCrosswalk.appendLine(builder, risFieldName, doiString);
			}
		}
	}

	@Override
	public void init(Properties properties, String converterName) {
		String key = "converterconfig." + converterName + ".prefix";
		if (properties.containsKey(key)) {
			String prefixProp = properties.getProperty(key);
			if (StringUtils.isNotBlank(prefixProp)) {
				prefix = prefixProp;
				prefix = StringUtils.removeStart(prefix, "\"");
				prefix = StringUtils.removeEnd(prefix, "\"");
			}
		}
	}
}
