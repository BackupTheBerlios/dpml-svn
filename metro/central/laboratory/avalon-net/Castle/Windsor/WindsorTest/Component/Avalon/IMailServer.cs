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

namespace Apache.Avalon.Castle.Windsor.Test.Component.Avalon
{
	using System;

	/// <summary>
	/// Summary description for IMailServer.
	/// </summary>
	public interface IMailServer
	{
		int Port { get; set; }

		String Server { get; set; }

		void Send(String contents);
	}
}
