/*
 * Created on 08.08.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.metro.facility.presentationservice.test;


/**
 * @author Andreas
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TestStarter
{

    public static void main(String args[])
    {

        ViewMock vm = new ViewMock();
        ModellMock model = new ModellMock();
        // register a listener on "ViewMock"
        model.initialize();
        // start test
        vm.testApply();
        // vm.testOk();
    }
}
