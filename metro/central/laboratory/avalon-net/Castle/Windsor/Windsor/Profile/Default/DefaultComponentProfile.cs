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

namespace Apache.Avalon.Castle.Windsor.Profile.Default
{
	using System;

	using Apache.Avalon.Framework;

	/// <summary>
	/// Summary description for DefaultComponentProfile.
	/// </summary>
	public class DefaultComponentProfile : IComponentProfile
	{
		private String m_key;
		private Type m_service;
		private Type m_implementation;
		private Lifestyle m_lifestyle;
		private Activation m_activation;
		private IConfiguration m_configuration;

		public DefaultComponentProfile( String key, Type service, Type implementation,
			Lifestyle lifestyle, Activation activation, IConfiguration configuration )
		{
			m_key = key;
			m_service = service;
			m_implementation = implementation;
			m_lifestyle = lifestyle;
			m_activation = activation;
			m_configuration = configuration;
		}

		#region IComponentProfile Members

		public String Key
		{
			get { return m_key; }
		}

		public Type Service
		{
			get { return m_service; }
		}

		public Type Implementation
		{
			get { return m_implementation; }
		}

		public Lifestyle Lifestyle
		{
			get { return m_lifestyle; }
		}

		public Activation Activation
		{
			get { return m_activation; }
		}

		public IConfiguration Configuration
		{
			get { return m_configuration; }
		}

		#endregion
	}
}