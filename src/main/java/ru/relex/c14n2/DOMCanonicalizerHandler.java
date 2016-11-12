package ru.relex.c14n2;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.apache.commons.lang3.StringUtils;
import ru.relex.c14n2.util.NSDeclaration;
import ru.relex.c14n2.util.PrefixesContainer;

/**
 * C14N2 canonicalizer.
 */
class DOMCanonicalizerHandler {
  private static final Logger LOGGER = LoggerFactory
      .getLogger(DOMCanonicalizerHandler.class);

  private static final String EMPTY_URI = "";
  private static final String EMPTY_PREFIX = "";
  private static final String XMLNS = "xmlns";
  private static final String XML = "xml";
  private static final String XSD = "xsd";

  private static final String CF = "&#x%s;";
  private static final String C = ":";
  private static final int ID_ARRAY_CAPACITY = 20;

  private List<Node> excludeList;
  private Parameters parameters;
  private StringBuilder outputBuffer;
  private int nodeDepth = 0;
  private int[] nextIdArray;


  //
  //       firstKey - prefix
  //       secondKey - url
  private PrefixesContainer declaredPrefixes;

  //
  // PrefixRewrite none:
  //       firstKey - prefix
  //       secondKey - url
  // PrefixRewrite sequence:
  //       firstKey - url
  //       secondKey - prefix
  private PrefixesContainer usedPrefixes;



  private boolean bSequential = false;

  private Map<String, NSContext> xpathesNsMap = new HashMap<String, NSContext>();

  /**
   * Constructor.
   * 
   * @param parameters
   *          canonicalization parameters
   * @param excludeList
   *          inclusion list
   * @param outputBuffer
   *          output
   */
  protected DOMCanonicalizerHandler(Node node, Parameters parameters,
      List<Node> excludeList, StringBuilder outputBuffer) {
    this.parameters = parameters;
    this.outputBuffer = outputBuffer;
    this.excludeList = excludeList;
    this.declaredPrefixes  = new PrefixesContainer();
    this.usedPrefixes = new PrefixesContainer();

    bSequential = parameters.getPrefixRewrite().equals(Parameters.SEQUENTIAL);

    // The default namespace is declared by xmlns="...". To make the algorithm simpler this will be treated as a
    // namespace declaration whose prefix value is "" i.e. an empty string.
    declaredPrefixes.definePrefix("","",0);
    if (bSequential) {
      nextIdArray = new int[ID_ARRAY_CAPACITY]; // check
      nextIdArray[0]=0;
    }

    loadParentNamespaces(node);
  }

  /**
   * Prosessing of element node.
   * 
   * @param node
   *          element node
   */
  protected void processElement(Node node) {
    LOGGER.debug("processElement: {}", node);
    if (isInExcludeList(node))
      return;
    nodeDepth++;
    if (bSequential) {
      if (nextIdArray.length==nodeDepth) {
        int [] newArr = new int [nextIdArray.length+ID_ARRAY_CAPACITY];
        System.arraycopy(nextIdArray,0,newArr,0,nextIdArray.length);
        nextIdArray = newArr;
      }
      nextIdArray[nodeDepth]= nextIdArray[nodeDepth - 1];

    }
    addNamespaces(node);

    SortedSet<NSDeclaration> nsDeclarations = new TreeSet<NSDeclaration>();
    evaluateUriVisibility(node,nsDeclarations);

    // write to outputBuffer

    // startElement

    String nodePrefix = getNodePrefix(node);

    String nodeUri = getNamespaceURIByPrefix(nodePrefix);

    String newPrefix = getNewPrefix(nodeUri,nodePrefix);


    if (newPrefix==null || newPrefix.isEmpty()) {
      outputBuffer.append(String.format("<%s", getLocalName(node)));
    } else {
      outputBuffer.append(String.format("<%s:%s",newPrefix, getLocalName(node)));
    }

    // output namespace nodes
    for (NSDeclaration nsDeclaration :  nsDeclarations) {
      String nsName = nsDeclaration.getPrefix();
      String nsUri = nsDeclaration.getUri();
      if (!nsName.equals(EMPTY_URI)) {
        outputBuffer.append(String.format(" %s:%s=\"%s\"", XMLNS, nsName, nsUri));
      } else {
        outputBuffer.append(String.format(" %s=\"%s\"", XMLNS, nsUri));
      }
    }

    List<Attribute> outAttrsList = processAttributes(node,nodeUri);

    for (Attribute attribute : outAttrsList) {

      String attrPrfx = getNewPrefix(attribute.getUri(),attribute.getOldPrefix());


      String attrName = attribute.getLocalName();
      String attrValue = attribute.getValue();
      // According to the xml-c14n:
      // "Note: unlike elements, if an attribute doesn't have a prefix, that means it is a locally scoped attribute."
      // but we used attributeFormDefault="qualified"
      if (attrPrfx==null && attribute.getLocalName().startsWith(XML)) {
        // The "xml" and "xmlns" prefixes are reserved and have special behavior
        outputBuffer.append(String.format(" %s=\"%s\"", attrName, attrValue));
      } else {

        if (attrPrfx.isEmpty() && attribute.isAttributeQualified()) {
          if (!nodeUri.equals(attribute.getUri())) {
            LOGGER.error("!!!");
            throw new RuntimeException();
          }
          attrPrfx = newPrefix;
        }

        if (!attribute.isAttributeQualified()) {
          if (!nodeUri.equals(attribute.getUri())) {
            LOGGER.error("!!!");
            throw new RuntimeException();
          }
          attrPrfx = "";
        }

        if (attrPrfx.isEmpty()) {
          outputBuffer.append(String.format(" %s=\"%s\"", attrName, attrValue));
        } else {
          outputBuffer.append(String.format(" %s:%s=\"%s\"", attrPrfx, attrName, attrValue));
        }
      }
    }

    outputBuffer.append(">");
  }

