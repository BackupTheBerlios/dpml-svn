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

namespace Apache.Avalon.Castle.Windsor.Test.Profile
{
	using System;

	using NUnit.Framework;

	using Apache.Avalon.Framework;
	using Apache.Avalon.Castle.Windsor.Profile;
	using Apache.Avalon.Castle.Windsor.Profile.Builders.Xml;
	using Apache.Avalon.Castle.Windsor.Test.Component.Simple;
	using Apache.Avalon.Castle.Windsor.Test.Component.Facility;
	using Apache.Avalon.Castle.Windsor.Test.Component.Subsystem;

	/// <summary>
	/// Summary description for XmlProfileBuilderTestCase.
	/// </summary>
	[TestFixture]
	public class XmlProfileBuilderTestCase : Assertion
	{
		[Test]
		public void BuildFromStringWithBaseAssembly()
		{
			String contents = 
				"<container>" + 
				"  <components baseAssembly=\"Apache.Avalon.Castle.Windsor.Test, Version=1.0.0.0\">" +
				"    <component key=\"a\" service=\"Apache.Avalon.Castle.Windsor.Test.Component.Simple.IService\" " +
				"               implementation=\"Apache.Avalon.Castle.Windsor.Test.Component.Simple.DefaultService\">  " +
				"      <configuration>" +
				"      </configuration>" +
				"    </component>" +
				"  </components>" +
				"</container>";

			IContainerProfile profile = new XmlProfileBuilder().Build(contents);

			AssertProfileComponent(profile);
		}

		[Test]
		public void BuildFromStringWithoutBaseAssembly()
		{
			String contents = 
				"<container>" + 
				"  <components>" +
				"    <component key=\"a\" service=\"Apache.Avalon.Castle.Windsor.Test.Component.Simple.IService,Apache.Avalon.Castle.Windsor.Test, Version=1.0.0.0\" " +
				"               implementation=\"Apache.Avalon.Castle.Windsor.Test.Component.Simple.DefaultService,Apache.Avalon.Castle.Windsor.Test, Version=1.0.0.0\">  " +
				"      <configuration>" +
				"      </configuration>" +
				"    </component>" +
				"  </components>" +
				"</container>";

			IContainerProfile profile = new XmlProfileBuilder().Build(contents);

			AssertProfileComponent(profile);
		}

		[Test]
		public void BuildFromStringWithInvalidTypes()
		{
			String contents = 
				"<container>" + 
				"  <components>" +
				"    <component key=\"a\" service=\"Apache.Avalon.Castle.Windsor.Test.Component.Simple.INoSuchService,Apache.Avalon.Castle.Windsor.Test, Version=1.0.0.0\" " +
				"               implementation=\"Apache.Avalon.Castle.Windsor.Test.Component.Simple.DefaultService,Apache.Avalon.Castle.Windsor.Test, Version=1.0.0.0\">  " +
				"      <configuration>" +
				"      </configuration>" +
				"    </component>" +
				"  </components>" +
				"</container>";

			try
			{
				new XmlProfileBuilder().Build(contents);
				Fail("Should not be able to construct profile with invalid arguments");
			}
			catch(ProfileException)
			{
				// Expected
			}
		}

		[Test]
		public void BuildFromStringWithInvalidTypes2()
		{
			String contents = 
				"<container>" + 
				"  <components>" +
				"    <component key=\"a\" service=\"Apache.Avalon.Castle.Windsor.Test.Component.Simple.INoSuchService\" " +
				"               implementation=\"Apache.Avalon.Castle.Windsor.Test.Component.Simple.DefaultService,Apache.Avalon.Castle.Windsor.Test, Version=1.0.0.0\">  " +
				"      <configuration>" +
				"      </configuration>" +
				"    </component>" +
				"  </components>" +
				"</container>";

			try
			{
				new XmlProfileBuilder().Build(contents);
				Fail("Should not be able to construct profile with invalid arguments");
			}
			catch(ProfileException)
			{
				// Expected
			}
		}

		[Test]
		public void BuildFromStringWithLifestyleAndActivationOverride()
		{
			String contents = 
				"<container>" + 
				"  <components>" +
				"    <component key=\"a\" lifestyle=\"singleton\" activation=\"start\" service=\"Apache.Avalon.Castle.Windsor.Test.Component.Simple.IService,Apache.Avalon.Castle.Windsor.Test, Version=1.0.0.0\" " +
				"               implementation=\"Apache.Avalon.Castle.Windsor.Test.Component.Simple.DefaultService,Apache.Avalon.Castle.Windsor.Test, Version=1.0.0.0\">  " +
				"      <configuration>" +
				"      </configuration>" +
				"    </component>" +
				"  </components>" +
				"</container>";

			IContainerProfile profile = new XmlProfileBuilder().Build(contents);
			IComponentProfile component = profile.ComponentProfiles[0];

			AssertEquals( Lifestyle.Singleton, component.Lifestyle );
			AssertEquals( Activation.Start, component.Activation );
		}

