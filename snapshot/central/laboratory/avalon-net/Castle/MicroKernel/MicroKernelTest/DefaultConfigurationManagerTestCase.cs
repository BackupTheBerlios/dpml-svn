// Copyright 2004 The Apache Software Foundation
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

namespace Apache.Avalon.Castle.MicroKernel.Test
{
	using System;

	using NUnit.Framework;

	using Apache.Avalon.Framework;
	using Apache.Avalon.Castle.MicroKernel;
	using Apache.Avalon.Castle.MicroKernel.Model;
	using Apache.Avalon.Castle.MicroKernel.Model.Default;
	using Apache.Avalon.Castle.MicroKernel.Subsystems.Configuration;
	using Apache.Avalon.Castle.MicroKernel.Subsystems.Configuration.Default;
	using Apache.Avalon.Castle.MicroKernel.Test.Components;

	/// <summary>
	/// Summary description for DefaultConfigurationManagerTestCase.
	/// </summary>
	[TestFixture]
	public class DefaultConfigurationManagerTestCase : Assertion
	{
		[Test]
		public void TestAdd()
		{
			DefaultConfigurationManager config = new DefaultConfigurationManager();
			
			config.Add( "key1", new DefaultConfiguration() );
			config.Add( "key2", new DefaultConfiguration() );

			AssertEquals( 2, config.Configurations.Length );
		}

		[Test]
		public void TestObtention()
		{
			DefaultConfigurationManager config = new DefaultConfigurationManager();
			
			config.Add( "key1", new DefaultConfiguration() );
			config.Add( "key2", new DefaultConfiguration() );

			AssertNotNull( config.GetConfiguration( "key1" ) );
			AssertNotNull( config.GetConfiguration( "key2" ) );
			AssertNotNull( config.GetConfiguration( "KeY1" ) );
			AssertEquals( DefaultConfiguration.EmptyConfiguration, config.GetConfiguration( "key3" ) );
		}
	}
}