  private String getNewPrefix(String nodeUri, String nodePrefix) {
    if (bSequential) {
      return usedPrefixes.getByFirstKey(nodeUri);
    } else {
      return nodePrefix;
    }
  }


  private String getNamespaceURIByPrefix(String prefix) {
    /*if (bSequential) {
      String uri = declaredPrefixes.getByFirstKey(prefix);
      return uri;
    } else {
      String uri = usedPrefixes.getByFirstKey(prefix);
      if (uri==null) {
        LOGGER.error("BAG!!");
        throw new RuntimeException();
      }
      return uri;
    } */
    String uri = declaredPrefixes.getByFirstKey(prefix);
    if (uri==null) {
      LOGGER.error("BAG!!");
      throw new RuntimeException();
    }
    return uri;

  }

  private List<Attribute> processAttributes(Node node, String nodeUri) {

    // Sort all the attributes in increasing lexicographic order with namespace URI as the primary key and local name
    // as the secondary key (an empty namespace URI is lexicographically least).

    List<Attribute> attributeList = new LinkedList<Attribute>();
    for (int ai = 0; ai < node.getAttributes().getLength(); ai++) {
      Node attr = node.getAttributes().item(ai);


      String suffix = getLocalName(attr);

      String prfxNs = getNodePrefix(attr);

      if (XMLNS.equals(prfxNs)) {
        continue;
      }
      Attribute attribute = new Attribute();
      attribute.setOldPrefix(prfxNs);
      attribute.setLocalName(getLocalName(attr));
      //Note: unlike elements, if an attribute doesn't have a prefix, that means it is a locally scoped attribute.
      if (EMPTY_PREFIX.equals(prfxNs)) {
        attribute.setUri(nodeUri);
        attribute.setAttributeQualified(false);
      } else {
        if (!XML.equals(prfxNs))
          attribute.setUri(getNamespaceURIByPrefix(prfxNs));
        else {
          // xml:space="preserver"
          attribute.setLocalName(XML+":"+suffix);
        }
      }

      String attrValue = attr.getNodeValue() != null ? attr.getNodeValue() : "";
      attrValue = processText(attrValue, true);
      StringBuffer value = new StringBuffer();
      for (int i = 0; i < attrValue.length(); i++) {
        char codepoint = attrValue.charAt(i);
        if (codepoint == 9 || codepoint == 10 || codepoint == 13) {
          value.append(String.format(CF, Integer.toHexString(codepoint)
                  .toUpperCase()));
        } else {
          value.append(codepoint);
        }
      }
      attribute.setValue(value.toString());
      attributeList.add(attribute);
    }

    Comparator<Attribute> comparator = new Comparator<Attribute>() {
      @Override
      public int compare(Attribute attribute, Attribute t1) {
        if (attribute.getUri().equals(t1.getUri())) {
          return attribute.getLocalName().compareTo(t1.getLocalName());
        }else {
          return attribute.getUri().compareTo(t1.getUri());
        }
      }
    };

    Collections.sort(attributeList,comparator);
    return attributeList;

  }

