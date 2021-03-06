package nz.ac.waikato.its.dspace.exportcitation.converters;

import nz.ac.waikato.its.dspace.exportcitation.Converter;
import nz.ac.waikato.its.dspace.exportcitation.EndnoteExportCrosswalk;
import org.apache.commons.lang.StringUtils;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;

import java.util.Objects;
import java.util.Properties;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for AgResearch and the LCoNZ Institutional Research Repositories
 */
public class UrlFromIdentifierAndRelationConverter implements Converter {
	private String otherField;

	@Override
	public void appendOutput(StringBuilder builder, String risFieldName, String mdFieldName, Metadatum[] values, Item item) {
		appendURLs(builder, risFieldName, values);
		if (StringUtils.isNotBlank(otherField) && !otherField.equals(mdFieldName)) {
			Metadatum[] otherFieldValues = item.getMetadataByMetadataString(otherField);
			appendURLs(builder, risFieldName, otherFieldValues);
		}
	}

	private void appendURLs(StringBuilder builder, String risFieldName, Metadatum[] values) {
		for (Metadatum value : values) {
			if (StringUtils.isNotBlank(value.value)) {
				String valueString = value.value;
				EndnoteExportCrosswalk.appendLine(builder, risFieldName, valueString);
			}
		}
	}

	@Override
	public void init(Properties properties, String converterName) {
		String key = "converterconfig." + converterName + ".otherfield";
		if (properties.containsKey(key)) {
			String otherFieldProp = properties.getProperty(key);
			if (StringUtils.isNotBlank(otherFieldProp)) {
				otherField = otherFieldProp;
			}
		}
	}
}
