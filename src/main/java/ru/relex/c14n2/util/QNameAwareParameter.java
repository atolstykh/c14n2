package ru.relex.c14n2.util;

/**
 * The internal representation of qualified element names, element names that
 * contain XPath 1.0 expressions, qualified attribute names, and unqualified
 * attribute names.
 */
public class QNameAwareParameter {
  private String name;
  private String ns;
  private String parentName;

  /**
   * Constructor.
   * 
   * @param name
   *          name
   * @param ns
   *          namespace
   */
  public QNameAwareParameter(String name, String ns) {
    this.name = name;
    this.ns = ns;
  }


  public QNameAwareParameter(String parentName, String parentNs, String name) {
    this.parentName=parentName;
    this.ns = parentNs;
    this.name = name;
  }

  /**
   * Returns a name of element, attribute, etc.
   * 
   * @return Returns a name
   */
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns a namespace of element, attribute, etc.
   * 
   * @return Returns a namespace
   */
  public String getNs() {
    return ns;
  }

  public void setNs(String ns) {
    this.ns = ns;
  }

  public String getParentName() {
    return parentName;
  }

  public void setParentName(String parentName) {
    this.parentName = parentName;
  }
}