  /**
   * evaluate prefixis for visible uri
   * @return
   */
  /*private void evaluateUriPrefixMap(int nodeDepth) {

    toOuputMap.clear();
    uriPrefixMap.clear();

    for (Map.Entry<String,Map<String,NamespaceContextParams>> entry:namespaces.entrySet()) {
      Map<String,NamespaceContextParams> nMap = entry.getValue();
      for (Map.Entry<String,NamespaceContextParams> ncEntry: nMap.entrySet()) {
        if (ncEntry.getValue().isToOutput()) {
          // url  -key , prefix - value
          toOuputMap.put(entry.getKey(),ncEntry.getValue().getPrefix());
        }
        if (ncEntry.getValue().isHasOutput()) {
          uriPrefixMap.put(entry.getKey(),ncEntry.getValue().getNewPrefix());
        }

      }
    }
    if (bSequential) {
      Integer maxId = nextIdListStack.get(nextIdListStack.size() - 1);
      for (Map.Entry<String,String> s : toOuputMap.entrySet()) {
        if (!s.getKey().isEmpty()) {
          String newPrefix = "n" + maxId;
          uriPrefixMap.put(s.getKey(), newPrefix);
          NamespaceContextParams lastestEntry = namespaces.get(s.getKey()).get(s.getValue());
          lastestEntry.setNewPrefix(newPrefix);
          maxId++;
        } else {
          // ns0
          NamespaceContextParams lastestEntry = namespaces.get(s.getKey()).get(s.getValue());
          uriPrefixMap.put(s.getKey(), lastestEntry.getNewPrefix());
        }
      }
      nextIdListStack.set(nextIdListStack.size()-1,maxId);

    } else {
      for (Map.Entry<String,String> s : toOuputMap.entrySet()) {


        NamespaceContextParams namespaceContextParams = getLastListElement(namespaces.get(s));


        uriPrefixMap.put(s,namespaceContextParams.getPrefix());
        namespaceContextParams.setNewPrefix(namespaceContextParams.getPrefix());
      }
    }

  }*/

  /**
   * Completion of processing element node.
   * 
   * @param node
   *          element node
   */
  protected void processEndElement(Node node) {
    if (isInExcludeList(node))
      return;

    String nodePrefix = getNodePrefix(node);
    String nodeUri = getNamespaceURIByPrefix(nodePrefix);


    String elementPrefix = getNewPrefix(nodeUri,nodePrefix);

    if (elementPrefix==null || elementPrefix.isEmpty()) {
      outputBuffer.append(String.format("</%s>", getLocalName(node)));
    } else {
      outputBuffer.append(String.format("</%s:%s>",elementPrefix, getLocalName(node)));
    }

    removeNamespaces(node);
    nodeDepth--;
  }

  /**
   * Prosessing of text node.
   * 
   * @param node
   *          text node
   */
  
  protected void processText(Node node) {
    LOGGER.debug("processText: {}", node);


    String text = node.getNodeValue() != null ? node.getNodeValue() : "";
    text = processText(text,false);

    StringBuilder value = new StringBuilder();
    for (int i = 0; i < text.length(); i++) {
      char codepoint = text.charAt(i);
      if (codepoint == 13) {
        value.append(String.format(CF, Integer.toHexString(codepoint)
                .toUpperCase()));
      } else {
        value.append(codepoint);
      }
    }
/*    text = value.toString();


    StringBuffer value = new StringBuffer(text.length());
    for (int i = 0; i < text.length(); i++) {
      char codepoint = text.charAt(i);

      if (codepoint == '&') {
    	  value.append(AMP);
      }
      else if (codepoint == '<') {
    	  value.append(LT);
      }
      else if (codepoint == '>') {
    	  value.append(GT);
      }
      else if (codepoint == 0xd) {
    	  value.append(XD);
      }
      else {
        value.append(codepoint);
      }
    } */
    text = value.toString();


    if (parameters.isTrimTextNodes()) {
      boolean b = true;
      NamedNodeMap attrs = node.getParentNode().getAttributes();
      for (int ai = 0; ai < attrs.getLength(); ai++) {
        Node attr = attrs.item(ai);
        if (isInExcludeList(attr))
          continue;
        if (XML.equals(getNodePrefix(attr))
            && "preserve".equals(attr.getNodeValue())
            && getLocalName(attr).equals("space")) {
          b = false;
          break;
        }
      }
      if (b) {
        text = StringUtils.trim(text);
      }
    }

    /*
    if (parameters.getQnameAwareElements().size() > 0 && bSequential) {
      if (text.startsWith(XSD + C)) {
        if (namespaces.containsKey(XSD)) {
          Node prntNode = node.getParentNode();
          String nodeName = getLocalName(prntNode);
          String nodeUri = getNamespaceURIByPrefix(prntNode);
          NamespaceContextParams ncp = getLastElement(XSD);
          for (QNameAwareParameter en : parameters.getQnameAwareElements()) {
            if (nodeName.equals(en.getName())
                && en.getNs().equals(nodeUri)) {
              text = StringUtils.join(ncp.getNewPrefix(), StringUtils.substring(text, XSD.length()));
            }
          }
        }
      }
    }*/
    /*if (parameters.getQnameAwareXPathElements().size() > 0 && bSequential
        && node.getParentNode().getChildNodes().getLength() == 1) {
      Node prntNode = node.getParentNode();
      String nodeName = getLocalName(prntNode);
      String nodeUri = getNamespaceURIByPrefix(prntNode);
      String nodeText = node.getTextContent();
      for (QNameAwareParameter en : parameters.getQnameAwareXPathElements()) {
        if (nodeName.equals(en.getName()) && nodeUri.equals(en.getNs())) {

          // we have
          // this.currentNamespaces and this.namespaces




          NSContext nsContext = xpathesNsMap.get(nodeText);



          List<String> xpathNs = nsContext.getXpathNs();
          StringBuffer sb = new StringBuffer(nodeText.length());
          int baseTextIdx = 0;
          if (xpathNs.size() > 0) {
            Iterator<String> it = xpathNs.iterator();
            String ns = it.next();
            ObjectVector words = nsContext.getWords();
            for (int i = 0; i < words.size(); i++) {
              Object obj = words.elementAt(i);
              String word = obj.toString();
              int idx = StringUtils.indexOf(nodeText, word, baseTextIdx);
              if (idx != baseTextIdx) {
                sb.append(StringUtils.substring(nodeText, baseTextIdx, idx));
                baseTextIdx = idx;
              }
              if (!(obj instanceof XString)
                  && ns.equals(word)
                  && (i != words.size() - 1 && C.equals(words.elementAt(i + 1)))) {
                sb.append(getLastElement(word).getNewPrefix());
                baseTextIdx += word.length();
                if (it.hasNext())
                  ns = it.next();
                else {
                  sb.append(StringUtils.substring(nodeText, baseTextIdx));
                  break;
                }
              } else {
                sb.append(word);
                baseTextIdx += word.length();
              }
            }
            text = sb.toString();
          }
        }
      }
    }*/

    outputBuffer.append(text);
  }

