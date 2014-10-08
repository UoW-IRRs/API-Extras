package nz.ac.waikato.its.dspace.reporting.configuration;

import javax.xml.bind.annotation.*;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Field {
	@XmlEnum(String.class)
	public enum ValuesMode {
		@XmlEnumValue("all") ALL,
		@XmlEnumValue("pick") PICK,
		@XmlEnumValue("search") SEARCH
	}

	@XmlAttribute(required = true)
	private String name;
	@XmlAttribute
	private String header;
	@XmlElement(defaultValue = "all")
	private ValuesMode valuesMode;
	@XmlElement
	private PostProcess postProcess;

	public Field() {

	}

	public Field(String name, ValuesMode valuesMode) {
		this.name = name;
		this.valuesMode = valuesMode;
	}

	public Field(String name, String header, ValuesMode valuesMode) {
		this.name = name;
		this.header = header;
		this.valuesMode = valuesMode;
	}

	public Field(String name, String header) {
		this.name = name;
		this.header = header;
	}

	public Field(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public ValuesMode getValuesMode() {
		return valuesMode;
	}

	public void setValuesMode(ValuesMode valuesMode) {
		this.valuesMode = valuesMode;
	}

	public PostProcess getPostProcess() {
		return postProcess;
	}

	public void setPostProcess(PostProcess postProcess) {
		this.postProcess = postProcess;
	}
}
