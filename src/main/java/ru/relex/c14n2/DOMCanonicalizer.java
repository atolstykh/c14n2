package ru.relex.c14n2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * C14N2 canonicalization.
 */
public class DOMCanonicalizer {

  private DOMCanonicalizerHandler canonicalizer = null;
  private Document doc = null;
  private List<Node> nodes = new ArrayList<Node>();
  private List<Node> includeList = null;

  /**
   * Constructor.
   * 
   * @param doc
   *          DOM document
   * @param includeList
   *          inclusion list
   * @param excludeList
   *          exclusion list
   * @param params
   *          canonicalization parameters
   * 
   * @throws Exception
   */
  private DOMCanonicalizer(Document doc, List<Node> includeList,
      List<Node> excludeList, Parameters params) throws Exception {
    if (doc == null) {
      throw new NullPointerException();
    }

    this.includeList = includeList != null && includeList.isEmpty() ? null
        : includeList;
    this.doc = doc;
    StringBuffer sb = new StringBuffer();
    canonicalizer = new DOMCanonicalizerHandler(
        params == null ? new Parameters() : params, excludeList != null
            && excludeList.isEmpty() ? null : excludeList, sb);
  }

  /**
   * Constructor.
   * 
   * @param doc
   *          DOM document
   * @param params
   *          canonicalization parameters
   * 
   * @throws Exception
   */
  public static String canonicalize(Document doc, Parameters params)
      throws Exception {
    return canonicalize(doc, null, null, params);
  }

  /**
   * Constructor.
   * 
   * @param doc
   *          DOM document
   * @param includeList
   *          inclusion list
   * @param params
   *          canonicalization parameters
   * 
   * @throws Exception
   */
  public static String canonicalize(Document doc, List<Node> includeList,
      Parameters params) throws Exception {
    return canonicalize(doc, includeList, null, params);
  }

  /**
   * Canonicalization method.
   * 
   * @param doc
   *          DOM document
   * @param includeList
   *          inclusion list
   * @param excludeList
   *          exclusion list
   * @param params
   *          canonicalization parameters
   * 
   * @return Returns the canonical form of an XML document
   * 
   * @throws Exception
   */
  public static String canonicalize(Document doc, List<Node> includeList,
      List<Node> excludeList, Parameters params) throws Exception {
    return new DOMCanonicalizer(doc, includeList, excludeList, params)
        .canonicalizeSubTree();
  }

  /**
   * Canonicalizing of subtree.
   * 
   * @return Returns the canonical form of a subtree
   * 
   * @throws Exception
   */
  private String canonicalizeSubTree() throws Exception {
    if (includeList == null) {
      process(doc);
    } else {
      processIncludeList();
      while (nodes.size() > 0) {
        process(nodes.get(0));
      }
    }
    return canonicalizer.getOutputBlock().toString();
  }

  /**
   * Processing (sorting) a inclusion list.
   */
  private void processIncludeList() {
    List<Node> allNodes = new ArrayList<Node>();
    for (Node node : includeList) {
      Node n = node;
      do {
        if (!allNodes.contains(n)) {
          allNodes.add(n);
        }
        n = n.getParentNode();
      } while (n != null);
    }
    Collections.sort(allNodes, new Comparator<Node>() {
      @Override
      public int compare(Node n1, Node n2) {
        int l1 = canonicalizer.getNodeDepth(n1);
        int l2 = canonicalizer.getNodeDepth(n2);
        if (l1 != l2) {
          return l1 - l2;
        } else {
          Node prnt1 = n1.getParentNode();
          Node prnt2 = n2.getParentNode();
          if (prnt1 == null) {
            return -1;
          } else if (prnt2 == null) {
            return 1;
          }
          if (prnt1.equals(prnt2)) {
            NodeList nl = prnt1.getChildNodes();
            l1 = -1;
            l2 = -1;
            for (int i = 0; i < nl.getLength(); i++) {
              if (n1.equals(nl.item(i))) {
                l1 = i;
              } else if (n2.equals(nl.item(i))) {
                l2 = i;
              }
              if (l1 != -1 && l2 != -1) {
                break;
              }
            }
            return l1 - l2;
          } else {
            return compare(prnt1, prnt2);
          }
        }
      }
    });
    nodes = allNodes;
  }

  /**
   * Processing a node.
   * 
   * @param node
   *          DOM node
   */
  private void process(Node node) {
    if (canonicalizer.isInExcludeList(node))
      return;

    switch (node.getNodeType()) {
    case Node.ELEMENT_NODE:
      canonicalizer.processElement(node);
      break;
    case Node.TEXT_NODE:
      canonicalizer.processText(node);
      break;
    case Node.PROCESSING_INSTRUCTION_NODE:
      canonicalizer.processPI(node);
      break;
    case Node.COMMENT_NODE:
      canonicalizer.processComment(node);
      break;
    case Node.CDATA_SECTION_NODE:
      canonicalizer.processCData(node);
      break;
    }
    if (nodes.size() > 0 && node.equals(nodes.get(0))) {
      nodes.remove(0);
    }
    if (node.hasChildNodes()) {
      boolean b = nodes.size() > 0 && node.equals(nodes.get(0).getParentNode());
      NodeList nl = node.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++) {
        if (!b || (nodes.size() > 0 && nl.item(i).equals(nodes.get(0)))) {
          process(nl.item(i));
        }
      }
    }

    if (node.getNodeType() == Node.ELEMENT_NODE) {
      canonicalizer.processEndElement(node);
    }
  }
}