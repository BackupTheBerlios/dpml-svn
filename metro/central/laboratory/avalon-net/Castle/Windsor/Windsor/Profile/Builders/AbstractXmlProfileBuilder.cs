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

namespace Apache.Avalon.Castle.Windsor.Profile.Builders
{
	using System;
	using System.Xml;

	/// <summary>
	/// Summary description for AbstractXmlProfileBuilder.
	/// </summary>
	public abstract class AbstractXmlProfileBuilder
	{
		protected static readonly String BASE_ASSEMBLY_ATT_NAME = "baseAssembly";
		protected static readonly String CONTAINER_NODE_NAME = "container";
		
		public AbstractXmlProfileBuilder()
		{
		}

		protected virtual String ObtainNonNullAttributeValue(XmlNode node, String attrKey)
		{
			String value = ObtainAttributeValue(node, attrKey);

			if (value == null)
			{
				throw new ProfileException(String.Format("Attribute {0} for node {1} must be specified", node.Name, attrKey));
			}

			return value;
		}

		protected virtual String ObtainAttributeValue(XmlNode node, String attrKey)
		{
			String value = null;

			XmlAttribute att = node.Attributes[attrKey];

			if (att != null)
			{
				value = att.Value;
			}

			return value;
		}

		protected virtual object ObtainEnumValue(XmlNode node, String attrKey, Type enumType, Enum defaultValue)
		{
			object value = defaultValue;

			XmlAttribute assemblyAttribute = node.Attributes[attrKey];

			if (assemblyAttribute != null)
			{
				try
				{
					value = Enum.Parse(enumType, assemblyAttribute.Value, true);
				}
				catch(Exception)
				{
					throw new ProfileException(String.Format("Could not convert " +
						"key {0} to enum {1}", attrKey, enumType.ToString()));
				}
			}

			return value;
		}

		protected virtual String ObtainDefaultAssemblyFromNode(XmlNode node)
		{
			return ObtainAttributeValue(node, BASE_ASSEMBLY_ATT_NAME);
		}

		/// <summary>
		/// Quite flawed assumption to check if we are dealing with a 
		/// full type name, or just a package.Type name.
		/// </summary>
		/// <param name="type"></param>
		/// <param name="assemblyName"></param>
		/// <returns></returns>
		protected virtual Type GetType(String type, String assemblyName)
		{
			int index = type.IndexOf(",");

			if (index != -1)
			{
				return GetType(type);
			}
			else if (assemblyName != null)
			{
				// Try to build a name

				String newTypeName = String.Format("{0}, {1}", type, assemblyName);
				return GetType(newTypeName);
			}
			else
			{
				throw new ProfileException(String.Format("Could not resolve Type {0} Assembly Name {1}", type, assemblyName));
			}
		}

		/// <summary>
		/// 
		/// </summary>
		/// <param name="fullTypeName"></param>
		/// <returns></returns>
		protected virtual Type GetType(String fullTypeName)
		{
			try
			{
				return Type.GetType(fullTypeName, true, true);
			}
			catch(Exception ex)
			{
				throw new ProfileException(String.Format("Could not resolve Type {0}", fullTypeName), ex);
			}
		}
	}
}
