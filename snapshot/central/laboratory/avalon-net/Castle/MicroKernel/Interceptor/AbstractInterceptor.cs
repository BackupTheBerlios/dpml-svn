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

namespace Apache.Avalon.Castle.MicroKernel.Interceptor
{
	/// <summary>
	/// Abstract implementation for <see cref="IInterceptor"/>
	/// which implements the Next property. Implementors should override
	/// the Process method and invoke the base class to invoke the next
	/// interceptor in the chain.
	/// </summary>
	public abstract class AbstractInterceptor : IInterceptor
	{
		private IInterceptor m_next;

		public AbstractInterceptor()
		{
		}

		#region IInterceptor Members

		/// <summary>
		/// Invokes the Next interceptor in the chain
		/// </summary>
		/// <param name="instance"></param>
		/// <param name="method"></param>
		/// <param name="arguments"></param>
		/// <returns></returns>
		public virtual object Process(object instance, System.Reflection.MethodInfo method, params object[] arguments)
		{
			return Next.Process( instance, method, arguments );
		}

		/// <summary>
		/// Get and set the next interceptor in the chain
		/// </summary>
		public IInterceptor Next
		{
			get
			{
				return m_next;
			}
			set
			{
				m_next = value;
			}
		}

		#endregion
	}
}
