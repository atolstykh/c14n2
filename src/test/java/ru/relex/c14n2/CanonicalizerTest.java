package ru.relex.c14n2;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CanonicalizerTest {

  @Test
  public void testN1Default() {
    Assert.assertTrue(processTest("inC14N1", "c14nDefault"));
  }

  @Test
  public void testN1Comment() {
    Assert.assertTrue(processTest("inC14N1", "c14nComment"));
  }

  @Test
  public void testN2Default() {
    Assert.assertTrue(processTest("inC14N2", "c14nDefault"));
  }

  @Test
  public void testN2Trim() {
    Assert.assertTrue(processTest("inC14N2", "c14nTrim"));
  }

  @Test
  public void testN21Default() {
    Assert.assertTrue(processTest("inC14N2_1", "c14nDefault"));
  }

  @Test
  public void testN21Trim() {
    Assert.assertTrue(processTest("inC14N2_1", "c14nTrim"));
  }

  @Test
  public void testN3Default() {
    Assert.assertTrue(processTest("inC14N3", "c14nDefault"));
  }

  @Test
  public void testN3Prefix() {
    Assert.assertTrue(processTest("inC14N3", "c14nPrefix"));
  }

  @Test
  public void testN3Trim() {
    Assert.assertTrue(processTest("inC14N3", "c14nTrim"));
  }

  @Test
  public void testN4Default() {
    Assert.assertTrue(processTest("inC14N4", "c14nDefault"));
  }

  @Test
  public void testN4Trim() {
    Assert.assertTrue(processTest("inC14N4", "c14nTrim"));
  }

  @Test
  public void testN5Default() {
    Assert.assertTrue(processTest("inC14N5", "c14nDefault"));
  }

  @Test
  public void testN5Trim() {
    Assert.assertTrue(processTest("inC14N5", "c14nTrim"));
  }

  @Test
  public void testN6Default() {
    Assert.assertTrue(processTest("inC14N6", "c14nDefault"));
  }

  @Test
  public void testNsPushdownDefault() {
    Assert.assertTrue(processTest("inNsPushdown", "c14nDefault"));
  }

  @Test
  public void testNsPushdownPrefix() {
    Assert.assertTrue(processTest("inNsPushdown", "c14nPrefix"));
  }

  @Test
  public void testNsDefaultDefault() {
    Assert.assertTrue(processTest("inNsDefault", "c14nDefault"));
  }

  @Test
  public void testNsDefaultPrefix() {
    Assert.assertTrue(processTest("inNsDefault", "c14nPrefix"));
  }

  @Test
  public void testNsSortDefault() {
    Assert.assertTrue(processTest("inNsSort", "c14nDefault"));
  }

  @Test
  public void testNsSortPrefix() {
    Assert.assertTrue(processTest("inNsSort", "c14nPrefix"));
  }

  @Test
  public void testNsRedeclDefault() {
    Assert.assertTrue(processTest("inNsRedecl", "c14nDefault"));
  }

  @Test
  public void testNsRedeclPrefix() {
    Assert.assertTrue(processTest("inNsRedecl", "c14nPrefix"));
  }

  @Test
  public void testNsSuperfluousDefault() {
    Assert.assertTrue(processTest("inNsSuperfluous", "c14nDefault"));
  }

  @Test
  public void testNsSuperfluousPrefix() {
    Assert.assertTrue(processTest("inNsSuperfluous", "c14nPrefix"));
  }

  @Test
  public void testNsXmlDefault() {
    Assert.assertTrue(processTest("inNsXml", "c14nDefault"));
  }

  @Test
  public void testNsXmlPrefix() {
    Assert.assertTrue(processTest("inNsXml", "c14nPrefix"));
  }

  @Test
  public void testNsXmlQname() {
    Assert.assertTrue(processTest("inNsXml", "c14nQname"));
  }

  @Test
  public void testNsXmlPrefixQname() {
    Assert.assertTrue(processTest("inNsXml", "c14nPrefixQname"));
  }

  @Test
  public void testNsContentDefault() {
    Assert.assertTrue(processTest("inNsContent", "c14nDefault"));
  }

  @Test
  public void testNsContentQnameElem() {
    Assert.assertTrue(processTest("inNsContent", "c14nQnameElem"));
  }

  @Test
  public void testNsContentQnameXpathElem() {
    Assert.assertTrue(processTest("inNsContent", "c14nQnameXpathElem"));
  }

  @Test
  public void testNsContentPrefixQnameXPathElem() {
    Assert.assertTrue(processTest("inNsContent", "c14nPrefixQnameXPathElem"));
  }

  @Test
  public void testRC242Default() {
    Assert.assertTrue(processTest("inRC2_4_2", "c14nDefault"));
  }

  @Test
  public void testN22Trim() {
    Assert.assertTrue(processTest("inC14N2_2", "c14nTrim"));
  }

  @Test
  public void testN22TrimExcl1() {
    Assert.assertTrue(processTest("inC14N2_2", "c14nTrim",
        new ICanonicalizerExcludeList() {

          @Override
          public String getExcludeListName() {
            return "excl1";
          }

          @Override
          public List<Node> getExcludeList(Document doc) {
            NodeList nl = doc.getChildNodes();
            List<Node> nodes = new ArrayList<Node>();
            nodes.add(nl.item(0).getChildNodes().item(3));
            return nodes;
          }
        }));
  }

  @Test
  public void testN3DefaultExcl1() {
    Assert.assertTrue(processTest("inC14N3", "c14nDefault",
        new ICanonicalizerExcludeList() {

          @Override
          public String getExcludeListName() {
            return "excl1";
          }

          @Override
          public List<Node> getExcludeList(Document doc) {
            NodeList nl = doc.getChildNodes();
            List<Node> nodes = new ArrayList<Node>();
            NamedNodeMap e5Attrs = nl.item(1).getChildNodes().item(9)
                .getAttributes();
            String[] names = new String[] { "a:attr", "attr" };
            for (String name : names) {
              nodes.add(e5Attrs.getNamedItem(name));
            }
            return nodes;
          }
        }));
  }

  @Test
  public void testN3DefaultExcl2() {
    Assert.assertTrue(processTest("inC14N3", "c14nDefault",
        new ICanonicalizerExcludeList() {

          @Override
          public String getExcludeListName() {
            return "excl2";
          }

          @Override
          public List<Node> getExcludeList(Document doc) {
            NodeList nl = doc.getChildNodes();
            List<Node> nodes = new ArrayList<Node>();
            NamedNodeMap e5Attrs = nl.item(1).getChildNodes().item(9)
                .getAttributes();
            String[] names = new String[] { "a:attr", "attr", "xmlns:a" };
            for (String name : names) {
              nodes.add(e5Attrs.getNamedItem(name));
            }
            return nodes;
          }
        }));
  }

  @Test
  public void testNsContent1PrefixQnameXPathElem() {
    Assert.assertTrue(processTest("inNsContent_1", "c14nPrefixQnameXPathElem"));
  }

  @Test
  public void testN22TrimExcl2() {
    Assert.assertTrue(processTest("inC14N2_2", "c14nTrim",
        new ICanonicalizerExcludeList() {

          @Override
          public String getExcludeListName() {
            return "excl2";
          }

          @Override
          public List<Node> getExcludeList(Document doc) {
            NodeList nl = doc.getChildNodes();
            List<Node> nodes = new ArrayList<Node>();
            Node dirtyNode = nl.item(0).getChildNodes().item(3);
            // "xml:" attribute
            nodes.add(dirtyNode.getAttributes().item(0));
            // text node
            nodes.add(dirtyNode.getChildNodes().item(0));
            return nodes;
          }
        }));
  }

  @Test
  public void testWsseDefault() {
    Assert.assertTrue(processTest("inWsse", "c14nDefault"));
  }

  @Test
  public void testWssePrefix() {
    Assert.assertTrue(processTest("inWsse", "c14nPrefix"));
  }

  @Test
  public void testN3PrefixIncl1() {
    Assert.assertTrue(processTest("inC14N3", "c14nPrefix",
        new ICanonicalizerExcludeList() {

          @Override
          public String getExcludeListName() {
            return "incl1";
          }

          @Override
          public List<Node> getIncludeList(Document doc) {
            List<Node> nodes = new ArrayList<Node>();
            NodeList nl = doc.getChildNodes().item(1).getChildNodes();
            // e3
            nodes.add(nl.item(5));
            // e7
            nodes.add(nl.item(11).getChildNodes().item(1));
            return nodes;
          }
        }));
  }

  @Test
  public void testFlyXmlDefault() {
    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.newDocument();

      Node root = doc.createElement("doc");
      doc.appendChild(root);

      Element n1 = doc.createElement("b:doc1");
      root.appendChild(n1);

      Attr n1a1 = doc.createAttribute("xmlns:a");
      n1a1.setValue("http://a");
      n1.setAttributeNode(n1a1);
      Attr n1a2 = doc.createAttribute("xmlns:b");
      n1a2.setValue("http://b");
      n1.setAttributeNode(n1a2);
      Attr n1a3 = doc.createAttribute("attr");
      n1a3.setValue("attr1");
      n1.setAttributeNode(n1a3);

      Element n11 = doc.createElement("doc11");
      n1.appendChild(n11);

      Attr n11a1 = doc.createAttribute("a:attr");
      n11a1.setValue("attr2");
      n11.setAttributeNode(n11a1);

      String path = CanonicalizerTest.class.getProtectionDomain()
          .getCodeSource().getLocation().getPath();
      Assert
          .assertTrue(processTest(doc, path, "inFlyXml", "c14nDefault", null));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.assertFalse(false);
    }
  }

  @Test
  public void testNsDefault1Prefix() {
    Assert.assertTrue(processTest("inNsDefault_1", "c14nPrefix"));
  }

  private static boolean processTest(String inFileName, String paramName) {
    return processTest(inFileName, paramName, null);
  }

  private static boolean processTest(String inFileName, String paramName,
      ICanonicalizerExcludeList iExcludeList) {
    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

      String path = CanonicalizerTest.class.getProtectionDomain()
          .getCodeSource().getLocation().getPath();

      Document doc = dBuilder.parse(new FileInputStream(path + inFileName
          + ".xml"));
      return processTest(doc, path, inFileName, paramName, iExcludeList);
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return false;
  }

  private static boolean processTest(Document doc, String path,
      String inFileName, String paramName,
      ICanonicalizerExcludeList iExInCludeList) throws Exception {
    long l = System.currentTimeMillis();
    String result = "";
    List<Node> includeList = iExInCludeList != null ? iExInCludeList
        .getIncludeList(doc) : null;
    List<Node> excludeList = iExInCludeList != null ? iExInCludeList
        .getExcludeList(doc) : null;
    if (includeList != null) {
      if (excludeList != null) {
        result = DOMCanonicalizer.canonicalize(doc, includeList, excludeList,
            getParams(paramName));
      } else {
        result = DOMCanonicalizer.canonicalize(doc, includeList,
            getParams(paramName));
      }
    } else {
      if (excludeList != null) {
        result = DOMCanonicalizer.canonicalize(doc, null, excludeList,
            getParams(paramName));
      } else {
        result = DOMCanonicalizer.canonicalize(doc, getParams(paramName));
      }
    }
    System.out
        .println("l = " + (System.currentTimeMillis() - l) / 1000.0 + "s");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    FileInputStream fis = new FileInputStream(
        path
            + "out_"
            + inFileName
            + "_"
            + paramName
            + (excludeList != null || includeList != null ? ("_" + iExInCludeList
                .getExcludeListName()) : "") + ".xml");
    byte[] bytes = new byte[1024];
    int cnt = 0;
    while ((cnt = fis.read(bytes)) > -1)
      baos.write(bytes, 0, cnt);
    fis.close();
    baos.flush();
    baos.close();
    boolean b = true;
    for (int i = 0; i < result.length(); i++)
      if (result.getBytes("UTF-8")[i] != baos.toByteArray()[i]) {
        System.out.println("Error pos: " + i + " res:"
            + result.getBytes("UTF-8")[i] + " base:" + baos.toByteArray()[i]);
        b = false;
        break;
      }
    System.out.println("'" + baos.toString("UTF-8") + "'\n" + "'" + result
        + "'");
    return b;
  }

  private static Parameters getParams(String paramName) {
    Parameters params = new Parameters();
    if ("c14nDefault".equals(paramName)) {
    } else if ("c14nComment".equals(paramName)) {
      params.setIgnoreComments(false);
    } else if ("c14nTrim".equals(paramName)) {
      params.setTrimTextNodes(true);
    } else if ("c14nPrefix".equals(paramName)) {
      params.setPrefixRewrite(Parameters.SEQUENTIAL);
    } else if ("c14nQname".equals(paramName)) {
      params.getQnameAwareAttributes().add(
          new QNameAwareParameter("type",
              "http://www.w3.org/2001/XMLSchema-instance"));
    } else if ("c14nPrefixQname".equals(paramName)) {
      params.setPrefixRewrite(Parameters.SEQUENTIAL);
      params.getQnameAwareAttributes().add(
          new QNameAwareParameter("type",
              "http://www.w3.org/2001/XMLSchema-instance"));
    } else if ("c14nQnameElem".equals(paramName)) {
      params.getQnameAwareElements().add(
          new QNameAwareParameter("bar", "http://a"));
    } else if ("c14nQnameXpathElem".equals(paramName)) {
      params.getQnameAwareElements().add(
          new QNameAwareParameter("bar", "http://a"));
      params.getQnameAwareXPathElements().add(
          new QNameAwareParameter("IncludedXPath",
              "http://www.w3.org/2010/xmldsig2#"));
    } else if ("c14nPrefixQnameXPathElem".equals(paramName)) {
      params.setPrefixRewrite(Parameters.SEQUENTIAL);
      params.getQnameAwareElements().add(
          new QNameAwareParameter("bar", "http://a"));
      params.getQnameAwareXPathElements().add(
          new QNameAwareParameter("IncludedXPath",
              "http://www.w3.org/2010/xmldsig2#"));
    }
    return params;
  }
}