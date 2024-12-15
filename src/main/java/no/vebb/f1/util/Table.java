package no.vebb.f1.util;

import java.util.List;
import java.util.Collections;

public class Table {

	private final String name;
	private final List<String> header;
	private final List<List<String>> body;

	public Table(String name, List<String> header, List<List<String>> body) {
		this.name = name;
		this.header = Collections.unmodifiableList(header);
		this.body = Collections.unmodifiableList(body);
	}

	public String getName() {
		return name;
	}

	public List<String> getHeader() {
		return header;
	}

	public List<List<String>> getBody() {
		return body;
	}
}