		[Test]
		public void BuildFromStringWithFacilities()
		{
			String contents = 
				"<container>" + 
				"  <facilities baseAssembly=\"Apache.Avalon.Castle.Windsor.Test, Version=1.0.0.0\">" +
				"    <facility key=\"myfacility\" type=\"Apache.Avalon.Castle.Windsor.Test.Component.Facility.MyFacility\" />" +
				"  </facilities>" +
				"</container>";

			IContainerProfile profile = new XmlProfileBuilder().Build(contents);
			AssertNotNull( profile );
			AssertEquals( 1, profile.Facilities.Length );
			
			IFacilityProfile facility = profile.Facilities[0];
			AssertNotNull( facility );
			AssertEquals( "myfacility", facility.Key );
			AssertEquals( typeof(MyFacility), facility.Implementation );
		}

		[Test]
		public void BuildFromStringWithSubsystems()
		{
			String contents = 
				"<container>" + 
				"  <subsystems baseAssembly=\"Apache.Avalon.Castle.Windsor.Test, Version=1.0.0.0\">" +
				"    <subsystem key=\"configuration\" type=\"Apache.Avalon.Castle.Windsor.Test.Component.Subsystem.MySubSystem\" />" +
				"  </subsystems>" +
				"</container>";

			IContainerProfile profile = new XmlProfileBuilder().Build(contents);
			AssertNotNull( profile );
			AssertEquals( 1, profile.SubSystems.Length );
			
			ISubSystemProfile subsystem = profile.SubSystems[0];
			AssertNotNull( subsystem );
			AssertEquals( "configuration", subsystem.Key );
			AssertEquals( typeof(MySubSystem), subsystem.Implementation );
		}

		[Test]
		public void BuildFromStringWithNestedContainers()
		{
			String contents = 
				"<container>" + 
				"  <components baseAssembly=\"Apache.Avalon.Castle.Windsor.Test, Version=1.0.0.0\">" +
				"    <component key=\"a\" service=\"Apache.Avalon.Castle.Windsor.Test.Component.Simple.IService\" " +
				"               implementation=\"Apache.Avalon.Castle.Windsor.Test.Component.Simple.DefaultService\">  " +
				"      <configuration>" +
				"      </configuration>" +
				"    </component>" +
				"  </components>" +
				"" + 
				"  <container>" +
				"  <components baseAssembly=\"Apache.Avalon.Castle.Windsor.Test, Version=1.0.0.0\">" +
				"      <component key=\"a\" service=\"Apache.Avalon.Castle.Windsor.Test.Component.Simple.IOtherService\" " +
				"                 implementation=\"Apache.Avalon.Castle.Windsor.Test.Component.Simple.DefaultOtherService\">  " +
				"        <configuration>" +
				"        </configuration>" +
				"      </component>" +
				"    </components>" +
				"  </container>" +
				"</container>";

			IContainerProfile profile = new XmlProfileBuilder().Build(contents);

			AssertProfileComponent(profile);

			AssertEquals( 1, profile.SubContainers.Length );

			profile = profile.SubContainers[0];

			AssertProfileComponent( profile, "a", typeof(IOtherService), typeof(DefaultOtherService) );
		}

		private void AssertProfileComponent(IContainerProfile profile)
		{
			AssertProfileComponent( profile, "a", typeof(IService), typeof(DefaultService) );
		}

		private void AssertProfileComponent(IContainerProfile profile, String key, Type service, Type impl)
		{
			AssertNotNull( profile );
			AssertEquals( 1, profile.ComponentProfiles.Length );
			AssertEquals( 0, profile.Facilities.Length );
	
			IComponentProfile component = profile.ComponentProfiles[0];
			AssertNotNull( component );
			AssertEquals( key, component.Key );
			AssertEquals( service, component.Service );
			AssertEquals( impl, component.Implementation );
			AssertEquals( Lifestyle.Undefined, component.Lifestyle );
			AssertEquals( Activation.Undefined, component.Activation );
			AssertNotNull( component.Configuration );
		}
	}
}
