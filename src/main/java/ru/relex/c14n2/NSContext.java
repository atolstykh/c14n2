package ru.relex.c14n2;

import java.util.ArrayList;
import java.util.List;

import org.apache.xml.utils.ObjectVector;
import org.apache.xml.utils.PrefixResolver;
import org.w3c.dom.Node;

class NSContext implements PrefixResolver {

  private List<String> xpathNs;
  private ObjectVector words;

  public NSContext() {
    xpathNs = new ArrayList<String>();
  }

  public List<String> getXpathNs() {
    return xpathNs;
  }

  public ObjectVector getWords() {
    return words;
  }

  public void setWords(ObjectVector words) {
    this.words = words;
  }

  @Override
  public String getBaseIdentifier() {
    return null;
  }

  @Override
  public String getNamespaceForPrefix(String prefix, Node node) {
    xpathNs.add(prefix);
    return prefix;
  }

  @Override
  public String getNamespaceForPrefix(String prefix) {
    xpathNs.add(prefix);
    return prefix;
  }

  @Override
  public boolean handlesNullPrefixes() {
    return false;
  }
}