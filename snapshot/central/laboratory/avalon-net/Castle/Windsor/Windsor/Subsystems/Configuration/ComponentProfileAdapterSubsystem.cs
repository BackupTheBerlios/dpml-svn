using Apache.Avalon.Castle.Windsor.Profile;
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

namespace Apache.Avalon.Castle.Windsor.Subsystems.Configuration
{
	using System;

	using Apache.Avalon.Framework;
	using Apache.Avalon.Castle.MicroKernel.Subsystems.Configuration.Default;

	/// <summary>
	/// Summary description for ComponentProfileAdapterSubsystem.
	/// </summary>
	public class ComponentProfileAdapterSubsystem : DefaultConfigurationManager
	{
		public ComponentProfileAdapterSubsystem( IComponentProfile[] profiles ) : base()
		{
			foreach( IComponentProfile component in profiles )
			{
				ConfigurationDictionary[ component.Key ] = component.Configuration;
			}
		}
	}
}
