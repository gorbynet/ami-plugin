package org.xmlcml.ami.plugin.regex;

import java.io.File;
import java.io.IOException;

import nu.xom.Element;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.ami.Fixtures;
import org.xmlcml.ami.plugin.AMIArgProcessor;
import org.xmlcml.ami.plugin.simple.SimpleArgProcessor;
import org.xmlcml.ami.plugin.simple.SimplePlugin;
import org.xmlcml.files.QuickscrapeNorma;
import org.xmlcml.xml.XMLUtil;

public class RegexPluginTest {
	
	private static final Logger LOG = Logger.getLogger(RegexPluginTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	@Test
	public void testRegexPlugin() throws IOException {
		QuickscrapeNorma qsNorma = new QuickscrapeNorma(Fixtures.TEST_BMC_15_1_511_QSN);
		File normaTemp = new File("target/bmc/regex/15_1_511_test");
		qsNorma.copyTo(normaTemp, true);
		Assert.assertFalse("results.xml", qsNorma.hasResultsXML());
		String[] args = {
				"-q", normaTemp.toString(),
				"-i", "scholarly.html",
				"-o", "results.xml",
				"--r.regex", "regex/common.xml",
		};
		RegexPlugin regexPlugin = new RegexPlugin(args);
		AMIArgProcessor argProcessor = (AMIArgProcessor) regexPlugin.getArgProcessor();
		Assert.assertNotNull(argProcessor);
		LOG.debug(argProcessor.getInputList());
		argProcessor.runAndOutput();
		QuickscrapeNorma qsNormaTemp = new QuickscrapeNorma(normaTemp);
		// fails at present
//		Assert.assertTrue("results.xml", qsNormaTemp.hasResultsXML());
	}
	
	/** process multiple Norma outputs.
	 * 
	 * @throws IOException
	 */
	@Test
	@Ignore // FIXME // Convert to regex
	public void testMultipleSimplePlugin() throws IOException {
		// this simply generates 7 temporary copies of the qsNormas
		int nfiles = Fixtures.TEST_MIXED_DIR.listFiles().length;
		File[] normaTemp = new File[nfiles];
		File test = new File("target/simple/multiple");
		if (test.exists()) FileUtils.deleteQuietly(test);
		for (int i = 0; i < nfiles; i++) {
			QuickscrapeNorma qsNorma = new QuickscrapeNorma(new File(Fixtures.TEST_MIXED_DIR, "file"+i));
			normaTemp[i] = new File(test, "file"+i);
			qsNorma.copyTo(normaTemp[i], true);
		}
		// this is the command line with multiple QSNorma directory names
		String[] args = {
				"-q", 
				normaTemp[0].toString(),
				normaTemp[1].toString(),
				normaTemp[2].toString(),
				normaTemp[3].toString(),
				normaTemp[4].toString(),
				normaTemp[5].toString(),
				normaTemp[6].toString(),
				"-i", "scholarly.html",
				"-o", "results.xml",
				"--s.simple", "foo", "bar"
		};
		SimplePlugin simplePlugin = new SimplePlugin(args);
		SimpleArgProcessor argProcessor = (SimpleArgProcessor) simplePlugin.getArgProcessor();
		argProcessor.runAndOutput();
		int[] size = {17624,4447,0, 4839,4311,4779,5288}; // file2 has smart quotes; fix HTMLFactory()
		for (int i = 0; i < nfiles; i++) {
			Element rootXML = (Element) XMLUtil.parseQuietlyToDocument(new File(test, "file"+i+"/results.xml")).getRootElement();
			Element resultXML = (Element) rootXML.getChildElements().get(0);
			Assert.assertEquals("file"+i, size[i], (int) new Integer(resultXML.getAttributeValue("wordCount")));
		}
	}
	

}