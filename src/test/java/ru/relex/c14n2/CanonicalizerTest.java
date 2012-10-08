package ru.relex.c14n2;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CanonicalizerTest extends TestCase {

	public CanonicalizerTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		return new TestSuite(CanonicalizerTest.class);
	}

	public void testN1Default() {
		assertTrue(processTest("inC14N1", "c14nDefault"));
	}

	public void testN1Comment() {
		assertTrue(processTest("inC14N1", "c14nComment"));
	}

	public void testN2Default() {
		assertTrue(processTest("inC14N2", "c14nDefault"));
	}

	public void testN2Trim() {
		assertTrue(processTest("inC14N2", "c14nTrim"));
	}

	public void testN21Default() {
		assertTrue(processTest("inC14N2_1", "c14nDefault"));
	}

	public void testN21Trim() {
		assertTrue(processTest("inC14N2_1", "c14nTrim"));
	}

	public void testN3Default() {
		assertTrue(processTest("inC14N3", "c14nDefault"));
	}

	public void testN3Prefix() {
		assertTrue(processTest("inC14N3", "c14nPrefix"));
	}

	public void testN3Trim() {
		assertTrue(processTest("inC14N3", "c14nTrim"));
	}

	public void testN4Default() {
		assertTrue(processTest("inC14N4", "c14nDefault"));
	}

	public void testN4Trim() {
		assertTrue(processTest("inC14N4", "c14nTrim"));
	}

	public void testN5Default() {
		assertTrue(processTest("inC14N5", "c14nDefault"));
	}

	public void testN5Trim() {
		assertTrue(processTest("inC14N5", "c14nTrim"));
	}

	public void testN6Default() {
		assertTrue(processTest("inC14N6", "c14nDefault"));
	}

	public void testNsPushdownDefault() {
		assertTrue(processTest("inNsPushdown", "c14nDefault"));
	}

	public void testNsPushdownPrefix() {
		assertTrue(processTest("inNsPushdown", "c14nPrefix"));
	}

	public void testNsDefaultDefault() {
		assertTrue(processTest("inNsDefault", "c14nDefault"));
	}

	public void testNsDefaultPrefix() {
		assertTrue(processTest("inNsDefault", "c14nPrefix"));
	}

	public void testNsSortDefault() {
		assertTrue(processTest("inNsSort", "c14nDefault"));
	}

	public void testNsSortPrefix() {
		assertTrue(processTest("inNsSort", "c14nPrefix"));
	}

	public void testNsRedeclDefault() {
		assertTrue(processTest("inNsRedecl", "c14nDefault"));
	}

	public void testNsRedeclPrefix() {
		assertTrue(processTest("inNsRedecl", "c14nPrefix"));
	}

	public void testNsSuperfluousDefault() {
		assertTrue(processTest("inNsSuperfluous", "c14nDefault"));
	}

	public void testNsSuperfluousPrefix() {
		assertTrue(processTest("inNsSuperfluous", "c14nPrefix"));
	}

	public void testNsXmlDefault() {
		assertTrue(processTest("inNsXml", "c14nDefault"));
	}

	public void testNsXmlPrefix() {
		assertTrue(processTest("inNsXml", "c14nPrefix"));
	}

	public void testNsXmlQname() {
		assertTrue(processTest("inNsXml", "c14nQname"));
	}

	public void testNsXmlPrefixQname() {
		assertTrue(processTest("inNsXml", "c14nPrefixQname"));
	}

	public void testNsContentDefault() {
		assertTrue(processTest("inNsContent", "c14nDefault"));
	}

	public void testNsContentQnameElem() {
		assertTrue(processTest("inNsContent", "c14nQnameElem"));
	}

	public void testNsContentQnameXpathElem() {
		assertTrue(processTest("inNsContent", "c14nQnameXpathElem"));
	}

	public void testNsContentPrefixQnameXPathElem() {
		assertTrue(processTest("inNsContent", "c14nPrefixQnameXPathElem"));
	}

	public void testRC242Default() {
		assertTrue(processTest("inRC2_4_2", "c14nDefault"));
	}

	private static boolean processTest(String inFileName, String paramName) {
		try {
			long l = System.currentTimeMillis();
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			String path = CanonicalizerTest.class.getProtectionDomain()
					.getCodeSource().getLocation().getPath();

			Document doc = dBuilder.parse(new FileInputStream(path + inFileName
					+ ".xml"));
			DOMCanonicalizer rf = new DOMCanonicalizer(doc,
					getParams(paramName));
			String result = rf.canonicalize();
			System.out.println("l = " + (System.currentTimeMillis() - l)
					/ 1000.0 + "s");

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			FileInputStream fis = new FileInputStream(path + "out_"
					+ inFileName + "_" + paramName + ".xml");
			byte[] bytes = new byte[1024];
			int cnt = 0;
			while ((cnt = fis.read(bytes)) > -1)
				baos.write(bytes, 0, cnt);
			fis.close();
			baos.flush();
			baos.close();
			for (int i = 0; i < result.length(); i++)
				if (result.charAt(i) != baos.toString("UTF-8").charAt(i)) {
					i = 0;
					break;
				}
			System.out.println(baos.toString("UTF-8") + " "
					+ result.equals(baos.toString("UTF-8")));
			return result.equals(baos.toString("UTF-8"));
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
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