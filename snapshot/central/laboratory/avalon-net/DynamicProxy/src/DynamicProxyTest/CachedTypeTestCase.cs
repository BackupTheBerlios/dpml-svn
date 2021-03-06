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

namespace Apache.Avalon.DynamicProxy.Test
{
	using System;

	using NUnit.Framework;

	using Apache.Avalon.DynamicProxy.Test.Classes;

	/// <summary>
	/// Summary description for CachedTypeTestCase.
	/// </summary>
	[TestFixture]
	public class CachedTypeTestCase
	{
		private ProxyGenerator m_generator = new ProxyGenerator();

		[Test]
		public void CachedClassProxies()
		{
			object proxy = m_generator.CreateClassProxy( 
				typeof(ServiceClass), new StandardInvocationHandler( new ServiceClass() ) );
			
			Assert.IsNotNull(proxy);
			Assert.IsTrue( typeof(ServiceClass).IsAssignableFrom( proxy.GetType() ) );

			proxy = m_generator.CreateClassProxy( 
				typeof(ServiceClass), new StandardInvocationHandler( new ServiceClass() ) );
			
			Assert.IsNotNull(proxy);
			Assert.IsTrue( typeof(ServiceClass).IsAssignableFrom( proxy.GetType() ) );
		}
	}
}