  /**
   * Prosessing of process instruction node.
   * 
   * @param node
   *          process instruction node
   */
  protected void processPI(Node node) {
    /*LOGGER.debug("processPI: {}", node);
    String nodeName = node.getNodeName();
    String nodeValue = node.getNodeValue() != null ? node.getNodeValue() : "";

    if (bEnd && getNodeDepth(node) == 1) {
      outputBuffer.append("\n");
    }
    outputBuffer.append(String.format("<?%s%s?>", nodeName,
        !nodeValue.isEmpty() ? (" " + nodeValue) : ""));
    if (bStart && getNodeDepth(node) == 1) {
      outputBuffer.append("\n");
    }*/

  }

  /**
   * Prosessing of comment node.
   * 
   * @param node
   *          comment node
   */
  protected void processComment(Node node) {
    /*LOGGER.debug("processComment: {}", node);
    if (parameters.isIgnoreComments())
      return;

    if (bEnd && getNodeDepth(node) == 1) {
      outputBuffer.append("\n");
    }
    outputBuffer.append(String.format("<!--%s-->", node.getNodeValue()));
    if (bStart && getNodeDepth(node) == 1) {
      outputBuffer.append("\n");
    } */

  }

  /**
   * Prosessing of CDATA node.
   * 
   * @param node
   *          CDATA node
   */
  protected void processCData(Node node) {
    LOGGER.debug("processCData:" + node);
    outputBuffer.append(processText(node.getNodeValue(), false));

  }

  /**
   * Returns an output buffer.
   * 
   * @return Returns an output buffer
   */
  protected StringBuilder getOutputBlock() {
    return outputBuffer;
  }

  /**
   * Returns whether a node in the exclusion list.
   * 
   * @param node
   *          DOM node
   * 
   * @return Returns true if a node there is in exclusion list, false -
   *         otherwise
   */
  protected boolean isInExcludeList(Node node) {
    if (excludeList != null
        && excludeList.contains(node)
        && (node.getNodeType() == Node.ELEMENT_NODE || node instanceof Attr)
        && !(node instanceof Attr && (XMLNS.equals(getNodePrefix(node)) || XML
            .equals(getNodePrefix(node)))))
      return true;
    return false;
  }


  /**
   * Returns whether there is a prefix in the parent output.
   *
   * @param prfx
   *          prefix
   *
   * @return Returns true if a prefix there is in parent output, false -
   *         otherwise
   */
  /*private boolean outputNSInParent(String prfx) {
    for (Entry<String, List<NamespaceContextParams>> en : namespaces.entrySet()) {
      if (!bSequential && !prfx.equals(en.getKey()))
        continue;
      List<NamespaceContextParams> lst = en.getValue();
      if (lst.size() > 1) {
        NamespaceContextParams last = getLastElement(prfx);
        for (int i = 2; i <= lst.size(); i++) {
          NamespaceContextParams prev = getLastElement(en.getKey(), -i);
          if (last.getNewPrefix().equals(prev.getNewPrefix())) {
            if (!bSequential && !last.getUri().equals(prev.getUri()))
              return false;
            else if (prev.isHasOutput() == null || prev.isHasOutput())
              return true;
          }
        }
      }
    }
    return false;
  }*/

