package ru.relex.c14n2;

import org.apache.xml.serializer.utils.DOM2Helper;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.*;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ru.relex.c14n2.util.Parameters;
import ru.relex.c14n2.util.QNameAwareParameter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CanonicalizerTest {

    @Test(threadPoolSize = 10, invocationCount = 1000, invocationTimeOut = 0) //, expectedExceptions = {NullPointerException.class, AssertionError.class})
    public void testMultiThread() {
/* not implemented    testN1Default();
    testN1Comment(); */
        testN2Default();
        testN2Trim();
        testN3Default();
        testN3Prefix();
        testN3Trim();
        testN4Default();
        testN4Trim();
        testN5Default();
        testN5Trim();
        testN6Default();
        testNsPushdownDefault();
        testNsPushdownPrefix();
        testNsDefaultDefault();
        testNsDefaultPrefix();
        testNsSortDefault();
        testNsSortPrefix();
        testNsRedeclDefault();
        testNsRedeclPrefix();
        testNsSuperfluousDefault();
        testNsSuperfluousPrefix();
        testNsXmlDefault();
        testNsXmlPrefix();
        testNsXmlQname();
        testNsXmlPrefixQname();
        testNsContentDefault();
        testNsContentQnameElem();
        testNsContentQnameXpathElem();
        testNsContentPrefixQnameXPathElem();
    }

    //  comment and pi not implemented yet
// @Test
//  public void testN1Default() {
//    Assert.assertTrue(processTest("1", "inC14N1", "c14nDefault"));
    // }


    //  comment and pi not implemented yet
//  @Test
//  public void testN1Comment() {
    //   Assert.assertTrue(processTest("2", "inC14N1", "c14nComment"));
    // }
//

    @Test
    public void testN2Default() {
        Assert.assertTrue(processTest("3", "inC14N2", "c14nDefault"));
    }

    @Test
    public void testN2Trim() {
        Assert.assertTrue(processTest("4", "inC14N2", "c14nTrim"));
    }

    @Test
    public void testN21Default() {
        Assert.assertTrue(processTest("1r", "inC14N2_1", "c14nDefault"));
    }

    @Test
    public void testN21Trim() {
        Assert.assertTrue(processTest("2r", "inC14N2_1", "c14nTrim"));
    }

    @Test
    public void testN3Default() {
        Assert.assertTrue(processTest("5", "inC14N3", "c14nDefault"));
    }

    //
    // work with PVDNP_MODE=false
    //

    @Test
    public void testTemp() {
        Assert.assertTrue(processTest("34f", "inC14N2", "c14nDefault"));
    }

    @Test
    public void testN3Prefix() {
        Assert.assertTrue(processTest("6", "inC14N3", "c14nPrefix"));
    }

    @Test
    public void testN3Trim() {
        Assert.assertTrue(processTest("7", "inC14N3", "c14nTrim"));
    }

    @Test
    public void testN4Default() {
        Assert.assertTrue(processTest("8", "inC14N4", "c14nDefault"));
    }

    @Test
    public void testN4Trim() {
        Assert.assertTrue(processTest("9", "inC14N4", "c14nTrim"));
    }

    @Test
    public void testN5Default() {
        Assert.assertTrue(processTest("10", "inC14N5", "c14nDefault"));
    }

    @Test
    public void testN5Trim() {
        Assert.assertTrue(processTest("11", "inC14N5", "c14nTrim"));
    }


    @Test
    public void testN6Default() {
        Assert.assertTrue(processTest("12", "inC14N6", "c14nDefault"));
    }

    @Test
    public void testNsPushdownDefault() {
        Assert.assertTrue(processTest("13", "inNsPushdown", "c14nDefault"));
    }

    @Test
    public void testNsPushdownPrefix() {
        Assert.assertTrue(processTest("14", "inNsPushdown", "c14nPrefix"));
    }


    @Test
    public void testNsDefaultDefault() {
        Assert.assertTrue(processTest("15", "inNsDefault", "c14nDefault"));
    }

    @Test
    public void testNsDefaultPrefix() {
        Assert.assertTrue(processTest("16", "inNsDefault", "c14nPrefix"));
    }


    @Test
    public void testNsSortDefault() {
        Assert.assertTrue(processTest("17", "inNsSort", "c14nDefault"));
    }

    @Test
    public void testNsSortPrefix() {
        Assert.assertTrue(processTest("18", "inNsSort", "c14nPrefix"));
    }

    @Test
    public void testNsRedeclDefault() {
        Assert.assertTrue(processTest("19", "inNsRedecl", "c14nDefault"));
    }

    @Test
    public void testNsRedeclPrefix() {
        Assert.assertTrue(processTest("20", "inNsRedecl", "c14nPrefix"));
    }


    @Test
    public void testNsSuperfluousDefault() {
        Assert.assertTrue(processTest("21", "inNsSuperfluous", "c14nDefault"));
    }

    @Test
    public void testNsSuperfluousPrefix() {
        Assert.assertTrue(processTest("22", "inNsSuperfluous", "c14nPrefix"));
    }

    @Test
    public void testNsXmlDefault() {
        Assert.assertTrue(processTest("23", "inNsXml", "c14nDefault"));
    }

    @Test
    public void testNsXmlPrefix() {
        Assert.assertTrue(processTest("24", "inNsXml", "c14nPrefix"));
    }

    @Test
    public void testNsXmlQname() {
        Assert.assertTrue(processTest("25", "inNsXml", "c14nQname"));
    }


    @Test
    public void testNsXmlPrefixQname() {
        Assert.assertTrue(processTest("26", "inNsXml", "c14nPrefixQname"));
    }

    @Test
    public void testNsContentDefault() {
        Assert.assertTrue(processTest("27", "inNsContent", "c14nDefault"));
    }

    @Test
    public void testNsContentQnameElem() {
        Assert.assertTrue(processTest("28", "inNsContent", "c14nQnameElem"));
    }

    @Test
    public void testNsContentQnameXpathElem() {
        Assert.assertTrue(processTest("29", "inNsContent", "c14nQnameXpathElem"));
    }


    @Test
    public void testNsContentPrefixQnameXPathElem() {
        Assert.assertTrue(processTest("30", "inNsContent",
                "c14nPrefixQnameXpathElem"));
    }

    @Test
    public void testRC242Default() {
        Assert.assertTrue(processTest("3r", "inRC2_4_2", "c14nDefault"));
    }

    @Test
    public void testN22Trim() {
        Assert.assertTrue(processTest("4r", "inC14N2_2", "c14nTrim"));
    }

    @Test
    public void testN22TrimExcl1() {
        Assert.assertTrue(processTest("5r", "inC14N2_2", "c14nTrim",
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
    public void testN3DefaultExcl2() {
        Assert.assertTrue(processTest("7r", "inC14N3", "c14nDefault"));
    }

    @Test
    public void testNsContent1PrefixQnameXPathElem() {
        Assert.assertTrue(processTest("8r", "inNsContent_1",
                "c14nPrefixQnameXpathElem"));
    }


    @Test
    public void testN22TrimExcl2() {
        Assert.assertTrue(processTest("9r", "inC14N2_2", "c14nTrim",
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
        Assert.assertTrue(processTest("10r", "inWsse", "c14nDefault"));
    }

    @Test
    public void testWssePrefix() {
        Assert.assertTrue(processTest("11r", "inWsse", "c14nPrefix"));
    }

    @Test
    public void testN3PrefixIncl1() {
        Assert.assertTrue(processTest("12r", "inC14N3", "c14nPrefix",
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

    // accoding to   xml-c14n2-testcases: 3.2 Default namespace declarations
    // wrong test
    //
    //  @Test
    //  public void testNsDefault1Prefix() {
    //    Assert.assertTrue(processTest("14r", "inNsDefault_1", "c14nPrefix"));
    //  }


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
            Assert.assertTrue(processTest("13r", doc, path, "inFlyXml",
                    "c14nDefault", null));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertFalse(false);
        }
    }

    private static boolean processTest(String testNumber, String inFileName,
                                       String paramName) {
        return processTest(testNumber, inFileName, paramName, null);
    }


    /*
     *********************

        Temp method

     *********************
     */

    @Test
    public void getPath() {
        try {
            String path = CanonicalizerTest.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath();
            System.out.println(path);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new FileInputStream(path + "inC14N1" + ".xml"));
            Node node = doc.getDocumentElement();
        }
        catch (Exception x) {
            x.printStackTrace();
        }
    }

    private static boolean processTest(String testNumber, String inFileName,
                                       String paramName, ICanonicalizerExcludeList iExcludeList) {
        try {
            /*EntityResolver entityResolver = new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId)
                        throws SAXException, IOException {
                    if (systemId.contains("doc.dtd")) {
                        return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
                    } else
                        return null;
                }
            };*/
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            //dBuilder.setEntityResolver(entityResolver);

            String path = CanonicalizerTest.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath();

            Document doc = dBuilder.parse(new FileInputStream(path + inFileName
                    + ".xml"));
            return processTest(testNumber, doc, path, inFileName, paramName,
                    iExcludeList);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean processTest(String testNumber, Document doc,
                                       String path, String inFileName, String paramName,
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
        l = System.currentTimeMillis() - l;

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
        boolean b = l <= 200;
        for (int i = 0; b && i < result.length(); i++) {
            if (result.getBytes("UTF-8")[i] != baos.toByteArray()[i]) {
                System.out.println("Error pos: " + i + " res:"
                        + result.getBytes("UTF-8")[i] + " base:" + baos.toByteArray()[i]);
                b = false;
            }
        }
        if (!b) {
            System.out.println("---Result---\n" + result
                    + "\n---Base---\n" + baos.toString("UTF-8") + "\n---time---\n" + l / 1000.0
                    + "\n------");

            /*
            java.io.FileOutputStream res = new FileOutputStream("./result");
            res.write(result.getBytes());
            res.close();

            java.io.FileOutputStream base = new FileOutputStream("./base");
            base.write(baos.toByteArray());
            base.close();
*/

        } else {
            System.out.println("Test " + testNumber + " (" + l / 1000.0
                    + " sec) – ok");
        }
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
            params.getQnameAwareQualifiedAttributes().add(
                    new QNameAwareParameter("type",
                            "http://www.w3.org/2001/XMLSchema-instance"));
        } else if ("c14nPrefixQname".equals(paramName)) {
            params.setPrefixRewrite(Parameters.SEQUENTIAL);
            params.getQnameAwareQualifiedAttributes().add(
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
        } else if ("c14nPrefixQnameXpathElem".equals(paramName)) {
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