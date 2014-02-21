package org.spin.gaitlib.test;

import junit.framework.TestCase;

import org.spin.gaitlib.GaitAnalysis;
import org.spin.gaitlib.core.GaitData;
import org.spin.gaitlib.core.IGaitUpdateListener;

public class GaitAnalysisTest extends TestCase {

	private static GaitAnalysis gaitAnalysis;

	public GaitAnalysisTest(String name) {
		super(name);
		gaitAnalysis = new GaitAnalysis();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetSignalListener() {
		assertNotNull(gaitAnalysis.getSignalListener());
	}

	public void testRegisterAndRemoveGaitUpdateListener() {
		IGaitUpdateListener listener = new IGaitUpdateListener() {

			public void onGaitUpdated(GaitData data) {
			}
		};
		IGaitUpdateListener listener2 = new IGaitUpdateListener() {

			public void onGaitUpdated(GaitData data) {
			}
		};

		assertFalse(gaitAnalysis.removeGaitUpdateListener(listener));
		assertFalse(gaitAnalysis.removeGaitUpdateListener(listener2));
		assertTrue(gaitAnalysis.registerGaitUpdateListener(listener));
		assertTrue(gaitAnalysis.registerGaitUpdateListener(listener2));
		assertTrue(gaitAnalysis.removeGaitUpdateListener(listener));
		assertTrue(gaitAnalysis.removeGaitUpdateListener(listener2));
	}

	public void testStartGaitAnalysis() {
		assertFalse(gaitAnalysis.isGaitAnalysisRunning());
		gaitAnalysis.startGaitAnalysis();
		assertTrue(gaitAnalysis.isGaitAnalysisRunning());
	}

	public void testStopGaitAnalysis() {
		assertTrue(gaitAnalysis.isGaitAnalysisRunning());
		gaitAnalysis.stopGaitAnalysis();
		assertFalse(gaitAnalysis.isGaitAnalysisRunning());
	}

}
