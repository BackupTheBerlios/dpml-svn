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
	/// <summary>
	/// Summary description for DefaultContainerProfile.
	/// </summary>
	public class DefaultContainerProfile : IContainerProfile
	{
		private IFacilityProfile[] m_facilities;
		private ISubSystemProfile[] m_subsystems;
		private IComponentProfile[] m_components;
		private IContainerProfile[] m_subContainers;

		public DefaultContainerProfile(IFacilityProfile[] facilities, ISubSystemProfile[] subsystems, IComponentProfile[] components, IContainerProfile[] subContainers)
		{
			m_facilities = facilities;
			m_subsystems = subsystems;
			m_components = components;
			m_subContainers = subContainers;
		}

		#region IContainerProfile Members

		public IFacilityProfile[] Facilities
		{
			get { return m_facilities; }
		}

		public ISubSystemProfile[] SubSystems
		{
			get { return m_subsystems; }
		}

		public IComponentProfile[] ComponentProfiles
		{
			get { return m_components; }
		}

		public IContainerProfile[] SubContainers
		{
			get { return m_subContainers; }
		}

		#endregion
	}
}