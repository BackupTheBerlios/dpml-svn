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

namespace Apache.Avalon.Castle.Windsor.Facilities.Configuration
{
	using System;
	using System.Collections;

	using Apache.Avalon.Framework;
	using Apache.Avalon.Castle.MicroKernel;
	using Apache.Avalon.Castle.MicroKernel.Model;
	using Apache.Avalon.Castle.Windsor.Profile;

	/// <summary>
	/// Summary description for ConfigurationOverrideFacility.
	/// </summary>
	public class ConfigurationOverrideFacility : IKernelFacility
	{
		private IDictionary	m_key2override = new Hashtable();

		public ConfigurationOverrideFacility( IComponentProfile[] profiles )
		{
			foreach( IComponentProfile component in profiles )
			{
				if ( HasOverride( component ) )
				{
					ComponentDictionary[ component.Key ] = new ComponentModelOverride( component );
				}
			}
		}

		protected virtual bool HasOverride( IComponentProfile component )
		{
			return component.Lifestyle != Lifestyle.Undefined || 
				component.Activation != Activation.Undefined;
		}

		protected virtual IDictionary ComponentDictionary
		{
			get
			{
				return m_key2override;
			}
		}

		#region IKernelFacility Members

		/// <summary>
		/// Init the facility given it a chance to subscribe
		/// to kernel events.
		/// </summary>
		/// <param name="kernel">Kernel instance</param>
		public void Init(IKernel kernel)
		{
			kernel.ComponentModelConstructed += new ComponentModelDelegate(ComponentModelConstructed);
		}

		/// <summary>
		/// Gives a chance to the facility to unsubscribe from the
		/// events and do its proper clean up.
		/// </summary>
		public void Terminate(IKernel kernel)
		{
			kernel.ComponentModelConstructed -= new ComponentModelDelegate(ComponentModelConstructed);
		}

		#endregion

		private void ComponentModelConstructed(IComponentModel model, String key)
		{
			ComponentModelOverride data = ComponentDictionary[ key ] as ComponentModelOverride;

			if (data != null)
			{
				model.ActivationPolicy = data.Activation != Activation.Undefined ? data.Activation : model.ActivationPolicy;
				model.SupportedLifestyle = data.Lifestyle != Lifestyle.Undefined ? data.Lifestyle : model.SupportedLifestyle;
			}
		}

		internal class ComponentModelOverride
		{
			private Lifestyle m_lifestyle;
			private Activation m_activation;

			public ComponentModelOverride( IComponentProfile componentProfile )
			{
				m_lifestyle = componentProfile.Lifestyle;
				m_activation = componentProfile.Activation;
			}

			public Lifestyle Lifestyle
			{
				get { return m_lifestyle; }
			}

			public Activation Activation
			{
				get { return m_activation; }
			}
		}
	}
}
