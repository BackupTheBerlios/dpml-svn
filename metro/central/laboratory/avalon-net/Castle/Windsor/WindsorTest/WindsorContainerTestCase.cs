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

	using Apache.Avalon.Castle.Windsor.Test.Component.Simple;

	/// <summary>
	/// Summary description for WindsorContainerTestCase.
	/// </summary>
	[TestFixture]
	public class WindsorContainerTestCase : Assertion
	{
		[Test]
		public void ContainerCreation()
		{
			using (IContainer container = new WindsorContainer())
			{
				AssertNotNull(container);
				AssertNotNull(container.MicroKernel);
				AssertNull(container.Parent);
			}
		}

		[Test]
		public void ContainerCreationWithParent()
		{
			IContainer parent = new WindsorContainer();
			IContainer child = new WindsorContainer(parent);
			AssertNull(parent.Parent);
			AssertNotNull(child.Parent);
			child.Dispose();
			parent.Dispose();
		}

		[Test]
		public void ResolveUnregisterdComponent()
		{
			using (IContainer container = new WindsorContainer())
			{
				try
				{
					object component = container.Resolve("a");
					Fail("This component wasn't registered, how can it be found?");
				}
				catch(ComponentNotFoundException)
				{
					// Expected
				}
			}
		}

		[Test]
		public void ResolveUnregisterdComponentByService()
		{
			using (IContainer container = new WindsorContainer())
			{
				try
				{
					object component = container.Resolve( typeof(IService) );
					Fail("This component wasn't registered, how can it be found?");
				}
				catch(ComponentNotFoundException)
				{
					// Expected
				}
			}
		}

		[Test]
		public void AddAndResolve()
		{
			IContainer container = new WindsorContainer();
			container.MicroKernel.AddComponent("a", typeof (IService), typeof (DefaultService));

			object component = container.Resolve("a");
			AssertNotNull(component);
			AssertNotNull(component as IService);

			container.Release(component);
			container.Dispose();
		}

		[Test]
		public void AddAndResolveByService()
		{
			IContainer container = new WindsorContainer();
			container.MicroKernel.AddComponent("a", typeof (IService), typeof (DefaultService));

			object component = container.Resolve(typeof (IService));
			AssertNotNull(component);
			AssertNotNull(component as IService);

			container.Release(component);
			container.Dispose();
		}

		[Test]
		public void AddAndResolveWithNestedContainer()
		{
			IContainer parent = new WindsorContainer();
			IContainer child = new WindsorContainer(parent);
			parent.MicroKernel.AddComponent("a", typeof (IService), typeof (DefaultService));

			object component = child.Resolve("a");
			AssertNotNull(component);
			AssertNotNull(component as IService);

			child.Release(component);
			child.Dispose();
			parent.Dispose();
		}

		[Test]
		public void AddAndResolveWithNestedContainerByService()
		{
			IContainer parent = new WindsorContainer();
			IContainer child = new WindsorContainer(parent);
			parent.MicroKernel.AddComponent("a", typeof (IService), typeof (DefaultService));

			object component = child.Resolve(typeof (IService));
			AssertNotNull(component);
			AssertNotNull(component as IService);

			child.Release(component);
			child.Dispose();
			parent.Dispose();
		}
	}
}