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

namespace Apache.Avalon.Castle.Windsor.Builder.Default
{
	using System;

	using Apache.Avalon.Castle.MicroKernel;
	using Apache.Avalon.Castle.Windsor.Profile;
	using Apache.Avalon.Castle.Windsor.Subsystems.Configuration;
	using Apache.Avalon.Castle.Windsor.Facilities.Configuration;
	using Apache.Avalon.Castle.Windsor.InstanceTracker;

	/// <summary>
	/// Summary description for ContainerBuilder.
	/// </summary>
	public class ContainerBuilder : IContainerBuilder
	{
		private IInstanceTracker m_instanceTracker;
		private IContainer m_parentContainer;

		public ContainerBuilder()
		{
		}

		public ContainerBuilder(IInstanceTracker instanceTracker)
		{
			InstanceTracker = instanceTracker;
		}

		public ContainerBuilder(IContainer parentContainer)
		{
			ParentContainer = parentContainer;
		}

		public ContainerBuilder(IContainer parentContainer, IInstanceTracker instanceTracker) : this(parentContainer)
		{
			InstanceTracker = instanceTracker;
		}

		#region IContainerBuilder Members

		public virtual IInstanceTracker InstanceTracker
		{
			get { return m_instanceTracker; }
			set { m_instanceTracker = value; }
		}

		public virtual IContainer ParentContainer
		{
			get { return m_parentContainer; }
			set { m_parentContainer = value; }
		}

		public virtual IContainer Build()
		{
			IContainer container;

			if (ParentContainer != null && InstanceTracker != null)
			{
				container = new WindsorContainer(ParentContainer, InstanceTracker);
			}
			else if (ParentContainer != null)
			{
				container = new WindsorContainer(ParentContainer);
			}
			else if (InstanceTracker != null)
			{
				container = new WindsorContainer(InstanceTracker);
			}
			else
			{
				container = new WindsorContainer();
			}

			return container;
		}

		public virtual IContainer Build(IContainerProfile profile)
		{
			IContainer container = Build();

			SetupSubsystems(profile, container);
			SetupFacilities(profile, container);
			AddFacilities(profile, container);
			AddComponents(profile, container);

			return container;
		}

		#endregion

		protected virtual void SetupSubsystems(IContainerProfile profile, IContainer container)
		{
			IKernelSubsystem subsystem = new ComponentProfileAdapterSubsystem(profile.ComponentProfiles);
			container.MicroKernel.AddSubsystem(KernelConstants.CONFIGURATION, subsystem);
		}

		protected virtual void SetupFacilities(IContainerProfile profile, IContainer container)
		{
			container.MicroKernel.AddFacility("Default", new ConfigurationOverrideFacility(profile.ComponentProfiles));
		}

		protected virtual void AddFacilities(IContainerProfile profile, IContainer container)
		{
			foreach(IFacilityProfile facility in profile.Facilities)
			{
				IKernelFacility instance = Activator.CreateInstance(facility.Implementation) as IKernelFacility;
				container.MicroKernel.AddFacility(facility.Key, instance);
			}
		}

		protected virtual void AddComponents(IContainerProfile profile, IContainer container)
		{
			foreach(IComponentProfile component in profile.ComponentProfiles)
			{
				container.MicroKernel.AddComponent(component.Key, component.Service, component.Implementation);
			}
		}
	}
}