  /**
   * Remove unused namespaces from the stack.
   * 
   * @param node
   *          DOM node
   */
  private void removeNamespaces(Node node) {

    usedPrefixes.deleteLevel(nodeDepth);
    declaredPrefixes.deleteLevel(nodeDepth);

/*
    for (Iterator<Map.Entry<String, List<NamespaceContextParams>>> it = namespaces.entrySet().iterator(); it.hasNext(); ) {


      Map.Entry<String, List<NamespaceContextParams>> entry = it.next();
      List<NamespaceContextParams> nsLevels = entry.getValue();

      while (!nsLevels.isEmpty() && nsLevels.get(nsLevels.size() - 1).getDefinitionDepth() >= nDepth) {
        nsLevels.remove(nsLevels.size() - 1);
      }

      if (nsLevels.isEmpty()) {
    	  it.remove(); // java.util.ConcurrentModificationException ???
      } else {
        NamespaceContextParams theLastElement = getLastListElement(nsLevels);
        if (theLastElement.getOutputDepth()>=nDepth){
          theLastElement.setHasOutput(false);
          theLastElement.setToOutput(false);
        }
      }
    }

    for (Iterator<Map.Entry<String, List<PrefixContextParams>>> it = currentNamespaces.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry<String, List<PrefixContextParams>> entry = it.next();
      List<PrefixContextParams> nsLevels = entry.getValue();
      while (!nsLevels.isEmpty() &&
              nsLevels.get(nsLevels.size() - 1).getDepth() >= nDepth) {

        nsLevels.remove(nsLevels.size() - 1);
      }
      if (nsLevels.isEmpty()) {
        it.remove();
      }
    }*/


  }

