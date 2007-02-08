/*
 * Copyright 2004-2006 Stephen J. McConnell.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dpml.library;

/**
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public enum ProjectLocation
{
   /**
    * Project base directory.
    */
    BASEDIR, 
    
   /**
    * Supplimentary resources directory.
    */
    ECT,
    
   /**
    * Supplimentary main codebase resources directory.
    */
    ECT_MAIN,
    
   /**
    * Supplimentary test codebase resources directory.
    */
    ECT_TEST,
    
   /**
    * Supplimentary test runtime resources directory.
    */
    ECT_DATA,
    
   /**
    * Supplimentary doc resources directory.
    */
    ECT_DOCS,
    
   /**
    * Preference resources directory.
    */
    ECT_PREFS,
    
   /**
    * Main codebase directory.
    */
    SRC,
    
   /**
    * Main codebase directory.
    */
    SRC_MAIN,
    
   /**
    * Test codebase directory.
    */
    SRC_TEST,
    
   /**
    * Documentation codebase directory.
    */
    SRC_DOCS,
    
   /**
    * Target directory.
    */
    TARGET,
    
   /**
    * Interim filtered codebase parent directory.
    */
    BUILD,
    
   /**
    * Interim filtered main codebase parent directory.
    */
    BUILD_MAIN,
    
   /**
    * Interim filtered test codebase parent directory.
    */
    BUILD_TEST,
    
   /**
    * Interim filtered documentation codebase parent directory.
    */
    BUILD_DOCS,
    
   /**
    * Compiled classes directory.
    */
    CLASSES,
    
   /**
    * Compiled main codebase classes directory.
    */
    CLASSES_MAIN,
    
   /**
    * Compiled test codebase classes directory.
    */
    CLASSES_TEST,
    
   /**
    * Target deliverables directory.
    */
    DELIVERABLES,
    
   /**
    * Target preferences directory.
    */
    PREFS,
    
   /**
    * Target working test directory.
    */
    TEST,

   /**
    * Target reports directory.
    */
    REPORTS;
    
}
/*
    BASEDIR contains ECT, SRC, TARGET
    
    ECT contains MAIN, TEST, DATA
    SRC contains MAIN, TEST, DOCS
    TARGET directories BUILD, CLASSES, DELIVERABLES, TEST, REPORTS
    
      etc/MAIN --> target/build/MAIN
      etc/TEST --> target/build/TEST
      etc/DOCS --> target/build/DOCS
      etc/${X} --> target/${X}
      
      src/MAIN --> target/build/MAIN --> target/classes/MAIN
      src/TEST --> target/build/TEST --> target/classes/TEST
      src/DOCS --> target/build/DOCS --> target/DOCS
      
      target/${X}
      target/deliverables/${type}s/
      target/reports/MAIN
      target/reports/TEST
      target/test
      target/test/DATA
      
    getDirectory( SRC.MAIN )
    getDirectory( BUILD.MAIN )
    getDirectory( CLASSES.MAIN )
    getDirectory( REPORTS.TEST )
    
    // full list
    
    BASEDIR
    
      SRC
        SRC_MAIN
        SRC_TEST
        SRC_DOCS
      ETC
        ETC_MAIN
        ETC_TEST
        ECT_DATA
        ETC_DOCS
        ECT_PREFS
      TARGET
        CLASSES
          CLASSES_MAIN
          CLASSES_TEST
        DELIVERABLES
        PREFS
        REPORTS
        TEST
        
*/



