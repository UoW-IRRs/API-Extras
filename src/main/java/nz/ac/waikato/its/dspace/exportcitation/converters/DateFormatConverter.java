package nz.ac.waikato.its.dspace.exportcitation.converters;

import nz.ac.waikato.its.dspace.exportcitation.Converter;
import nz.ac.waikato.its.dspace.exportcitation.EndnoteExportCrosswalk;
import org.apache.commons.lang.StringUtils;
import org.dspace.content.DCDate;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;

import java.text.SimpleDateFormat;
import java.util.Properties;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for AgResearch and the LCoNZ Institutional Research Repositories
 */
public class DateFormatConverter implements Converter {

	private SimpleDateFormat formatter;

	@Override
	public void appendOutput(StringBuilder builder, String risFieldName, String mdFieldName, Metadatum[] values, Item item) {
		for (Metadatum value : values) {
			if (StringUtils.isNotBlank(value.value)) {
				String formattedDate = value.value;
				if (formatter != null) {
					DCDate date = new DCDate(value.value);
					if (date.toDate() != null) {
						formattedDate = formatter.format(date.toDate());
					}
				}
				EndnoteExportCrosswalk.appendLine(builder, risFieldName, formattedDate);
			}
		}
	}

	@Override
	public void init(Properties properties, String converterName) {
		String key = "converterconfig." + converterName + ".format";
		if (properties.containsKey(key)) {
			String format = null;
			String formatProp = properties.getProperty(key);
			if (StringUtils.isNotBlank(formatProp)) {
				format = formatProp;
				format = StringUtils.removeStart(format, "\"");
				format = StringUtils.removeEnd(format, "\"");
			}
			if (StringUtils.isNotBlank(format)) {
				try {
					formatter = new SimpleDateFormat(format);
				} catch (NullPointerException | IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
