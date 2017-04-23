package ru.relex.c14n2;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import ru.relex.c14n2.util.*;

import java.util.*;

import static ru.relex.c14n2.util.XPathParserStates.*;

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
    private static final String CF = "&#x%s;";
    private static final String C = ":";
    private static final int ID_ARRAY_CAPACITY = 20;

    private static final int PREFIX_ARRAY_CAPACITY = 10;

    private List<Node> excludeList;
    private Parameters parameters;
    private StringBuilder outputBuffer;

    private int nextId;
    private HashMap<String, String> redefinedPrefixesMap;

    private static final boolean PVDNP_MODE = true;
    private int nodeDepth = 0;

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


    private Set<String> qNameAwareElements;

    private Set<String> qNameAwareQualifiedAttrs;

    private Set<String> qNameAwareXPathElements;

    private Set<String> qNameAwareUnqualifiedAttrs;


    private boolean bSequential = false;

    private char[] tempXpathStorage;
    private char[] tempPrefixStorage;

//  private Map<String, NSContext> xpathesNsMap = new HashMap<String, NSContext>();

    /**
     * Constructor.
     *
     * @param parameters   canonicalization parameters
     * @param excludeList  inclusion list
     * @param outputBuffer output
     */
    protected DOMCanonicalizerHandler(Node node, Parameters parameters,
                                      List<Node> excludeList, StringBuilder outputBuffer) {
        this.parameters = parameters;
        this.outputBuffer = outputBuffer;
        this.excludeList = excludeList;
        this.declaredPrefixes = new PrefixesContainer();



        this.usedPrefixes = new PrefixesContainer();
        this.qNameAwareElements = new HashSet<String>();
        this.qNameAwareQualifiedAttrs = new HashSet<String>();
        this.qNameAwareXPathElements = new HashSet<String>();
        this.qNameAwareUnqualifiedAttrs = new HashSet<String>();
        this.redefinedPrefixesMap = new HashMap<String, String>();
        bSequential = parameters.getPrefixRewrite().equals(Parameters.SEQUENTIAL);


        loadParentNamespaces(node);

        if (declaredPrefixes.getByFirstKey("") == null) {
            // The default namespace is declared by xmlns="...". To make the algorithm simpler this will be treated as a
            // namespace declaration whose prefix value is "" i.e. an empty string.
            declaredPrefixes.definePrefix("", "", 0);
        }

        initQNameAwareElements();
        initQNameAwareQualifiedAttrs();
        initQNameAwareXPathElements();
        initQNameAwareUnqualifiedAttrs();
    }

    private void initQNameAwareUnqualifiedAttrs() {

        for (QNameAwareParameter en : parameters.getQnameAwareUnqualifiedAttributes()) {
            String qNameAwareElement = createQName(en.getNs(), en.getParentName(), en.getName());
            qNameAwareUnqualifiedAttrs.add(qNameAwareElement);
        }

    }

    private void initQNameAwareQualifiedAttrs() {
        for (QNameAwareParameter en : parameters.getQnameAwareQualifiedAttributes()) {
            String qNameAwareElement = createQName(en.getNs(), en.getName());
            qNameAwareQualifiedAttrs.add(qNameAwareElement);
        }
    }

    private void initQNameAwareXPathElements() {
        for (QNameAwareParameter en : parameters.getQnameAwareXPathElements()) {
            String qNameAwareElement = createQName(en.getNs(), en.getName());
            qNameAwareXPathElements.add(qNameAwareElement);
        }
    }

    private String createQName(String uri, String localName) {
        StringBuffer sb = new StringBuffer("{");
        sb.append(uri);
        sb.append("}");
        sb.append(localName);
        return sb.toString();
    }

    private String createQName(String uri, String localName, String attrName) {
        StringBuilder sb = new StringBuilder(createQName(uri, localName));
        sb.append("/");
        sb.append(attrName);
        return sb.toString();
    }


    private void initQNameAwareElements() {
        for (QNameAwareParameter en : parameters.getQnameAwareElements()) {
            String qNameAwareElement = createQName(en.getNs(), en.getName());
            qNameAwareElements.add(qNameAwareElement);
        }

    }

    /**
     * Prosessing of element node.
     *
     * @param node element node
     */
    protected void processElement(Node node) {
        LOGGER.debug("processElement: {}", node);
        if (isInExcludeList(node))
            return;
        nodeDepth++;
        addNamespaces(node);

        Set<NSDeclaration> nsDeclarations = new HashSet<NSDeclaration>();

        evaluateUriVisibility(node, nsDeclarations);


        List<NSDeclaration> nsDeclarationList = new LinkedList<NSDeclaration>();
        nsDeclarationList.addAll(nsDeclarations);

        if (bSequential) {
            // Sort this list of namespace URIs by lexicographic(ascending) order.
            Collections.sort(nsDeclarationList, new Comparator<NSDeclaration>() {
                @Override
                public int compare(NSDeclaration t0, NSDeclaration t1) {
                    return t0.getUri().compareTo(t1.getUri());
                }
            });

            for (NSDeclaration nsDeclaration : nsDeclarationList) {
                int nextId;
                String newPrefix;
                String uri = nsDeclaration.getUri();
                if (redefinedPrefixesMap.containsKey(uri)) {
                    newPrefix = redefinedPrefixesMap.get(uri);
                    nsDeclaration.setPrefix(newPrefix);
                } else {
                    nextId = this.nextId;
                    this.nextId = nextId + 1;
                    newPrefix = "n" + nextId;
                    nsDeclaration.setPrefix(newPrefix);
                    redefinedPrefixesMap.put(uri, newPrefix);
                }


                usedPrefixes.definePrefix(nsDeclaration.getUri(), newPrefix, nodeDepth);
            }
        }

        // write to outputBuffer

        // startElement

        String nodeLocalName = getLocalName(node);
        String nodePrefix = getNodePrefix(node);

        String nodeUri = getNamespaceURIByPrefix(nodePrefix);

        String newPrefix = getNewPrefix(nodeUri, nodePrefix);

        if (newPrefix == null || newPrefix.isEmpty()) {
            outputBuffer.append(String.format("<%s", getLocalName(node)));
        } else {
            outputBuffer.append(String.format("<%s:%s", newPrefix, getLocalName(node)));
        }

        //  Sort this list of namespace declaration in lexicographic(ascending) order of prefixes.
        //  see Collections.sort above
        if (!PVDNP_MODE || !bSequential) {
            Collections.sort(nsDeclarationList, new Comparator<NSDeclaration>() {
                @Override
                public int compare(NSDeclaration t0, NSDeclaration t1) {
                    return t0.getPrefix().compareTo(t1.getPrefix());
                }
            });
        }


        for (NSDeclaration nsDeclaration : nsDeclarationList) {
            String nsName = nsDeclaration.getPrefix();
            String nsUri = nsDeclaration.getUri();
            if (!nsName.equals(EMPTY_URI)) {
                outputBuffer.append(String.format(" %s:%s=\"%s\"", XMLNS, nsName, nsUri));
            } else {
                outputBuffer.append(String.format(" %s=\"%s\"", XMLNS, nsUri));
            }
        }

        List<Attribute> outAttrsList = processAttributes(node, nodeUri);

        for (Attribute attribute : outAttrsList) {

            String attrPrfx = attribute.getAttrPrfx();
            String attrName = attribute.getLocalName();
            String attrValue = attribute.getValue();
            if (attribute.isAttributeQualified()) {
                String attrQName = createQName(attribute.getUri(), attribute.getLocalName());
                if (this.qNameAwareQualifiedAttrs.contains(attrQName)) {
                    attrValue = processQNameText(attrValue);
                }
            } else {
                String attrQName = createQName(nodeUri, nodeLocalName, attribute.getLocalName());
                if (this.qNameAwareUnqualifiedAttrs.contains(attrQName)) {
                    attrValue = processQNameText(attrValue);
                }
            }


            // According to the xml-c14n:
            // "Note: unlike elements, if an attribute doesn't have a prefix, that means it is a locally scoped attribute."
            // but we used attributeFormDefault="qualified"
      /*if (attrPrfx==null && attribute.getLocalName().startsWith(XML)) {
        // The "xml" and "xmlns" prefixes are reserved and have special behavior
        outputBuffer.append(String.format(" %s=\"%s\"", attrName, attrValue));
      } else { */
            // xack for xml:AAA attributes
            if (XML.equals(attribute.getOldPrefix())) {
                outputBuffer.append(String.format(" %s:%s=\"%s\"", attribute.getOldPrefix(), attrName, attrValue));
                continue;
            }

            if (attrPrfx.isEmpty()) {
                outputBuffer.append(String.format(" %s=\"%s\"", attrName, attrValue));
            } else {
                outputBuffer.append(String.format(" %s:%s=\"%s\"", attrPrfx, attrName, attrValue));
            }
            //}
        }

        outputBuffer.append(">");
    }

    private String processQNameText(String text) {
        String textPrefix = getTextPrefix(text);
        String textUri = getNamespaceURIByPrefix(textPrefix);
        String newTextPrefix = getNewPrefix(textUri, textPrefix);

        StringBuffer sb = new StringBuffer(newTextPrefix);
        sb.append(C);
        sb.append(StringUtils.substring(text, textPrefix.length() + 1));
        return sb.toString();
    }

    private String getNewPrefix(String nodeUri, String nodePrefix) {
        if (bSequential) {
            return usedPrefixes.getByFirstKey(nodeUri);
        } else {
            return nodePrefix;
        }
    }


    private String getNamespaceURIByPrefix(String prefix) {
        String uri = declaredPrefixes.getByFirstKey(prefix);
        if (uri == null) {
            LOGGER.error("BUG!!");
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
                if (!XML.equals(prfxNs)) {
                    attribute.setUri(getNamespaceURIByPrefix(prfxNs));

                } else {
                    // xml:... Canonical XML 2.0 should ignore this declaration.
                    attribute.setLocalName(suffix);
                }
            }
            attribute.setValue(getAttributeValue(attr.getNodeValue()));


            //  If it is a qualified attribute and the PrefixRewrite parameter is sequential, modify the QName
            // of the attribute name to use the new prefix
            attribute.setAttrPrfx(attribute.isAttributeQualified() ? getNewPrefix(attribute.getUri(), attribute.getOldPrefix()) : "");

            attributeList.add(attribute);
        }

        //  Sort this list of namespace declaration in lexicographic(ascending) order of prefixes.
        Comparator<Attribute> comparator = new Comparator<Attribute>() {
            @Override
            public int compare(Attribute t0, Attribute t1) {
                String t0Uri = t0.isAttributeQualified() ? t0.getUri() : " ";
                String t1Uri = t1.isAttributeQualified() ? t1.getUri() : " ";
                String q0 = createQName(t0Uri, t0.getLocalName());
                String q1 = createQName(t1Uri, t1.getLocalName());
                return q0.compareTo(q1);
            }
        };

        Collections.sort(attributeList, comparator);
        return attributeList;
    }

    private String getAttributeValue(String input) {
        String attrValue = input != null ? input : "";

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

        return value.toString();
    }

    /**
     * Completion of processing element node.
     *
     * @param node element node
     */
    protected void processEndElement(Node node) {
        if (isInExcludeList(node))
            return;

        String nodePrefix = getNodePrefix(node);
        String nodeUri = getNamespaceURIByPrefix(nodePrefix);


        String elementPrefix = getNewPrefix(nodeUri, nodePrefix);

        if (elementPrefix == null || elementPrefix.isEmpty()) {
            outputBuffer.append(String.format("</%s>", getLocalName(node)));
        } else {
            outputBuffer.append(String.format("</%s:%s>", elementPrefix, getLocalName(node)));
        }

        removeNamespaces(node);
        nodeDepth--;
    }

    /**
     * Prosessing of text node.
     *
     * @param node text node
     */

    protected void processText(Node node) {
        LOGGER.debug("processText: {}", node);
        String text = node.getNodeValue() != null ? node.getNodeValue() : "";
        text = processText(text, false);

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

        Node element = node.getNodeType() == Node.TEXT_NODE ? node.getParentNode() : node;
        String nodePrefix = getNodePrefix(element);
        String nodeLocalName = getLocalName(element);
        String nodeUri = getNamespaceURIByPrefix(nodePrefix);

        String nodeQName = createQName(nodeUri, nodeLocalName);
        if (this.qNameAwareElements.contains(nodeQName)) {
            text = processQNameText(text);
        }
        if (this.qNameAwareXPathElements.contains(nodeQName)) {
            text = processXPathText(text);
        }
        outputBuffer.append(text);
    }

    private int writeNewXPathCharacter(char ch, int pos) {
        pos--;
        if (pos < 0) {
            char[] newResultArr = new char[tempXpathStorage.length + PREFIX_ARRAY_CAPACITY * 2];
            System.arraycopy(tempXpathStorage, 0, newResultArr, PREFIX_ARRAY_CAPACITY * 2, tempXpathStorage.length);
            tempXpathStorage = newResultArr;
            pos += PREFIX_ARRAY_CAPACITY * 2;
        }
        tempXpathStorage[pos] = ch;
        return pos;
    }

    private int writeXPathPrefix(char ch, int pos) {
        pos--;
        if (pos < 0) {
            char[] newResultArr = new char[tempPrefixStorage.length + PREFIX_ARRAY_CAPACITY];
            System.arraycopy(tempPrefixStorage, 0, newResultArr, PREFIX_ARRAY_CAPACITY, tempPrefixStorage.length);
            tempPrefixStorage = newResultArr;
            pos += PREFIX_ARRAY_CAPACITY;
        }
        tempPrefixStorage[pos] = ch;
        return pos;
    }


    private String processXPathText(String text) {

        tempXpathStorage = new char[text.length()];

        int resultPos = tempXpathStorage.length;

        tempPrefixStorage = new char[PREFIX_ARRAY_CAPACITY];

        int prefixPos = tempPrefixStorage.length;

        XPathParserStates state = COMMON;

        for (int i = text.length() - 1; i >= 0; i--) {
            char ch = text.charAt(i);
            switch (state) {
                case COMMON:
                    switch (ch) {
                        case '\'':
                            state = SINGLE_QUOTED_STRING;
                            break;
                        case '"':
                            state = DOUBLE_QUOTED_STRING;
                            break;
                        case ':':
                            state = COLON;
                            break;
                    }
                    resultPos = writeNewXPathCharacter(ch, resultPos);
                    break;

                case SINGLE_QUOTED_STRING:
                    switch (ch) {
                        case '\'':
                            state = COMMON;
                            break;
                    }
                    resultPos = writeNewXPathCharacter(ch, resultPos);
                    break;

                case DOUBLE_QUOTED_STRING:
                    switch (ch) {
                        case '"':
                            state = COMMON;
                            break;
                    }
                    resultPos = writeNewXPathCharacter(ch, resultPos);
                    break;

                case COLON:
                    if (ch == ':') { // double colon - axis
                        state = COMMON;
                        resultPos = writeNewXPathCharacter(ch, resultPos);
                        continue;
                    }
                    if (isNCSymbol(ch)) {
                        state = PREFIX;
                        prefixPos = writeXPathPrefix(ch, prefixPos);
                    }
                    break;
                case PREFIX:
                    if (isNCSymbol(ch)) {
                        prefixPos = writeXPathPrefix(ch, prefixPos);
                    } else {
                        String prefix = String.valueOf(tempPrefixStorage, prefixPos, tempPrefixStorage.length - prefixPos);
                        prefixPos = tempPrefixStorage.length;
                        String uri = getNamespaceURIByPrefix(prefix);
                        String newPrefix = getNewPrefix(uri, prefix);
                        for (int j = newPrefix.length() - 1; j >= 0; j--) {
                            char newPrefixCh = newPrefix.charAt(j);
                            resultPos = writeNewXPathCharacter(newPrefixCh, resultPos);
                        }
                        switch (ch) {
                            case '\'':
                                state = SINGLE_QUOTED_STRING;
                                break;
                            case '"':
                                state = DOUBLE_QUOTED_STRING;
                                break;
                            case ':':
                                state = COLON;
                                break;
                            default:
                                state = COMMON;
                        }
                        resultPos = writeNewXPathCharacter(ch, resultPos);
                    }
                    break;

            }
        }

        String result = String.valueOf(tempXpathStorage, resultPos, tempXpathStorage.length - resultPos);
        return result;
    }

    /**
     * Prosessing of process instruction node.
     *
     * @param node process instruction node
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
     * @param node comment node
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
     * @param node CDATA node
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
     * @param node DOM node
     * @return Returns true if a node there is in exclusion list, false -
     * otherwise
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
     * Remove unused namespaces from the stack.
     *
     * @param node DOM node
     */
    private void removeNamespaces(Node node) {

        usedPrefixes.deleteLevel(nodeDepth);
        declaredPrefixes.deleteLevel(nodeDepth);
    }

    /**
     * Prosessing of node attributes.
     *
     * @param node DOM node
     * @return Returns a list of output attributes
     */
    private void evaluateUriVisibility(final Node node, Set<NSDeclaration> nsDeclarations) {

        String nodePrf = getNodePrefix(node);
        String nodeLocalName = getLocalName(node);
        String nodeUri = getNamespaceURIByPrefix(nodePrf);

        addNSDeclarationForPrefix(nodePrf, nsDeclarations);

        for (int ai = 0; ai < node.getAttributes().getLength(); ai++) {
            Node attr = node.getAttributes().item(ai);
            if (isInExcludeList(attr)) continue;
            String prfx = getNodePrefix(attr);
            if (!XMLNS.equals(prfx)) {

                if (XML.equals(prfx)) {
                    /**
                     Canonical XML 2.0 ignores these attributes as well.
                     */
                    continue;
                }

                String attrNamespaceURI;
                String text = getAttributeValue(attr.getNodeValue());
                //Note: unlike elements, if an attribute doesn't have a prefix, that means it is a locally scoped attribute.
                if (EMPTY_PREFIX.equals(prfx)) {
                    // unqualifierAttr
                    attrNamespaceURI = nodeUri;
                    String qName = createQName(attrNamespaceURI, nodeLocalName, getLocalName(attr));
                    addVisibilityIfNessesaryByText(qName, text, nsDeclarations, qNameAwareUnqualifiedAttrs);

                } else {
                    attrNamespaceURI = getNamespaceURIByPrefix(prfx);
                    // qualifierAttr, check by QualifiedAttr
                    String qName = createQName(attrNamespaceURI, getLocalName(attr));
                    addVisibilityIfNessesaryByText(qName, text, nsDeclarations, qNameAwareQualifiedAttrs);
                }

                addNSDeclarationForPrefix(prfx, nsDeclarations);
            }
        }

        String text = node.getTextContent();
        String qName = createQName(nodeUri, nodeLocalName);
        addVisibilityIfNessesaryByText(qName, text, nsDeclarations, qNameAwareElements);
        addXPathVisibilityIfNessesaryByText(qName, text, nsDeclarations);

    }

    private boolean isNCSymbol(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '_' || ch == '-' || ch == '.';
    }

    private void addXPathVisibilityIfNessesaryByText(String qName, String text, Set<NSDeclaration> nsDeclarations) {
        if (qNameAwareXPathElements.contains(qName)) {
            // Search for single colons : in the XPath expression, but do not consider single colons inside quoted strings.
            // Double colons are used for axes, e.g. in self::node() , "self:" is not a prefix, but an axis name.
            // The prefix will be present just before the single colon. Go backwards from the colon, skip whitespace, and
            // extract the prefix, by collecting characters till the first non NCName match. e.g. in /soap : Body, extract the "soap".
            // The NCName production is defined in [XML-NAMES].

            Set<String> xPathPrefixes = new HashSet<String>();

            XPathParserStates state = COMMON;

            char[] prefixArr = new char[PREFIX_ARRAY_CAPACITY];
            int pos = prefixArr.length;

            for (int i = text.length() - 1; i >= 0; i--) {
                char ch = text.charAt(i);
                switch (state) {
                    case COMMON:
                        if (ch == '\'') {
                            state = SINGLE_QUOTED_STRING;
                            continue;
                        }
                        if (ch == '"') {
                            state = DOUBLE_QUOTED_STRING;
                            continue;
                        }
                        if (ch == ':') {
                            state = COLON;
                            continue;
                        }
                        break;
                    case SINGLE_QUOTED_STRING:
                        if (ch == '\'') {
                            state = COMMON;
                        }
                        break;
                    case DOUBLE_QUOTED_STRING:
                        if (ch == '"') {
                            state = COMMON;
                        }
                        break;
                    case COLON:
                        if (ch == ':') { // double colon - axis
                            state = COMMON;
                            continue;
                        }
                        if (isNCSymbol(ch)) {
                            state = PREFIX;
                            pos--;
                            if (pos < 0) {
                                char[] newPrefixArr = new char[prefixArr.length + PREFIX_ARRAY_CAPACITY];
                                System.arraycopy(prefixArr, 0, newPrefixArr, PREFIX_ARRAY_CAPACITY, prefixArr.length);
                                prefixArr = newPrefixArr;
                                pos += PREFIX_ARRAY_CAPACITY;
                            }
                            prefixArr[pos] = ch;
                        }
                        break;
                    case PREFIX:
                        if (isNCSymbol(ch)) {
                            pos--;
                            if (pos < 0) {
                                char[] newPrefixArr = new char[prefixArr.length + PREFIX_ARRAY_CAPACITY];
                                System.arraycopy(prefixArr, 0, newPrefixArr, PREFIX_ARRAY_CAPACITY, prefixArr.length);
                                prefixArr = newPrefixArr;
                                pos += PREFIX_ARRAY_CAPACITY;
                            }
                            prefixArr[pos] = ch;
                        } else {
                            String prefix = String.valueOf(prefixArr, pos, prefixArr.length - pos);
                            pos = prefixArr.length;
                            xPathPrefixes.add(prefix);
                            if (ch == '\'') {
                                state = SINGLE_QUOTED_STRING;
                                continue;
                            }
                            if (ch == '"') {
                                state = DOUBLE_QUOTED_STRING;
                                continue;
                            }
                            if (ch == ':') {
                                state = COLON;
                                continue;
                            }
                            state = COMMON;
                        }
                        break;

                }
            }

            for (String prefix : xPathPrefixes) {
                addNSDeclarationForPrefix(prefix, nsDeclarations);
            }

        }
    }

    private void addNSDeclarationForPrefix(String prefix, Set<NSDeclaration> nsDeclarations) {

        String prefixUri = getNamespaceURIByPrefix(prefix);
        if (bSequential) {
            //       firstKey - url
            //       secondKey - prefix
            if (usedPrefixes.getByFirstKey(prefixUri) == null) {
                NSDeclaration nsDeclaration = new NSDeclaration();
                nsDeclaration.setUri(prefixUri);
                nsDeclarations.add(nsDeclaration);
            }
        } else {
            //       firstKey - prefix
            //       secondKey - url
            String existsUri = usedPrefixes.getByFirstKey(prefix);

            // hack xmlns=""
            if (existsUri == null && EMPTY_PREFIX.equals(prefix) && EMPTY_URI.equals(prefixUri)) {
                usedPrefixes.definePrefix(prefix, prefixUri, nodeDepth);
                return;
            }

            if (existsUri == null || !existsUri.equals(prefixUri)) {
                usedPrefixes.definePrefix(prefix, prefixUri, nodeDepth);
                NSDeclaration nsDeclaration = new NSDeclaration();
                nsDeclaration.setUri(prefixUri);
                nsDeclaration.setPrefix(prefix);
                nsDeclarations.add(nsDeclaration);
            }

        }
    }


    private void addVisibilityIfNessesaryByText(String checkStr, String text, Set<NSDeclaration> nsDeclarations, Set<String> checkSet) {
        if (checkSet.contains(checkStr)) {
            String prefix = getTextPrefix(text);
            if (XML.equals(prefix)) { // Canonical XML 2.0 should ignore xml declaration.
                return;
            }
            addNSDeclarationForPrefix(prefix, nsDeclarations);
        }

    }

    private String getTextPrefix(String text) {
        int idx = text.indexOf(C);
        String prefix = "";
        if (idx > -1) {
            prefix = StringUtils.substring(text, 0, idx);
        }
        return prefix;
    }


    /**
     * Add namespaces to stack.
     *
     * @param node DOM node
     */
    private void addNamespaces(Node node) {

        for (int ni = 0; ni < node.getAttributes().getLength(); ni++) {
            Node attr = node.getAttributes().item(ni);
            if (isInExcludeList(attr))
                continue;
            String suffix = getLocalName(attr);
            String prfxNs = getNodePrefix(attr);

            if (XMLNS.equals(prfxNs)) {
                String uri = attr.getNodeValue();
                declaredPrefixes.definePrefix(suffix, uri, nodeDepth);
            }
        }

        // happens: <Signature  xmlns="http://www.w3.org/2000/09/xmldsig#">
        String prfxEl = getNodePrefix(node);
        String uri = node.getNamespaceURI();
        if (prfxEl.equals("") && !uri.equals("")){
            declaredPrefixes.definePrefix(prfxEl,uri,nodeDepth);
        }

    }


    /**
     * Replace special characters.
     *
     * @param text  input text
     * @param bAttr true if text is attribute value
     * @return replacement text
     */
    private String processText(String text, boolean bAttr) {

        text = StringUtils.replace(text, "&", "&amp;");
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
     * @param node DOM node
     * @return Returns local name
     */
    private String getLocalName(Node node) {
        String name = node.getLocalName()!=null?node.getLocalName():node.getNodeName();
        if (XMLNS.equals(name)) {
            return ""; // to simplify code
        }
        int idx = name.indexOf(C);
        if (idx > -1)
            return name.substring(idx + 1);
        return name;
    }

    /**
     * Returns the node prefix.
     *
     * @param node DOM node
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
                return StringUtils.substring(name, 0, idx);
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


        List<Node> parentNodeList = new LinkedList<Node>();
        while ((current = current.getParentNode()) != null && (current.getNodeType() != Node.DOCUMENT_NODE)) {
            // revert list
            parentNodeList.add(current);
        }

        int depth = 0;
        for (int i = parentNodeList.size()-1;i>=0;i--) {
            depth++;
            Node pnode = parentNodeList.get(i);
            for (int ni = 0; ni < pnode.getAttributes().getLength(); ni++) {
                Node attr = pnode.getAttributes().item(ni);
                String suffix = getLocalName(attr);
                String prfxNs = getNodePrefix(attr);

                if (XMLNS.equals(prfxNs)) {
                    String uri = attr.getNodeValue();
                    this.declaredPrefixes.definePrefix(suffix, uri, -depth);
                }

            }

        }
        depth++;
        // HACK <Body xmlns="http://schemas.xmlsoap.org/soap/envelope/" ..> but node.name=SOAP-ENV:Body ???
        this.declaredPrefixes.definePrefix("SOAP-ENV", "http://schemas.xmlsoap.org/soap/envelope/", -depth);

    }



}