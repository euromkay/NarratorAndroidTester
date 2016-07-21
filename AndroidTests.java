package iowrapper;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AndroidTests {

	public static Test suite() {

		TestSuite suite = new TestSuite(AndroidTests.class.getName());
		
		suite.addTestSuite(PhoneGUITests.class);
		
		
		return suite;
	}


}