  /**
   * Prosessing of node attributes.
   * 
   * @param node
   *          DOM node
   * 
   * @return Returns a list of output attributes
   */
  private void evaluateUriVisibility(final Node node, SortedSet<NSDeclaration> nsDeclarations) {

    String nodePrf = getNodePrefix(node);
    String nodeUri = getNamespaceURIByPrefix(nodePrf);

    if (bSequential) {
      //       firstKey - url
      //       secondKey - prefix
      if (usedPrefixes.getByFirstKey(nodeUri) == null) {
        int nextId = nextIdArray[nodeDepth];
        String newPrefix = "n" + nextId;
        nextIdArray[nodeDepth]=nextId + 1;
        usedPrefixes.definePrefix(nodeUri, newPrefix, nodeDepth);
        NSDeclaration nsDeclaration = new NSDeclaration();
        nsDeclaration.setUri(nodeUri);
        nsDeclaration.setPrefix(newPrefix);
        nsDeclarations.add(nsDeclaration);
      }
    } else {
      //       firstKey - prefix
      //       secondKey - url
      if (usedPrefixes.getByFirstKey(nodePrf) == null) {
        usedPrefixes.definePrefix(nodePrf, nodeUri, nodeDepth);
        // xak for xmlns=""
        if (!(EMPTY_PREFIX.equals(nodePrf) && EMPTY_URI.equals(nodeUri))) {
          NSDeclaration nsDeclaration = new NSDeclaration();
          nsDeclaration.setUri(nodeUri);
          nsDeclaration.setPrefix(nodePrf);
          nsDeclarations.add(nsDeclaration);
        }
      }

    }
    for (int ai = 0; ai < node.getAttributes().getLength(); ai++) {
      Node attr = node.getAttributes().item(ai);
      if (isInExcludeList(attr)) continue;
      String prfx = getNodePrefix(attr);
      if (!XMLNS.equals(prfx)) {

        String attrNamespaceURI;
        //Note: unlike elements, if an attribute doesn't have a prefix, that means it is a locally scoped attribute.
        if (EMPTY_PREFIX.equals(prfx)) {
          attrNamespaceURI = nodeUri;
        } else {
          attrNamespaceURI = getNamespaceURIByPrefix(prfx);
        }

        if (bSequential) {
          if (usedPrefixes.getByFirstKey(attrNamespaceURI) == null) {
            int nextId = nextIdArray[nodeDepth];
            String newPrefix = "n" + nextId;
            nextIdArray[nodeDepth]=nextId + 1;
            usedPrefixes.definePrefix(attrNamespaceURI, newPrefix, nodeDepth);
            NSDeclaration nsDeclaration = new NSDeclaration();
            nsDeclaration.setUri(attrNamespaceURI);
            nsDeclaration.setPrefix(newPrefix);
            nsDeclarations.add(nsDeclaration);
          }
        } else {
          if (usedPrefixes.getByFirstKey(prfx) == null) {
            usedPrefixes.definePrefix(prfx, attrNamespaceURI, nodeDepth);
            NSDeclaration nsDeclaration = new NSDeclaration();
            nsDeclaration.setUri(attrNamespaceURI);
            nsDeclaration.setPrefix(prfx);
            nsDeclarations.add(nsDeclaration);
          }

        }
      }

/*    Collections.sort(outAttrsList, new Comparator<Attribute>() {
      public int compare(Attribute x, Attribute y) {
        String x_uri, y_uri;
        if (XML.equals(x.getUri())) {
          x_uri = node.lookupNamespaceURI(XML);
        } else {
          NamespaceContextParams x_stack = getLastElement(x.getUri());
          x_uri = x_stack != null ? x_stack.getUri() : "";
        }
        if (XML.equals(y.getUri())) {
          y_uri = node.lookupNamespaceURI(XML);
        } else {
          NamespaceContextParams y_stack = getLastElement(y.getUri());
          y_uri = y_stack != null ? y_stack.getUri() : "";
        }
        return String.format("%s:%s", x_uri, x.getLocalName()).compareTo(
            String.format("%s:%s", y_uri, y.getLocalName()));
      }
    });

    return outAttrsList; */
    }


    for (QNameAwareParameter en : parameters.getQnameAwareElements()) {
      String nodeLocalName = getLocalName(node);

      // 1. If there is an Element subchild, whose Name and NS attributes match E's localname and namespace
      // respectively, then E is expected to have a single text node child containing a QName.
      // Extract the prefix from this QName, and consider this prefix as visibly utilized.

      if (nodeLocalName.equals(en.getName())
              && nodeUri.equals(en.getNs())) {
        String text = node.getTextContent();
        int idx = text.indexOf(C);

        String prefix = "";
        if (idx > -1) {
          prefix = StringUtils.substring(text, 0, idx);
        }

        String textUri = getNamespaceURIByPrefix(prefix);

        if (bSequential) {
          //       firstKey - url
          //       secondKey - prefix
          if (usedPrefixes.getByFirstKey(textUri) == null) {
            int nextId = nextIdArray[nodeDepth];
            String newPrefix = "n" + nextId;
            nextIdArray[nodeDepth]=nextId + 1;
            usedPrefixes.definePrefix(nodeUri, newPrefix, nodeDepth);
            NSDeclaration nsDeclaration = new NSDeclaration();
            nsDeclaration.setUri(nodeUri);
            nsDeclaration.setPrefix(newPrefix);
            nsDeclarations.add(nsDeclaration);
          }
        } else {
          //       firstKey - prefix
          //       secondKey - url
          if (usedPrefixes.getByFirstKey(prefix) == null) {
            usedPrefixes.definePrefix(prefix, textUri, nodeDepth);
            NSDeclaration nsDeclaration = new NSDeclaration();
            nsDeclaration.setUri(textUri);
            nsDeclaration.setPrefix(prefix);
            nsDeclarations.add(nsDeclaration);
          }
        }
      }

    }

  }





  /**
   * Prosessing of namespace attributes.
   * 
   * @param node
   *          DOM node
   * 
   * @return Returns a list of output namespace attributes
   */
 /* private SortedSet<NamespaceContextParams> processNamespaces(Node node) {


    Comparator<NamespaceContextParams> comparator;
    if (bSequential) {
      comparator = new Comparator<NamespaceContextParams>() {
        public int compare(NamespaceContextParams x, NamespaceContextParams y) {
          return x.getUri().compareTo(y.getUri());
        }
      };
    } else {
      comparator = new Comparator<NamespaceContextParams>() {
        public int compare(NamespaceContextParams x, NamespaceContextParams y) {
          return x.getPrefix().compareTo(y.getPrefix());
        }
      };
    }

    SortedSet<NamespaceContextParams> outNSList = new TreeSet<NamespaceContextParams>(comparator);

    NamespaceContextParams namespaceContextParams = getLastElement(getNodePrefix(node));
    outNSList.add(namespaceContextParams);

    for (int ni = 0; ni < node.getAttributes().getLength(); ni++) {
      Node attr = node.getAttributes().item(ni);
      if (isInExcludeList(attr))
        continue;
      outNSList.add(getLastElement(getNodePrefix(node))); // set !!
    }

    return outNSList;
  }*/

