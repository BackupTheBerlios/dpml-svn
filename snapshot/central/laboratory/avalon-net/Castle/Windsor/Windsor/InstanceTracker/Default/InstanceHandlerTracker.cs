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

namespace Apache.Avalon.Castle.Windsor.InstanceTracker.Default
{
	using System;
	using System.Collections;
	using System.Collections.Specialized;

	using Apache.Avalon.Castle.MicroKernel;

	/// <summary>
	/// Summary description for InstanceHandlerTracker.
	/// </summary>
	public class InstanceHandlerTracker : IInstanceTracker
	{
		private IDictionary m_instance2Handler = new HybridDictionary();

		#region IInstanceTracker Members

		public virtual object Resolve(IHandler handler)
		{
			object instance = handler.Resolve();

			m_instance2Handler[instance] = handler;
			
			return instance;
		}

		public virtual bool IsOwner(object instance)
		{
			return m_instance2Handler.Contains(instance);
		}

		public virtual void Release(object instance)
		{
			IHandler handler = (IHandler) m_instance2Handler[instance];
			
			if (handler != null)
			{
				m_instance2Handler.Remove( instance );
				handler.Release( instance );
			}
		}

		#endregion

		#region IDisposable Members

		public void Dispose()
		{
			m_instance2Handler.Clear();
		}

		#endregion
	}
}
