package ru.relex.c14n2;

import java.util.ArrayList;
import java.util.List;

public class Parameters {
	public static String SEQUENTIAL = "sequential";
	public static String NONE = "none";

	private boolean ignoreComments = true;
	private boolean trimTextNodes = false;
	private String prefixRewrite = NONE;
	private boolean sortAttributes = true;
	private List<QNameAwareParameter> QnameAwareAttributes = new ArrayList<QNameAwareParameter>();
	private List<QNameAwareParameter> QnameAwareElements = new ArrayList<QNameAwareParameter>();
	private List<QNameAwareParameter> QnameAwareXPathElements = new ArrayList<QNameAwareParameter>();

	public boolean isIgnoreComments() {
		return ignoreComments;
	}

	public void setIgnoreComments(boolean ignoreComments) {
		this.ignoreComments = ignoreComments;
	}

	public boolean isTrimTextNodes() {
		return trimTextNodes;
	}

	public void setTrimTextNodes(boolean trimTextNodes) {
		this.trimTextNodes = trimTextNodes;
	}

	public String getPrefixRewrite() {
		return prefixRewrite;
	}

	public void setPrefixRewrite(String prefixRewrite) {
		this.prefixRewrite = prefixRewrite;
	}

	public boolean isSortAttributes() {
		return sortAttributes;
	}

	public void setSortAttributes(boolean sortAttributes) {
		this.sortAttributes = sortAttributes;
	}

	public List<QNameAwareParameter> getQnameAwareAttributes() {
		return QnameAwareAttributes;
	}

	public void setQnameAwareAttributes(
			List<QNameAwareParameter> qnameAwareAttributes) {
		QnameAwareAttributes = qnameAwareAttributes;
	}

	public List<QNameAwareParameter> getQnameAwareElements() {
		return QnameAwareElements;
	}

	public void setQnameAwareElements(
			List<QNameAwareParameter> qnameAwareElements) {
		QnameAwareElements = qnameAwareElements;
	}

	public List<QNameAwareParameter> getQnameAwareXPathElements() {
		return QnameAwareXPathElements;
	}

	public void setQnameAwareXPathElements(
			List<QNameAwareParameter> qnameAwareXPathElements) {
		QnameAwareXPathElements = qnameAwareXPathElements;
	}
}