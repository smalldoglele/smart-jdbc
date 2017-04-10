package org.smart.jdbc.object;

import java.util.ArrayList;
import java.util.List;

public class TableDefined {

	private String name;

	private List<String[]> uniqueConstraints = new ArrayList<String[]>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String[]> getUniqueConstraints() {
		return uniqueConstraints;
	}

	public void setUniqueConstraints(List<String[]> uniqueConstraints) {
		this.uniqueConstraints = uniqueConstraints;
	}
}
