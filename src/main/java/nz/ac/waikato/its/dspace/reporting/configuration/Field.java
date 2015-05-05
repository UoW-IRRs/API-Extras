package nz.ac.waikato.its.dspace.reporting.configuration;

import javax.xml.bind.annotation.*;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for AgResearch
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
	@XmlAttribute
	private ValuesMode valuesMode = ValuesMode.ALL;
	@XmlElement
	private PostProcess postProcess;

	public Field() {

	}

	public Field(String name, ValuesMode valuesMode) {
		this(name, null, valuesMode);
	}

	public Field(String name, String header, ValuesMode valuesMode) {
		this.name = name;
		this.header = header;
		this.valuesMode = valuesMode != null ? valuesMode : ValuesMode.ALL;
	}

	public Field(String name, String header) {
		this(name, header, null);
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
		this.valuesMode = valuesMode != null ? valuesMode : ValuesMode.ALL;
	}

	public PostProcess getPostProcess() {
		return postProcess;
	}

	public void setPostProcess(PostProcess postProcess) {
		this.postProcess = postProcess;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Field field = (Field) o;

		if (header != null ? !header.equals(field.header) : field.header != null) return false;
		if (!name.equals(field.name)) return false;
		if (postProcess != null ? !postProcess.equals(field.postProcess) : field.postProcess != null) return false;
		if (valuesMode != field.valuesMode) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + (header != null ? header.hashCode() : 0);
		result = 31 * result + (valuesMode != null ? valuesMode.hashCode() : 0);
		result = 31 * result + (postProcess != null ? postProcess.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "Field{" +
				       "name='" + name + '\'' +
				       ", header='" + header + '\'' +
				       ", valuesMode=" + valuesMode +
				       ", postProcess=" + postProcess +
				       '}';
	}
}
