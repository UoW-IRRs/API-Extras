package nz.ac.waikato.its.dspace.exportcitation.converters;

import nz.ac.waikato.its.dspace.exportcitation.Converter;
import nz.ac.waikato.its.dspace.exportcitation.EndnoteExportCrosswalk;
import org.apache.commons.lang.StringUtils;
import org.dspace.content.DCValue;
import org.dspace.content.Item;

import java.util.*;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for AgResearch
 */
public class AgResearchEndnoteTypeConverter implements Converter {

	private static final String SUBTYPE_FIELD = "agresearch.subtype";
	private static final String AUDIENCE_FIELD = "agresearch.event.audience";
	private static final String PLACE_FIELD = "agresearch.event.place";

	private static final String SECONDARY_TITLE_RIS = "T2";
	private static final String PLACE_PUBLISHED_RIS = "CY";

	@Override
	public void appendOutput(StringBuilder builder, String risFieldName, String mdFieldName, DCValue[] values, Item item) {
		String dspaceType = findFirstNonBlank(values);

		Map<String, List<String>> additionalLines = null;

		String endnoteType = "GEN"; // default EndNote type: "Generic"
		if ("Thesis".equalsIgnoreCase(dspaceType)) {
			endnoteType = "THES";
		} else if ("Report".equalsIgnoreCase(dspaceType)) {
			endnoteType = "RPRT";
		} else if ("IP/Patent".equalsIgnoreCase(dspaceType)) {
			endnoteType = "PAT";
		} else if ("Conference".equalsIgnoreCase(dspaceType)) {
			endnoteType = "CPAPER";
			additionalLines = makeConferenceExtraFields(item);
		} else if ("Article".equals(dspaceType)) {
			String subtype = findFirstNonBlank(item.getMetadata(SUBTYPE_FIELD));
			if ("Peer reviewed (refereed)".equalsIgnoreCase(subtype)) {
				endnoteType = "JOUR";
			} else {
				endnoteType = "MGZN";
			}
		} else if ("Book/Chapter".equalsIgnoreCase(dspaceType)) {
			String subtype = findFirstNonBlank(item.getMetadata(SUBTYPE_FIELD));
			if ("Book".equalsIgnoreCase(subtype)) {
				endnoteType = "BOOK";
			} else if ("Chapter".equalsIgnoreCase(subtype)) {
				endnoteType = "CHAP";
			} else if ("Edited Book".equalsIgnoreCase(subtype)) {
				endnoteType = "EDBOOK";
			}
		} else if ("Technology Transfer".equalsIgnoreCase(dspaceType)) {
			endnoteType = "GEN";
			additionalLines = makeTechTransferExtraFields(item);
		}

		EndnoteExportCrosswalk.appendLine(builder, risFieldName, endnoteType);
		if (additionalLines != null) {
			for (String additionalField : additionalLines.keySet()) {
				List<String> additionalValues = additionalLines.get(additionalField);
				for (String additionalValue : additionalValues) {
					EndnoteExportCrosswalk.appendLine(builder, additionalField, additionalValue);
				}
			}
		}
	}

	private Map<String, List<String>> makeConferenceExtraFields(Item item) {
		Map<String, List<String>> result = new TreeMap<>();
		String place = findFirstNonBlank(item.getMetadata(PLACE_FIELD));
		if (StringUtils.isNotBlank(place)) {
			result.put(PLACE_PUBLISHED_RIS, Arrays.asList(place));
		}
		return result;
	}

	private Map<String, List<String>> makeTechTransferExtraFields(Item item) {
		Map<String, List<String>> result = new TreeMap<>();
		String subtype = findFirstNonBlank(item.getMetadata(SUBTYPE_FIELD));
		if ("Manual".equalsIgnoreCase(subtype) || "User group booklet".equalsIgnoreCase(subtype)) {
			result.put(SECONDARY_TITLE_RIS, Arrays.asList(subtype));
		}
		if ("User group presentation".equalsIgnoreCase(subtype) || "Professional group".equalsIgnoreCase(subtype)) {
			String audience = findFirstNonBlank(item.getMetadata(AUDIENCE_FIELD));
			String place = findFirstNonBlank(item.getMetadata(PLACE_FIELD));

			if (StringUtils.isNotBlank(audience) || StringUtils.isNotBlank(place)) {
				StringBuilder secondaryTitle = new StringBuilder("Presentation");

				if (StringUtils.isNotBlank(audience)) {
					secondaryTitle.append(" to ").append(audience);
				}
				if (StringUtils.isNotBlank(place)) {
					secondaryTitle.append(" at ").append(place);
				}
				result.put(SECONDARY_TITLE_RIS, Arrays.asList(secondaryTitle.toString()));
			}

		}
		return result;
	}

	private String findFirstNonBlank(DCValue[] values) {
		for (DCValue value : values) {
			if (StringUtils.isNotBlank(value.value)) {
				return value.value;
			}
		}
		return null;
	}

	@Override
	public void init(Properties properties, String converterName) {
		// no config values for this class
	}
}