  /**
   * Add namespaces to stack.
   * 
   * @param node
   *          DOM node
   */
  private void addNamespaces(Node node) {

    for (int ni = 0; ni < node.getAttributes().getLength(); ni++) {
      Node attr = node.getAttributes().item(ni);
      if (isInExcludeList(attr))
        continue;
      String suffix = getLocalName(attr);
      String prfxNs = getNodePrefix(attr);

      if (XMLNS.equals(prfxNs) ) {
        String uri = attr.getNodeValue();
        declaredPrefixes.definePrefix(suffix,uri,nodeDepth);
      }
    }

  }
/*

  private void addQNameAwareNamespaces(Node node) {
    for (QNameAwareParameter en : parameters.getQnameAwareElements()) {
      String nodeLocalName = getLocalName(node);
      String nodeUri = getNamespaceURIByPrefix(node);

      // 1. If there is an Element subchild, whose Name and NS attributes match E's localname and namespace
      // respectively, then E is expected to have a single text node child containing a QName.
      // Extract the prefix from this QName, and consider this prefix as visibly utilized.

      if (nodeLocalName.equals(en.getName())
              && nodeUri.equals(en.getNs())) {
        String text = node.getTextContent();
        int idx = text.indexOf(C);

        String prefix = "";
        if (idx > -1) {
          prefix = StringUtils.substring(text, 0, idx);
        }



        // we have to know the prefix at this moment !!
        List<PrefixContextParams> currentStack =currentNamespaces.get(prefix);
        String textUri = getLastCurrentListElement(currentStack).getUri();

        if (!namespaces.containsKey(textUri)) {
          Map<String,NamespaceContextParams> nMap = new HashMap<String,NamespaceContextParams>();
          NamespaceContextParams namespaceContextParams = new NamespaceContextParams();
          namespaceContextParams.setToOutput(true);
          namespaceContextParams.setPrefix(prefix);
          namespaceContextParams.setDefinitionDepth(getNodeDepth(node));
          nMap.put(prefix,namespaceContextParams);
          namespaces.put(textUri,nMap);
        } else {
          if (!bSequential) {
            Map<String,NamespaceContextParams> nMap = namespaces.get(textUri);
            if (!nMap.containsKey(prefix)) {
              NamespaceContextParams namespaceContextParams = new NamespaceContextParams();
              namespaceContextParams.setToOutput(true);
              namespaceContextParams.setPrefix(prefix);
              namespaceContextParams.setDefinitionDepth(getNodeDepth(node));
              nMap.put(prefix,namespaceContextParams);
            }
          }
        }
      }

    }

  }*/

  /**
   * Returns whether to show the prefix in the output of the node.
   * 
   * @param node
   *          DOM node
   * @param prefix
   *          prefix
   * 
   * @return Returns true if prefix is shown in the output of the node, false -
   *         otherwise.
   */
 /* private boolean isPrefixVisible(Node node, String prefix, String childText, String nPrefix) {

    if (nPrefix.equals(prefix)) {
      return true;
    }

    String nodeLocalName = getLocalName(node);
    if (parameters.getQnameAwareElements().size() > 0) {
      NamespaceContextParams ncp = getLastElement(prefix);
      String prfx = ncp.getPrefix();
      if (childText == null) {
    	  childText = node.getTextContent();
      }
      if (childText != null && childText.startsWith(prfx + C)
          && node.getChildNodes().getLength() == 1) {
        NamespaceContextParams attrPrfxNcp = getLastElement(nPrefix);
        for (QNameAwareParameter en : parameters.getQnameAwareElements()) {
          if (nodeLocalName.equals(en.getName())
              && en.getNs().equals(attrPrfxNcp.getUri())) {
            return true;
          }
        }
      }
    }
    if (parameters.getQnameAwareXPathElements().size() > 0
        && node.getChildNodes().getLength() == 1) {
      NamespaceContextParams ncp = getLastElement(nPrefix);
      if (childText == null) {
    	  childText = node.getTextContent();
      }
      for (QNameAwareParameter en : parameters.getQnameAwareXPathElements()) {
        if (nodeLocalName.equals(en.getName())
            && ncp.getUri().equals(en.getNs())) {
          NSContext nsContext = xpathesNsMap.get(childText);
          try {
            if (nsContext == null) {
              nsContext = new NSContext();
              XPathParser xpathParser = new XPathParser(null, null);
              org.apache.xpath.compiler.Compiler xpathCompiler = new org.apache.xpath.compiler.Compiler();
              xpathParser.initXPath(xpathCompiler, childText, nsContext);
              xpathesNsMap.put(childText, nsContext);
              nsContext.setWords(xpathCompiler.getTokenQueue());
            }
            if (nsContext.getXpathNs().contains(prefix))
              return true;
          } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
          }
        }
      }
    }

    NamespaceContextParams ncp = getLastElement(prefix);
    String prfx = ncp.getPrefix();
    for (int ai = 0; ai < node.getAttributes().getLength(); ai++) {
      Node attr = node.getAttributes().item(ai);
      String attrPrfx = getNodePrefix(attr);
      if (!attrPrfx.isEmpty() && attrPrfx.equals(prefix)) {
        return true;
      }
      if (parameters.getQnameAwareAttributes().size() > 0) {
        String attrValue = attr.getNodeValue();
        if (attrValue.startsWith(prfx + C)) {
          String attrLocalName = getLocalName(attr);
          String attrPrefix = getNodePrefix(attr);
          NamespaceContextParams attrPrfxNcp = getLastElement(attrPrefix);
          for (QNameAwareParameter en : parameters.getQnameAwareAttributes()) {
            if (attrLocalName.equals(en.getName())
                && en.getNs().equals(attrPrfxNcp.getUri())) {
              return true;
            }
          }
        }
      }
    }

    return false;
  }*/

