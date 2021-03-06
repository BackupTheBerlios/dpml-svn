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

namespace Apache.Avalon.Castle.MicroKernel.Model
{
	using System;
	using System.Reflection;

	/// <summary>
	/// Holds the information to allow the container to
	/// correctly instantiate the component implementation.
	/// </summary>
	public interface IConstructionModel
	{
		/// <summary>
		/// Implementation type
		/// </summary>
        Type Implementation { get; set; }

        /// <summary>
		/// The best constructor selected.
		/// </summary>
        ConstructorInfo SelectedConstructor { get; set; }

        /// <summary>
		/// Properties that will be used to satisfy dependencies.
		/// </summary>
		PropertyInfo[] SelectedProperties { get; }
	}
}