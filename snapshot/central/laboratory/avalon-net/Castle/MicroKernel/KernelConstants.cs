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

namespace Apache.Avalon.Castle.MicroKernel
{
	using System;

	/// <summary>
	/// Holds general Subsystem's names.
	/// </summary>
	public abstract class KernelConstants
	{
		/// <summary>
		/// Identifies the ConfigurationManager subsystem.
		/// </summary>
		public static readonly String CONFIGURATION = "configuration";

		/// <summary>
		/// Identifies the LoggerManager subsystem.
		/// </summary>
		public static readonly String LOGGER = "logger";

		/// <summary>
		/// Identifies the ContextManager subsystem.
		/// </summary>
		public static readonly String CONTEXT = "context";

		/// <summary>
		/// Identifies the LookupCriteriaMatcher subsystem.
		/// </summary>
		public static readonly String LOOKUP = "lookup";

		/// <summary>
		/// Identifies the EventManager subsystem.
		/// </summary>
		public static readonly String EVENTS = "events";
	}
}
