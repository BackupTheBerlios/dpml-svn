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

	using Apache.Avalon.Framework;

	/// <summary>
	/// Summary description for MailServer.
	/// </summary>
	public class MailServer : IMailServer, IConfigurable
	{
		private int m_port = 1;
		private String m_server = "localhost";

		public MailServer()
		{
		}

		#region IMailServer Members

		public int Port { get { return m_port; }  set { m_port = value; } }

		public String Server { get { return m_server; } set { m_server = value; } }

		public void Send(String contents)
		{
		}

		#endregion

		#region IConfigurable Members

		public void Configure(IConfiguration config)
		{
			Port = (int) config.GetChild("port", true).GetValue( typeof(int), 2 );
			Server = (String) config.GetChild("server", true).GetValue( typeof(String), "nonlocalhost" );
		}

		#endregion
	}
}
