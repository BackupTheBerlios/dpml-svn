/* 
 * Copyright 2004 Apache Software Foundation
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

package tutorial;

import java.util.Random;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Disposable;

/**
 * An implementation of a random number generator.
 *
 * @avalon.component version="1.0" name="random" lifestyle="singleton"
 * @avalon.service type="tutorial.RandomGenerator" version="1.0"
 */
public class RandomGeneratorProvider extends AbstractLogEnabled 
  implements Initializable, RandomGenerator, Disposable
{

    private Random m_random = new Random();

    public void initialize()
    {
        getLogger().info( "initialization" );
    }

   /**
    * Return a random integer
    * @return the random number
    */
    public int getRandom()
    {
        getLogger().info( "processing request" );
        return m_random.nextInt();
    }

    public void dispose()
    {
        getLogger().info( "disposal" );
    }

}
