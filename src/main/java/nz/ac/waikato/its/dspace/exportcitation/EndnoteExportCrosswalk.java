package nz.ac.waikato.its.dspace.exportcitation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.content.crosswalk.CrosswalkException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for Waikato University ITS
 */
public class EndnoteExportCrosswalk implements CitationDisseminationCrosswalk {
	private static final String SEPARATOR = "  - ";
	private static final String FIELD_NAME_TOP_LEVEL_TYPE = "TY";
	private static final String END_OF_RECORD = "ER";

	private static final Logger log = Logger.getLogger(EndnoteExportCrosswalk.class);

	public static final String CHARSET = "UTF8";

	private Map<String, String> fieldTypes;
	private Map<String, String> fields;
	private Map<String, Converter> converters;

	public EndnoteExportCrosswalk() {
		Properties properties = ConfigurationManager.getProperties("endnote-export");
		// init converters
		converters = new HashMap<>();
		Pattern keyPattern1 = Pattern.compile("^converter\\.(\\w+)$");
		for (Object key1 : properties.keySet()) {
			Matcher matcher = keyPattern1.matcher(key1.toString());
			if (matcher.matches()) {
				String name = matcher.group(1);
				try {
					String className = properties.getProperty(key1.toString());
					Converter converter = (Converter) Class.forName(className).newInstance();
					converter.init(properties, name);
					converters.put(name, converter);
				} catch (ClassNotFoundException e) {
					log.error("Can't find converter class " + name, e);
				} catch (InstantiationException e) {
					log.error("Can't instantiate converter class " + name, e);
				} catch (IllegalAccessException e) {
					log.error("Not allowed to access converter class " + name, e);
				}
			}
		}
		// init fields and field types
		fields = new HashMap<>();
		fieldTypes = new HashMap<>();

		Pattern keyPattern = Pattern.compile("^field\\.([a-zA-Z0-9\\-]+)$");
		Pattern valuePattern = Pattern.compile("^([a-zA-Z\\.,]+)(?:\\((\\w+)\\))?$");
		for (Object key : properties.keySet()) {
			Matcher keyMatcher = keyPattern.matcher(key.toString());
			if (keyMatcher.matches()) {
				String field = keyMatcher.group(1);
				String value = properties.getProperty(key.toString());
				Matcher valueMatcher = valuePattern.matcher(value);
				if (valueMatcher.matches()) {
					String dcField = valueMatcher.group(1);
					fields.put(field, dcField);
					if (valueMatcher.groupCount() > 1) {
						String fieldConverter = valueMatcher.group(2);
						if (fieldConverter != null) {
							fieldTypes.put(dcField, fieldConverter);
						}
					}
				}
			}
		}
	}

	public void writeHeader(OutputStream out) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, CHARSET));

		writer.append("Provider: DSpace RIS Export");
		writer.newLine();
		writer.append("Database: ").append(ConfigurationManager.getProperty("dspace.name"));
		writer.newLine();
		writer.append("Content: text/plain; charset=\"UTF-8\"");
		writer.newLine();
		writer.append("\n\n"); // two line breaks to separate document header from reference data

		writer.flush();
		writer.close();
	}

	@Override
	public boolean canDisseminate(Context context, Item item) {
		try {
			return item != null && AuthorizeManager.authorizeActionBoolean(context, item, Constants.READ, true);
		} catch (SQLException e) {
			log.warn("Cannot determine whether item id=" + item.getID() + " is readable by current user, assuming no", e);
			return false;
		}
	}

	@Override
	public void disseminate(Context context, Item item, OutputStream out) throws CrosswalkException, IOException, SQLException, AuthorizeException {
		if (!canDisseminate(context, item)) {
			throw new CrosswalkException("Cannot disseminate object (null or non-item object type)");
		}

		writeHeader(out);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, CHARSET));

		processSingleItem(item, writer);

		writer.close();
		out.flush();
	}

	@Override
	public void disseminateList(Context context, List<Item> items, OutputStream out) throws CrosswalkException, IOException, SQLException, AuthorizeException {
		writeHeader(out);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, CHARSET));

		for (Item item : items) {
			if (canDisseminate(context, item)) {
				processSingleItem(item, writer);
			} else {
				log.warn("Cannot disseminate " + item.getTypeText() + " id=" + item.getID() + ", skipping");
			}
		}

		writer.close();
		out.flush();
	}

	private void processSingleItem(Item item, BufferedWriter writer) throws IOException {
		StringBuilder result = new StringBuilder();

		processField(item, result, FIELD_NAME_TOP_LEVEL_TYPE);
		for (String field : fields.keySet()) {
			if (!field.equals(FIELD_NAME_TOP_LEVEL_TYPE)) {
				processField(item, result, field);
			}
		}
		appendLine(result, END_OF_RECORD, "");
		result.append("\n\n");

		writer.append(result);
	}

	private void processField(Item item, StringBuilder result, String field) {
		if (fields.containsKey(field)) {
			String fieldValue = fields.get(field);
			Converter converter = null;
			if (fieldTypes.containsKey(fieldValue)) {
				if (converters.containsKey(fieldTypes.get(fieldValue))) {
					converter = converters.get(fieldTypes.get(fieldValue));
					if (converter == null) {
						log.warn("Converter " + fieldTypes.get(fieldValue)  + " configured for field " + fieldValue + " but none found, check config and classpath");
					}
				}
			}

			String[] mdFields = fieldValue.split(","); // multiple md fields might be involved

			for (String mdField : mdFields) {
				Metadatum[] values = item.getMetadataByMetadataString(mdField);
				if (values != null && values.length > 0) {
					//we have found a value
					if (converter != null) {
						converter.appendOutput(result, field, mdField, values, item);
					} else {
						for (Metadatum value : values) {
							if (StringUtils.isNotBlank(value.value)) {
								appendLine(result, field, value.value);
							}
						}
					}
					break; // don't look at any other md fields
				}
			}
		}
	}


	public static void appendLine(StringBuilder out, String field, String content) {
		out.append(field).append(SEPARATOR).append(content).append("\n");
	}

	@Override
	public String getMIMEType() {
		return "application/x-research-info-systems";
	}

}
