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

namespace Apache.Avalon.Castle.Windsor.Test.InstanceTracker
{
	using System;

	using NUnit.Framework;

	using Apache.Avalon.Castle.MicroKernel;
	using Apache.Avalon.Castle.Windsor.InstanceTracker.Default;

	/// <summary>
	/// Summary description for InstanceHandlerTrackerTestCase.
	/// </summary>
	[TestFixture]
	public class InstanceHandlerTrackerTestCase : Assertion
	{
		[Test]
		public void ResolveAndRelease()
		{
			InstanceHandlerTracker tracker = new InstanceHandlerTracker();

			MockHandler handler = new MockHandler();

			object instance = tracker.Resolve( handler );

			Assert( handler.Resolved );
			AssertNotNull( instance );

			Assert( tracker.IsOwner( instance ) );
			Assert( !tracker.IsOwner( new object() ) );

			tracker.Release( new object() );
			Assert( !handler.Released );

			tracker.Release( instance );
			Assert( handler.Released );
		}

		internal class MockHandler : IHandler
		{
			private object m_instance = new object();
			private bool m_released = false;
			private bool m_resolved = false;

			public bool Released
			{
				get { return m_released; }
			}

			public bool Resolved
			{
				get { return m_resolved; }
			}

			#region IHandler Members

			public void Init(IKernel kernel)
			{
			}

			public void AddChangeStateListener(Apache.Avalon.Castle.MicroKernel.ChangeStateListenerDelegate changeStateDelegate)
			{
			}

			public bool IsOwner(object instance)
			{
				return Object.ReferenceEquals( instance, m_instance );
			}

			public Apache.Avalon.Castle.MicroKernel.Model.IComponentModel ComponentModel
			{
				get
				{
					return null;
				}
			}

			public Apache.Avalon.Castle.MicroKernel.State ActualState
			{
				get
				{
					return new Apache.Avalon.Castle.MicroKernel.State ();
				}
			}

			#endregion

			#region IResolver Members

			public void Release(object instance)
			{
				m_released = true;
			}

			public object Resolve()
			{
				m_resolved = true;
				return m_instance;
			}

			#endregion

			#region IDisposable Members

			public void Dispose()
			{
			}

			#endregion
		}
	}
}
