package ru.relex.c14n2;

import java.util.ArrayList;
import java.util.List;

import org.apache.xml.utils.ObjectVector;
import org.apache.xml.utils.PrefixResolver;
import org.w3c.dom.Node;

/**
 * The internal representation of the XPath declaration.
 */
class NSContext implements PrefixResolver {

  private List<String> xpathNs;
  private ObjectVector words;

  /**
   * Constructor.
   */
  public NSContext() {
    xpathNs = new ArrayList<String>();
  }

  /**
   * Returns a list of namespace prefixes from the XPath declaration.
   * 
   * @return Returns a list
   */
  public List<String> getXpathNs() {
    return xpathNs;
  }

  /**
   * Returns the lexical elements of the XPath declaration.
   * 
   * @return Returns the lexical elements
   */
  public ObjectVector getWords() {
    return words;
  }

  public void setWords(ObjectVector words) {
    this.words = words;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getBaseIdentifier() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getNamespaceForPrefix(String prefix, Node node) {
    xpathNs.add(prefix);
    return prefix;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getNamespaceForPrefix(String prefix) {
    xpathNs.add(prefix);
    return prefix;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean handlesNullPrefixes() {
    return false;
  }
}