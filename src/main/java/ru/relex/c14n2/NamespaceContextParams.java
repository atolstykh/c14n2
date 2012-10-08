package ru.relex.c14n2;

class NamespaceContextParams {
	private String uri = "";
	private String prefix = "";
	private int depth = 1;
	private String newPrefix = "";
	private Boolean hasOutput = null;

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Boolean isHasOutput() {
		return hasOutput;
	}

	public void setHasOutput(Boolean hasOutput) {
		this.hasOutput = hasOutput;
	}

	public String getNewPrefix() {
		return newPrefix;
	}

	public void setNewPrefix(String newPrefix) {
		this.newPrefix = newPrefix;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public NamespaceContextParams clone() {
		NamespaceContextParams ncp = new NamespaceContextParams();
		ncp.depth = depth;
		ncp.hasOutput = hasOutput;
		ncp.newPrefix = newPrefix;
		ncp.prefix = prefix;
		ncp.uri = uri;
		return ncp;
	}

	public void set(String uri, boolean hasOutput, String newPrefix, int depth) {
		setUri(uri);
		setHasOutput(hasOutput);
		setNewPrefix(newPrefix);
		setPrefix(newPrefix);
		setDepth(depth);
	}
}