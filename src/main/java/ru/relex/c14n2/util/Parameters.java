package ru.relex.c14n2.util;

import java.util.ArrayList;
import java.util.List;

/**
 * The representation of the canonicalization algorithm parameters.
 */
public class Parameters {
  public static String SEQUENTIAL = "sequential";
  public static String NONE = "none";

  private boolean ignoreComments = true;
  private boolean trimTextNodes = false;
  private String prefixRewrite = NONE;
  private List<QNameAwareParameter> qnameAwareQualifiedAttributes = new ArrayList<QNameAwareParameter>();
  private List<QNameAwareParameter> qnameAwareUnqualifiedAttributes = new ArrayList<QNameAwareParameter>();
  private List<QNameAwareParameter> qnameAwareElements = new ArrayList<QNameAwareParameter>();
  private List<QNameAwareParameter> qnameAwareXPathElements = new ArrayList<QNameAwareParameter>();


  /**
   * Returns whether to ignore comments during canonicalization.
   * 
   * @return Returns true if comments are ignored, false otherwise.
   */
  public boolean isIgnoreComments() {
    return ignoreComments;
  }

  public void setIgnoreComments(boolean ignoreComments) {
    this.ignoreComments = ignoreComments;
  }

  /**
   * Returns whether to trim (i.e. remove leading and trailing whitespaces) all
   * text nodes when canonicalizing.
   * 
   * @return Returns true if whitespaces are removed, false otherwise.
   */
  public boolean isTrimTextNodes() {
    return trimTextNodes;
  }

  public void setTrimTextNodes(boolean trimTextNodes) {
    this.trimTextNodes = trimTextNodes;
  }

  /**
   * Defines the mode of replacement prefixes.
   * 
   * @return With none, prefixes are left unchanged, with sequential, prefixes
   *         are changed to "n0", "n1", "n2" ... except the special prefixes
   *         "xml" and "xmlns" which are left unchanged.
   */
  public String getPrefixRewrite() {
    return prefixRewrite;
  }

  public void setPrefixRewrite(String prefixRewrite) {
    this.prefixRewrite = prefixRewrite;
  }

  public List<QNameAwareParameter> getQnameAwareQualifiedAttributes() {
    return qnameAwareQualifiedAttributes;
  }

  public void setQnameAwareQualifiedAttributes(List<QNameAwareParameter> qnameAwareQualifiedAttributes) {
    this.qnameAwareQualifiedAttributes = qnameAwareQualifiedAttributes;
  }

  public List<QNameAwareParameter> getQnameAwareUnqualifiedAttributes() {
    return qnameAwareUnqualifiedAttributes;
  }

  public void setQnameAwareUnqualifiedAttributes(List<QNameAwareParameter> qnameAwareUnqualifiedAttributes) {
    this.qnameAwareUnqualifiedAttributes = qnameAwareUnqualifiedAttributes;
  }

  public List<QNameAwareParameter> getQnameAwareElements() {
    return qnameAwareElements;
  }

  public void setQnameAwareElements(List<QNameAwareParameter> qnameAwareElements) {
    this.qnameAwareElements = qnameAwareElements;
  }

  public List<QNameAwareParameter> getQnameAwareXPathElements() {
    return qnameAwareXPathElements;
  }

  public void setQnameAwareXPathElements(List<QNameAwareParameter> qnameAwareXPathElements) {
    this.qnameAwareXPathElements = qnameAwareXPathElements;
  }
}