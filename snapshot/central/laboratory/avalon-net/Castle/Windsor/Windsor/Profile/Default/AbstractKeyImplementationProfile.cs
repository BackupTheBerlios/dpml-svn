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

	/// <summary>
	/// Summary description for AbstractKeyImplementationProfile.
	/// </summary>
	public abstract class AbstractKeyImplementationProfile
	{
		private String m_key;
		private Type m_type;

		public AbstractKeyImplementationProfile(String key, Type type)
		{
			m_key = key;
			m_type = type;
		}

		public virtual String Key
		{
			get { return m_key; }
		}

		public virtual Type Implementation
		{
			get { return m_type; }
		}
	}
}
