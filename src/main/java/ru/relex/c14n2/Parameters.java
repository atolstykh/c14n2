package ru.relex.c14n2;

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
  private List<QNameAwareParameter> QnameAwareAttributes = new ArrayList<QNameAwareParameter>();
  private List<QNameAwareParameter> QnameAwareElements = new ArrayList<QNameAwareParameter>();
  private List<QNameAwareParameter> QnameAwareXPathElements = new ArrayList<QNameAwareParameter>();

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

  /**
   * Returns a list of parameters which defines the qualified attribute names
   * and unqualified attribute names whose entire content must be processed as
   * QName-valued for the purposes of canonicalization.
   * 
   * @return Returns a list
   */
  public List<QNameAwareParameter> getQnameAwareAttributes() {
    return QnameAwareAttributes;
  }

  public void setQnameAwareAttributes(
      List<QNameAwareParameter> qnameAwareAttributes) {
    QnameAwareAttributes = qnameAwareAttributes;
  }

  /**
   * Returns a list of parameters which defines the qualified element names
   * whose entire content must be processed as QName-valued for the purposes of
   * canonicalization.
   * 
   * @return Returns a list
   */
  public List<QNameAwareParameter> getQnameAwareElements() {
    return QnameAwareElements;
  }

  public void setQnameAwareElements(List<QNameAwareParameter> qnameAwareElements) {
    QnameAwareElements = qnameAwareElements;
  }

  /**
   * Returns a list of parameters which defines the element names that contain
   * XPath 1.0 expressions whose entire content must be processed as
   * QName-valued for the purposes of canonicalization.
   * 
   * @return Returns a list
   */
  public List<QNameAwareParameter> getQnameAwareXPathElements() {
    return QnameAwareXPathElements;
  }

  public void setQnameAwareXPathElements(
      List<QNameAwareParameter> qnameAwareXPathElements) {
    QnameAwareXPathElements = qnameAwareXPathElements;
  }
}