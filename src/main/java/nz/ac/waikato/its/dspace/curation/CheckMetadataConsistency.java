package nz.ac.waikato.its.dspace.curation;

import org.apache.commons.lang.StringUtils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.ItemIterator;
import org.dspace.content.Metadatum;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the UoW Institutional Research Repositories
 */
public class CheckMetadataConsistency extends AbstractCurationTask {
	private List<String> fields;
	private int threshold;

	@Override
	public void init(Curator curator, String taskId) throws IOException {
		super.init(curator, taskId);

		fields = new ArrayList<>();
		String fieldsProperty = taskProperty("fields");
		if (!StringUtils.isBlank(fieldsProperty)) {
			Collections.addAll(fields, fieldsProperty.split(",\\s*"));
		}

		threshold = taskIntProperty("threshold", 5); // TODO per-field threshold?
	}

	@Override
	public int perform(DSpaceObject dso) throws IOException {
		if (dso == null || !(dso instanceof Item)) {
			return Curator.CURATE_SKIP;
		}

		if (fields.isEmpty()) {
			String message = "No field set up for task " + super.taskId;
			report(message);
			setResult(message);
			return Curator.CURATE_SKIP;
		}

		Item item = (Item) dso;
		List<String> inconsistent = new ArrayList<>();
		try {
			for (String field : fields) {
				if (!checkConsistent(item, field, threshold)) {
					inconsistent.add(field);
				}
			}
		} catch (SQLException | AuthorizeException e) {
			String message = "Exception encountered while checking metadata consistency for item id=" + item.getID() + ": " + e.getMessage();
			report(message);
			setResult(message);
			return Curator.CURATE_ERROR;
		}

		if (inconsistent.isEmpty()) {
			String message = "Metadata for item id=" + item.getID() + " is consistent for field/s " + fields.toString();
			setResult(message);
			return Curator.CURATE_SUCCESS;
		} else {
			List<String> consistent = new ArrayList<>();
			consistent.addAll(fields);
			consistent.removeAll(inconsistent);
			String message = "Inconsistencies in metadata for item id=" + item.getID() + " for field/s " + inconsistent.toString() + "; consistent for fields " + consistent.toString();
			report(message);
			setResult(message);
			return Curator.CURATE_FAIL;
		}
	}

	private boolean checkConsistent(Item item, String field, int threshold) throws SQLException, IOException, AuthorizeException {
		String[] components = field.split("\\.");
		if (components.length < 2 || components.length > 3) {
			throw new IllegalArgumentException("Not a valid metadata field name: " + field);
		}
		ItemIterator itemsWithField = Item.findByMetadataField(Curator.curationContext(), components[0], components[1], components.length > 2 ? components[2] : null, Item.ANY);

		Map<String, Integer> itemsWithSameValue = new HashMap<>();
		Map<String, Set<String>> similarValues = new HashMap<>();


		Metadatum[] thisMetadata = item.getMetadataByMetadataString(field);

		while (itemsWithField.hasNext()) {
			Item otherItem = itemsWithField.next();
			if (otherItem.getID() == item.getID()) {
				continue;
			}

			Metadatum[] otherMetadata = otherItem.getMetadataByMetadataString(field);
			for (Metadatum thisMd : thisMetadata) {
				for (Metadatum otherMD : otherMetadata) {
					if (thisMd.value.equals(otherMD.value)) {
						incrementCount(itemsWithSameValue, thisMd.value);
					} else if (StringUtils.getLevenshteinDistance(thisMd.value, otherMD.value) < threshold) {
						addSimilarValue(similarValues, thisMd.value, otherMD.value);
					}
				}
			}
			otherItem.decache();
		}

		boolean allValuesConsistent = true;
		for (Metadatum thisMd : thisMetadata) {
			if (!similarValues.containsKey(thisMd.value) || similarValues.get(thisMd.value) == null || similarValues.get(thisMd.value).isEmpty()) {
				continue; // no similar values recorded at all -> this value must be consistent
			}
			int maxCount = 0;
			String bestValue = null;
			Set<String> allSimilar = similarValues.get(thisMd.value);
			for (String similar : allSimilar) {
				ItemIterator itemsWithThatValue = Item.findByMetadataField(Curator.curationContext(), components[0], components[1], components.length > 2 ? components[2] : null, similar);
				int count = countItems(itemsWithThatValue);
				if (count > maxCount) {
					maxCount = count;
					bestValue = similar;
				}
			}
			int sameCount = itemsWithSameValue.containsKey(thisMd.value) ? itemsWithSameValue.get(thisMd.value) : 0;
			if (maxCount > sameCount) {
				allValuesConsistent = false;
				report("Item id=" + item.getID() + ", field=" + field + ", value=" + thisMd.value + ": a better value may be " + bestValue
						       + ", it is used for " + maxCount + " other item/s while " + thisMd.value + " is used for " + sameCount + " item/s");
			}
		}

		return allValuesConsistent;
	}

	private int countItems(ItemIterator itemIterator) throws SQLException {
		int result = 0;
		while (itemIterator.hasNext()) {
			itemIterator.nextID();
			result++;
		}
		return result;
	}

	private void addSimilarValue(Map<String, Set<String>> map, String value, String similar) {
		if (!map.containsKey(value)) {
			map.put(value, new HashSet<String>());
		}

		Set<String> similarValues = map.get(value);
		similarValues.add(similar);
	}

	private void incrementCount(Map<String, Integer> valueCountMap, String value) {
		Integer currentValue = 0;
		if (valueCountMap.containsKey(value)) {
			currentValue = valueCountMap.get(value);
		}
		valueCountMap.put(value, currentValue + 1);
	}
}
