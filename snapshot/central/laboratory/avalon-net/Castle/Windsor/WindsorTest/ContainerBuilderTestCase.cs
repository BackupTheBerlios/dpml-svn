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

namespace Apache.Avalon.Castle.Windsor.Test
{
	using System;

	using NUnit.Framework;

	using Apache.Avalon.Castle.Windsor.Builder.Default;
	using Apache.Avalon.Castle.Windsor.InstanceTracker.Default;
	using Apache.Avalon.Castle.Windsor.Profile;
	using Apache.Avalon.Castle.Windsor.Profile.Builders.Xml;
	using Apache.Avalon.Castle.Windsor.Test.Component.Avalon;
	using Apache.Avalon.Castle.Windsor.Test.Component.Simple;

	/// <summary>
	/// Summary description for ContainerBuilderTestCase.
	/// </summary>
	[TestFixture]
	public class ContainerBuilderTestCase : Assertion
	{
		[Test]
		public void Build()
		{
			ContainerBuilder builder = new ContainerBuilder();
			IContainer container = builder.Build();

			AssertNotNull(container);
			AssertNotNull(container.MicroKernel);
			AssertNull(container.Parent);
		}

		[Test]
		public void BuildWithInstanceTracker()
		{
			ContainerBuilder builder = new ContainerBuilder( new InstanceHandlerTracker() );
			IContainer container = builder.Build();

			AssertNotNull(container);
			AssertNotNull(container.MicroKernel);
			AssertNull(container.Parent);
		}

		[Test]
		public void BuildWithParentContainer()
		{
			ContainerBuilder builder = new ContainerBuilder( new WindsorContainer() );
			IContainer container = builder.Build();

			AssertNotNull(container);
			AssertNotNull(container.MicroKernel);
			AssertNotNull(container.Parent);
		}

		[Test]
		public void BuildWithCustomProfileAndDefaultComponentSettings()
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
			ContainerBuilder builder = new ContainerBuilder();
			
			IContainer container = builder.Build( profile );

			IService service1 = (IService) container.Resolve( typeof(IService) );
			IService service2 = (IService) container.Resolve( typeof(IService) );
			
			AssertNotNull( service1 );
			AssertNotNull( service2 );
			Assert( !Object.ReferenceEquals(service1, service2) );
		}

		[Test]
		public void BuildWithCustomProfileAndOverridedComponentSettings()
		{
			String contents = 
				"<container>" + 
				"  <components baseAssembly=\"Apache.Avalon.Castle.Windsor.Test, Version=1.0.0.0\">" +
				"    <component lifestyle=\"singleton\" key=\"a\" service=\"Apache.Avalon.Castle.Windsor.Test.Component.Simple.IService\" " +
				"               implementation=\"Apache.Avalon.Castle.Windsor.Test.Component.Simple.DefaultService\">  " +
				"      <configuration>" +
				"      </configuration>" +
				"    </component>" +
				"  </components>" +
				"</container>";

			IContainerProfile profile = new XmlProfileBuilder().Build(contents);
			ContainerBuilder builder = new ContainerBuilder();
			
			IContainer container = builder.Build( profile );

			IService service1 = (IService) container.Resolve( typeof(IService) );
			IService service2 = (IService) container.Resolve( typeof(IService) );
			
			AssertNotNull( service1 );
			AssertNotNull( service2 );
			Assert( Object.ReferenceEquals(service1, service2) );
		}

		[Test]
		public void BuildWithCustomProfileAndComponentConfiguration()
		{
			String contents = 
				"<container>" + 
				"  <components baseAssembly=\"Apache.Avalon.Castle.Windsor.Test, Version=1.0.0.0\">" +
				"    <component lifestyle=\"singleton\" key=\"a\" service=\"Apache.Avalon.Castle.Windsor.Test.Component.Avalon.IMailServer\" " +
				"               implementation=\"Apache.Avalon.Castle.Windsor.Test.Component.Avalon.MailServer\">  " +
				"      <configuration>" +
				"        <port>1001</port>" +
				"        <server>mailserver</server>" +
				"      </configuration>" +
				"    </component>" +
				"  </components>" +
				"</container>";

			IContainerProfile profile = new XmlProfileBuilder().Build(contents);
			ContainerBuilder builder = new ContainerBuilder();
			
			IContainer container = builder.Build( profile );

			IMailServer mailServer = (IMailServer) container.Resolve( typeof(IMailServer) );
			AssertNotNull( mailServer );
			AssertEquals( 1001, mailServer.Port );
			AssertEquals( "mailserver", mailServer.Server );
		}

		[Test]
		public void BuildWithCustomProfileAndAvalonComponentConfiguration()
		{
			String contents = 
				"<container>" + 
				"  <components baseAssembly=\"Apache.Avalon.Castle.Windsor.Test, Version=1.0.0.0\">" +
				"    <component lifestyle=\"singleton\" key=\"mail\" service=\"Apache.Avalon.Castle.Windsor.Test.Component.Avalon.IMailServer\" " +
				"               implementation=\"Apache.Avalon.Castle.Windsor.Test.Component.Avalon.MailServer2\">  " +
				"      <configuration>" +
				"        <port>1001</port>" +
				"        <server>mailserver</server>" +
				"      </configuration>" +
				"    </component>" +
				"  </components>" +
				"</container>";

			IContainerProfile profile = new XmlProfileBuilder().Build(contents);
			ContainerBuilder builder = new ContainerBuilder();
			
			IContainer container = builder.Build( profile );

			IMailServer mailServer = (IMailServer) container.Resolve( typeof(IMailServer) );
			AssertNotNull( mailServer );
			AssertEquals( 1001, mailServer.Port );
			AssertEquals( "mailserver", mailServer.Server );
		}
	}
}
