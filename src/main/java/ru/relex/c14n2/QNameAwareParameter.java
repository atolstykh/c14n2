package ru.relex.c14n2;

/**
 * The internal representation of qualified element names, element names that
 * contain XPath 1.0 expressions, qualified attribute names, and unqualified
 * attribute names.
 */
public class QNameAwareParameter {
  private String name;
  private String ns;

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

}