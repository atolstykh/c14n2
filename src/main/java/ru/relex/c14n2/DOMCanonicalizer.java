package ru.relex.c14n2;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMCanonicalizer {

	private DOMCanonicalizerHandler canonicalizer = null;
	private List<Node> nodes = new ArrayList<Node>();

	public DOMCanonicalizer(Document doc, Parameters params) throws Exception {
		this.nodes.add(doc);
		StringBuffer sb = new StringBuffer();
		canonicalizer = new DOMCanonicalizerHandler(params, sb);
	}

	public DOMCanonicalizer(List<Element> elements, Parameters params)
			throws Exception {
		this.nodes.addAll(elements);
		StringBuffer sb = new StringBuffer();
		canonicalizer = new DOMCanonicalizerHandler(params, sb);
	}

	public String canonicalize() {
		for (Node node : nodes)
			process(node);
		return canonicalizer.getOutputBlock().toString();
	}

	private void process(Node node) {
		switch (node.getNodeType()) {
		case Node.ELEMENT_NODE:
			processElement(node);
			break;
		case Node.TEXT_NODE:
			processText(node);
			break;
		case Node.PROCESSING_INSTRUCTION_NODE:
			processPI(node);
			break;
		case Node.COMMENT_NODE:
			processComment(node);
			break;
		case Node.CDATA_SECTION_NODE:
			processCData(node);
			break;
		}
		if (node.hasChildNodes()) {
			NodeList nl = node.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++)
				process(nl.item(i));
		}

		if (node.getNodeType() == Node.ELEMENT_NODE) {
			processEndElement(node);
		}
	}

	private void processElement(Node node) {
		canonicalizer.processElement(node);
	}

	private void processText(Node node) {
		canonicalizer.processText(node);
	}

	private void processPI(Node node) {
		canonicalizer.processPI(node);
	}

	private void processComment(Node node) {
		canonicalizer.processComment(node);
	}

	private void processCData(Node node) {
		canonicalizer.processCData(node);
	}

	private void processEndElement(Node node) {
		canonicalizer.processEndElement(node);
	}
}