  /**
   * Replace special characters.
   * 
   * @param text
   *          input text
   * @param bAttr
   *          true if text is attribute value
   * 
   * @return replacement text
   */
  private String processText(String text, boolean bAttr) {

    text = StringUtils.replace(text,"&", "&amp;");
    text = StringUtils.replace(text, "<", "&lt;");
    if (!bAttr) {
      text = StringUtils.replace(text, ">", "&gt;");
    } else {
      text = StringUtils.replace(text, "\"", "&quot;");
      text = StringUtils.replace(text, "#xA", "&#xA;");
      text = StringUtils.replace(text, "#x9", "&#x9;");
    }
    text = StringUtils.replace(text, "#xD", "&#xD;");
    return text;
  }

  /**
   * Returns the node local name.
   * 
   * @param node
   *          DOM node
   * @return Returns local name
   */
  private String getLocalName(Node node) {
    if (node.getLocalName() != null)
      return node.getLocalName();
    String name = node.getNodeName();
    int idx = name.indexOf(C);
    if (idx > -1)
      return name.substring(idx + 1);
    if (XMLNS.equals(name)) {
      return ""; // to simplify code
    }
    return name;
  }

  /**
   * Returns parameter by key.
   * 
   * @return parameter
   */
/*  private NamespaceContextParams getLastElement(String key) {
    return getLastElement(key, -1);
  }
*/

  private NamespaceContextParams getLastListElement(List<NamespaceContextParams> nList) {
    if (nList!=null && nList.size()>0) {
      return nList.get(nList.size()-1);
    }
    return null;
  }

  private PrefixContextParams getLastCurrentListElement(List<PrefixContextParams> nList) {
    if (nList!=null && nList.size()>0) {
      return nList.get(nList.size()-1);
    }
    return null;
  }

  /**
   * Returns parameter by key.
   * 
   * @param uri uri
   * @param shift
   *          shift
   * @return parameter
   */
  /*private NamespaceContextParams getLastElement(String uri, int shift) {
    List<NamespaceContextParams> lst = namespaces.get(uri);
    if (lst!=null)
      return lst.size() + shift > -1 ? lst.get(lst.size() + shift) : null;
    return null;
  }
*/
  /**
   * Returns the node prefix.
   * 
   * @param node
   *          DOM node
   * @return Returns prefix
   */
  private String getNodePrefix(Node node) {
    String prfx = node.getPrefix();
    if (prfx == null || prfx.isEmpty()) {
      prfx = "";
      String name = node.getNodeName();
      if (XMLNS.equals(name)) {
        return name; // to simplify code
      }
      int idx = name.indexOf(C);
      if (idx > -1)
        return StringUtils.substring(name,0, idx);
    }
    return prfx;
  }



  /**
   * primer:  canonicalization of element ds:SignedInfo required namespace difinition xmlns:ds="http://www.w3.org/2000/09/xmldsig#"
   * in the parent node ds:Signature.
   * Load all namespace definitions for canonicalized element before canonicalization process start.
   */

  protected void loadParentNamespaces(Node node) {
    Node current = node;
    // processing up to root

    int depth = 0;
    while((current=current.getParentNode())!=null && (current.getNodeType()!=Node.DOCUMENT_NODE)) {
      depth --;
      for (int ni = 0; ni < current.getAttributes().getLength(); ni++) {
        Node attr = current.getAttributes().item(ni);
        String suffix = getLocalName(attr);
        String prfxNs = getNodePrefix(attr);

        if (XMLNS.equals(prfxNs)) {
          String uri = attr.getNodeValue();
          this.declaredPrefixes.definePrefix(suffix,uri,depth);
        }
      }
    }

  }


}