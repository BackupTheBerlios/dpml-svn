package org.apache.avalon.repository.util;

import junit.framework.TestCase;
import java.io.File;

public class LoaderUtilsTest extends TestCase
{

    public LoaderUtilsTest(String name)
    {
        super(name);
    }
    
    public void testIsSnapshot()
    {
        // First, try without file extension
        assertTrue(LoaderUtils.isSnapshot(new File("some-file-SNAPSHOT")));
        assertTrue(LoaderUtils.isSnapshot(new File(".some-file-SNAPSHOT")));
        assertTrue(LoaderUtils.isSnapshot(new File("/home/usr/some/path/some-file-SNAPSHOT")));
        assertTrue(LoaderUtils.isSnapshot(new File("C:\\home\\usr\\some\\parh\\some-file-SNAPSHOT")));
        
        assertFalse(LoaderUtils.isSnapshot(new File("some-file-snapshot")));
        assertFalse(LoaderUtils.isSnapshot(new File("some-file-SAPSHOT")));
        assertFalse(LoaderUtils.isSnapshot(new File("some-fileSNAPSHOT")));
        assertFalse(LoaderUtils.isSnapshot(new File("some-file-SNAPSHOT1")));
        assertFalse(LoaderUtils.isSnapshot(null));

        // With 0-char file extensions
        assertTrue(LoaderUtils.isSnapshot(new File("some-file-SNAPSHOT.")));
        assertTrue(LoaderUtils.isSnapshot(new File(".some-file-SNAPSHOT.")));
        assertTrue(LoaderUtils.isSnapshot(new File("/home/usr/some/path/some-file-SNAPSHOT.")));
        assertTrue(LoaderUtils.isSnapshot(new File("C:\\home\\usr\\some\\parh\\some-file-SNAPSHOT.")));
        
        assertFalse(LoaderUtils.isSnapshot(new File("some-file-snapshot.")));
        assertFalse(LoaderUtils.isSnapshot(new File("some-file-SAPSHOT.")));
        assertFalse(LoaderUtils.isSnapshot(new File("some-fileSNAPSHOT.")));
        assertFalse(LoaderUtils.isSnapshot(new File("some-file-SNAPSHOT1.")));

        // With 1-char file extensions
        assertTrue(LoaderUtils.isSnapshot(new File("some-file-SNAPSHOT.a")));
        assertTrue(LoaderUtils.isSnapshot(new File(".some-file-SNAPSHOT.a")));
        assertTrue(LoaderUtils.isSnapshot(new File("/home/usr/some/path/some-file-SNAPSHOT.a")));
        assertTrue(LoaderUtils.isSnapshot(new File("C:\\home\\usr\\some\\parh\\some-file-SNAPSHOT.a")));
        
        assertFalse(LoaderUtils.isSnapshot(new File("some-file-snapshot.a")));
        assertFalse(LoaderUtils.isSnapshot(new File("some-file-SAPSHOT.a")));
        assertFalse(LoaderUtils.isSnapshot(new File("some-fileSNAPSHOT.a")));
        assertFalse(LoaderUtils.isSnapshot(new File("some-file-SNAPSHOT1.a")));

        // With 2-char file extensions
        assertTrue(LoaderUtils.isSnapshot(new File("some-file-SNAPSHOT.ab")));
        assertTrue(LoaderUtils.isSnapshot(new File(".some-file-SNAPSHOT.ab")));
        assertTrue(LoaderUtils.isSnapshot(new File("/home/usr/some/path/some-file-SNAPSHOT.ab")));
        assertTrue(LoaderUtils.isSnapshot(new File("C:\\home\\usr\\some\\parh\\some-file-SNAPSHOT.ab")));
        
        assertFalse(LoaderUtils.isSnapshot(new File("some-file-snapshot.ab")));
        assertFalse(LoaderUtils.isSnapshot(new File("some-file-SAPSHOT.ab")));
        assertFalse(LoaderUtils.isSnapshot(new File("some-fileSNAPSHOT.ab")));
        assertFalse(LoaderUtils.isSnapshot(new File("some-file-SNAPSHOT1.ab")));

        // With 3-char file extensions
        assertTrue(LoaderUtils.isSnapshot(new File("some-file-SNAPSHOT.abc")));
        assertTrue(LoaderUtils.isSnapshot(new File(".some-file-SNAPSHOT.abc")));
        assertTrue(LoaderUtils.isSnapshot(new File("/home/usr/some/path/some-file-SNAPSHOT.abc")));
        assertTrue(LoaderUtils.isSnapshot(new File("C:\\home\\usr\\some\\parh\\some-file-SNAPSHOT.abc")));
        
        assertFalse(LoaderUtils.isSnapshot(new File("some-file-snapshot.abc")));
        assertFalse(LoaderUtils.isSnapshot(new File("some-file-SAPSHOT.abc")));
        assertFalse(LoaderUtils.isSnapshot(new File("some-fileSNAPSHOT.abc")));
        assertFalse(LoaderUtils.isSnapshot(new File("some-file-SNAPSHOT1.abc")));

        // With 4-char file extensions
        assertTrue(LoaderUtils.isSnapshot(new File("some-file-SNAPSHOT.abcd")));
        assertTrue(LoaderUtils.isSnapshot(new File(".some-file-SNAPSHOT.abcd")));
        assertTrue(LoaderUtils.isSnapshot(new File("/home/usr/some/path/some-file-SNAPSHOT.abcd")));
        assertTrue(LoaderUtils.isSnapshot(new File("C:\\home\\usr\\some\\parh\\some-file-SNAPSHOT.abcd")));
        
        assertFalse(LoaderUtils.isSnapshot(new File("some-file-snapshot.abcd")));
        assertFalse(LoaderUtils.isSnapshot(new File("some-file-SAPSHOT.abcd")));
        assertFalse(LoaderUtils.isSnapshot(new File("some-fileSNAPSHOT.abcd")));
        assertFalse(LoaderUtils.isSnapshot(new File("some-file-SNAPSHOT1.abcd")));

        // We'll assume that it will pass for n-char file extensions
        
    }

}

