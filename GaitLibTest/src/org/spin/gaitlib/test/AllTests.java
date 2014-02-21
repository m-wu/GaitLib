package org.spin.gaitlib.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTests extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		// $JUnit-BEGIN$
		suite.addTestSuite(GaitAnalysisTest.class);
		suite.addTestSuite(FiltersTest.class);
		// $JUnit-END$
		return suite;
	}

}
