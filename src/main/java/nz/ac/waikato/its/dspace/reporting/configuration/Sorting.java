package nz.ac.waikato.its.dspace.reporting.configuration;

import javax.xml.bind.annotation.*;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Sorting {
	@XmlEnum(String.class)
	public enum Order {
		@XmlEnumValue("asc") ASC,
		@XmlEnumValue("desc") DESC
	};


	@XmlAttribute(required = true)
	private String fieldName;
	@XmlElement
	private Order order;

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
}
