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

namespace Apache.Avalon.Castle.Windsor
{
	using System;

	using Apache.Avalon.Castle.MicroKernel;
	using Apache.Avalon.Castle.Windsor.InstanceTracker;
	using Apache.Avalon.Castle.Windsor.InstanceTracker.Default;

	/// <summary>
	/// Summary description for WindsorContainer.
	/// </summary>
	public class WindsorContainer : IContainer
	{
		protected IKernel m_kernel;
		protected IContainer m_parent;
		protected IInstanceTracker m_instanceTracker;

		public WindsorContainer()
		{
			m_kernel = new DefaultAvalonKernel();
			m_instanceTracker = new InstanceHandlerTracker();
		}

		public WindsorContainer( IContainer parent ) : this()
		{
			AssertUtil.ArgumentNotNull( parent, "parent" );
			m_parent = parent;
		}

		public WindsorContainer( IInstanceTracker tracker ) : this()
		{
			AssertUtil.ArgumentNotNull( tracker, "tracker" );
			m_instanceTracker = tracker;
		}

		public WindsorContainer( IContainer parent, IInstanceTracker tracker ) : this(parent)
		{
			AssertUtil.ArgumentNotNull( tracker, "tracker" );
			m_instanceTracker = tracker;
		}

		protected IInstanceTracker InstanceTracker
		{
			get { return m_instanceTracker; }
		}

		#region IContainer Members

		public IContainer Parent
		{
			get { return m_parent; }
		}

		public IKernel MicroKernel
		{
			get
			{
				return m_kernel;
			}
		}

		public object Resolve(String key)
		{
			AssertUtil.ArgumentNotNull( key, "key" );

			IHandler handler = MicroKernel[key];

			if ( handler == null )
			{
				if ( Parent != null )
				{
					return Parent.Resolve(key);
				}

				throw new ComponentNotFoundException(key);
			}

			return InstanceTracker.Resolve( handler );
		}

		object IContainer.Resolve(Type service)
		{
			AssertUtil.ArgumentNotNull( service, "service" );
			AssertUtil.ArgumentMustBeInterface( service, "service" );

			IHandler handler = MicroKernel.GetHandlerForService( service );

			if ( handler == null )
			{
				if ( Parent != null )
				{
					return Parent.Resolve(service);
				}

				throw new ComponentNotFoundException(service);
			}

			return InstanceTracker.Resolve( handler );
		}

		public void Release(object instance)
		{
			if (!InstanceTracker.IsOwner( instance ))
			{
				if (Parent != null)
				{
					Parent.Release( instance );
				}
			}
			else
			{
				InstanceTracker.Release( instance );
			}
		}

		#endregion

		#region IDisposable Members

		public void Dispose()
		{
			InstanceTracker.Dispose();
			MicroKernel.Dispose();
		}

		#endregion
	}
}