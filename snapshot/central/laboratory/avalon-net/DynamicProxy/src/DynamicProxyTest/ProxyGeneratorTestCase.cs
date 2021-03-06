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
	using System.Reflection;

	using NUnit.Framework;

	using Apache.Avalon.DynamicProxy;
	using Apache.Avalon.DynamicProxy.Test.Classes;
	using Apache.Avalon.DynamicProxy.Test.ClassInterfaces;

	/// <summary>
	/// Summary description for ProxyGeneratorTestCase.
	/// </summary>
	[TestFixture]
	public class ProxyGeneratorTestCase
	{
		private ProxyGenerator m_generator;

		[SetUp]
		public void Init()
		{
			m_generator = new ProxyGenerator();
		}

		[Test]
		public void ProxyForClass()
		{
			object proxy = m_generator.CreateClassProxy( 
				typeof(ServiceClass), new ResultModifiedInvocationHandler( new ServiceClass() ) );
			
			Assert.IsNotNull( proxy );
			Assert.IsTrue( typeof(ServiceClass).IsAssignableFrom( proxy.GetType() ) );

			ServiceClass inter = (ServiceClass) proxy;

			Assert.AreEqual( 44, inter.Sum( 20, 25 ) );
			Assert.AreEqual( true, inter.Valid );
		}

		[Test]
		public void ProxyForClassWithSuperClass()
		{
			object proxy = m_generator.CreateClassProxy( 
				typeof(SpecializedServiceClass), new ResultModifiedInvocationHandler( new SpecializedServiceClass() ) );
			
			Assert.IsNotNull( proxy );
			Assert.IsTrue( typeof(ServiceClass).IsAssignableFrom( proxy.GetType() ) );
			Assert.IsTrue( typeof(SpecializedServiceClass).IsAssignableFrom( proxy.GetType() ) );

			SpecializedServiceClass inter = (SpecializedServiceClass) proxy;

			Assert.AreEqual( 44, inter.Sum( 20, 25 ) );
			Assert.AreEqual( -6, inter.Subtract( 20, 25 ) );
			Assert.AreEqual( true, inter.Valid );
		}

		[Test]
		public void ProxyForClassWhichImplementsInterfaces()
		{
			object proxy = m_generator.CreateClassProxy( 
				typeof(MyInterfaceImpl), new ResultModifiedInvocationHandler( new MyInterfaceImpl() ) );
			
			Assert.IsNotNull( proxy );
			Assert.IsTrue( typeof(MyInterfaceImpl).IsAssignableFrom( proxy.GetType() ) );
			Assert.IsTrue( typeof(IMyInterface).IsAssignableFrom( proxy.GetType() ) );

			IMyInterface inter = (IMyInterface) proxy;

			Assert.AreEqual( 44, inter.Calc( 20, 25 ) );
		}

		[Test]
		public void ProxyingClassWithoutVirtualMethods()
		{
			object proxy = m_generator.CreateClassProxy( 
				typeof(NoVirtualMethodClass), new ResultModifiedInvocationHandler( new SpecializedServiceClass() ) );
			
			Assert.IsNotNull( proxy );
			Assert.IsTrue( typeof(NoVirtualMethodClass).IsAssignableFrom( proxy.GetType() ) );

			NoVirtualMethodClass inter = (NoVirtualMethodClass) proxy;

			Assert.AreEqual( 45, inter.Sum( 20, 25 ) );
		}

		[Test]
		public void ProxyingClassWithSealedMethods()
		{
			object proxy = m_generator.CreateClassProxy( 
				typeof(SealedMethodsClass), new ResultModifiedInvocationHandler( new SpecializedServiceClass() ) );
			
			Assert.IsNotNull( proxy );
			Assert.IsTrue( typeof(SealedMethodsClass).IsAssignableFrom( proxy.GetType() ) );

			SealedMethodsClass inter = (SealedMethodsClass) proxy;

			Assert.AreEqual( 45, inter.Sum( 20, 25 ) );
		}

		[Test]
		public void CreateClassProxyInvalidArguments()
		{
			try
			{
				m_generator.CreateClassProxy( 
					typeof(ICloneable), new StandardInvocationHandler( new SpecializedServiceClass() ) );
			}
			catch(ArgumentException)
			{
				// Expected
			}

			try
			{
				m_generator.CreateClassProxy( 
					null, new StandardInvocationHandler( new SpecializedServiceClass() ) );
			}
			catch(ArgumentNullException)
			{
				// Expected
			}

			try
			{
				m_generator.CreateClassProxy( 
					typeof(SpecializedServiceClass), null );
			}
			catch(ArgumentNullException)
			{
				// Expected
			}
		}

		[Test]
		public void TestGenerationSimpleInterface()
		{
			object proxy = m_generator.CreateProxy( 
				typeof(IMyInterface), new StandardInvocationHandler( new MyInterfaceImpl() ) );
			
			Assert.IsNotNull( proxy );
			Assert.IsTrue( typeof(IMyInterface).IsAssignableFrom( proxy.GetType() ) );

			IMyInterface inter = (IMyInterface) proxy;

			Assert.AreEqual( 45, inter.Calc( 20, 25 ) );

			inter.Name = "opa";
			Assert.AreEqual( "opa", inter.Name );

			inter.Started = true;
			Assert.AreEqual( true, inter.Started );
		}

		[Test]
		public void TestGenerationWithInterfaceHeritage()
		{
			object proxy = m_generator.CreateProxy( 
				typeof(IMySecondInterface), new StandardInvocationHandler( new MySecondInterfaceImpl() ) );

			Assert.IsNotNull( proxy );
			Assert.IsTrue( typeof(IMyInterface).IsAssignableFrom( proxy.GetType() ) );
			Assert.IsTrue( typeof(IMySecondInterface).IsAssignableFrom( proxy.GetType() ) );

			IMySecondInterface inter = (IMySecondInterface) proxy;
			inter.Calc(1, 1);

			inter.Name = "hammett";
			Assert.AreEqual( "hammett", inter.Name );

			inter.Address = "pereira leite, 44";
			Assert.AreEqual( "pereira leite, 44", inter.Address );
			
			Assert.AreEqual( 45, inter.Calc( 20, 25 ) );
		}

		[Test]
		public void TestEnumProperties()
		{
			ServiceStatusImpl service = new ServiceStatusImpl();

			object proxy = m_generator.CreateProxy( 
				typeof(IServiceStatus), new StandardInvocationHandler( service ) );
			
			Assert.IsNotNull( proxy );
			Assert.IsTrue( typeof(IServiceStatus).IsAssignableFrom( proxy.GetType() ) );

			IServiceStatus inter = (IServiceStatus) proxy;
			Assert.AreEqual( State.Invalid, inter.ActualState );
			
			inter.ChangeState( State.Valid );
			Assert.AreEqual( State.Valid, inter.ActualState );
		}

		public class MyInterfaceProxy : IInvocationHandler
		{
			#region IInvocationHandler Members

			public object Invoke(object proxy, MethodInfo method, params object[] arguments)
			{
				return null;
			}

			#endregion
		}
	}

	public class ResultModifiedInvocationHandler : StandardInvocationHandler
	{
		public ResultModifiedInvocationHandler( object instanceDelegate ) : base(instanceDelegate)
		{
		}

		protected override void PostInvoke(object proxy, System.Reflection.MethodInfo method, ref object returnValue, params object[] arguments)
		{
			if ( returnValue != null && returnValue.GetType() == typeof(int))
			{
				int value = (int) returnValue;
				returnValue = --value;
			}
		}
	}
}


