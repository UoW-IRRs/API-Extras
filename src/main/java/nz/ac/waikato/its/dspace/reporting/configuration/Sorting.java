package nz.ac.waikato.its.dspace.reporting.configuration;

import javax.xml.bind.annotation.*;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for AgResearch
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Sorting {
	@XmlEnum(String.class)
	public enum Order {
		@XmlEnumValue("asc") ASC,
		@XmlEnumValue("desc") DESC
	}


	@XmlAttribute(required = true)
	private String fieldName;
	@XmlElement(defaultValue = "asc")
	private Order order = Order.ASC;

	public Sorting() {

	}

	public Sorting(String fieldName) {
		this(fieldName, Order.ASC);
	}

	public Sorting(String fieldName, Order order) {
		this.order = order;
		this.fieldName = fieldName;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Sorting sorting = (Sorting) o;

		if (!fieldName.equals(sorting.fieldName)) return false;
		if (order != sorting.order) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = fieldName.hashCode();
		result = 31 * result + order.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "Sorting{" +
				       "fieldName='" + fieldName + '\'' +
				       ", order=" + order +
				       '}';
	}
}
