package nz.ac.waikato.its.dspace.reporting.configuration;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class PostProcess {
	@XmlAttribute(name = "class", required = true)
	private String className;

	@XmlElementWrapper(name = "params")
	@XmlElement(name = "param")
	private List<Param> params = new ArrayList<>();

	public PostProcess() { } // no-arg constructor needed by JAXB

	public PostProcess(String className) {
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public List<Param> getParams() {
		return params;
	}

	public void setParams(List<Param> params) {
		this.params = params;
	}

	public void addParam(Param param) {
		params.add(param);
	}

	public Param getParam(String name) {
		for (Param param : params) {
			if (param.name.equals(name)) {
				return param;
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PostProcess that = (PostProcess) o;

		if (className != null ? !className.equals(that.className) : that.className != null) return false;
		if (params != null ? !params.equals(that.params) : that.params != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = className != null ? className.hashCode() : 0;
		result = 31 * result + (params != null ? params.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "PostProcess{" +
				       "className='" + className + '\'' +
				       ", params=" + params +
				       '}';
	}

	public static class Param {
		@XmlAttribute(required = true)
		private String name;
		@XmlAttribute(required = true)
		private String value;

		public Param() { } // no-arg constructor needed for JAXB

		public Param(String name, String value) {
			this.name = name;
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Param param = (Param) o;

			if (!name.equals(param.name)) return false;
			if (!value.equals(param.value)) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = name.hashCode();
			result = 31 * result + value.hashCode();
			return result;
		}

		@Override
		public String toString() {
			return "Param{" +
					       "name='" + name + '\'' +
					       ", value='" + value + '\'' +
					       '}';
		}
	}
}
