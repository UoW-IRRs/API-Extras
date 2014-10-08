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

	}
}
