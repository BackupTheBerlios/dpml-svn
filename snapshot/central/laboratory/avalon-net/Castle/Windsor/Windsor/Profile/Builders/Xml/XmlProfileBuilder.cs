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

namespace Apache.Avalon.Castle.Windsor.Profile.Builders.Xml
{
	using System;
	using System.Xml;
	using System.Collections;

	using Apache.Avalon.Framework;
	using Apache.Avalon.Castle.Windsor.Profile.Default;

	/// <summary>
	/// Process a xml content to create the according Profile representations.
	/// </summary>
	/// <example>
	/// Follows a sample node in XML:
	/// <code>
	/// <![CDATA[
	/// &lt;container&gt;
	///   &lt;facilities baseAssembly=\"optional name of assembly\"&gt;
	///     &lt;facility key="key" type="type full name" /&gt;
	///   &lt;/facilities&gt;
	///   
	///   &lt;subsystems baseAssembly=\"optional name of assembly\"&gt;
	///     &lt;subsystem key="key" type="type full name" /&gt;
	///   &lt;/subsystems&gt;
	///   
	///   &lt;components baseAssembly=\"optional name of assembly\"&gt;
	///     &lt;component key="key" service="type full name" implementation="type full name"
	///       lifestyle="optional override" activation="optional override" &gt;
	///       &lt;configuration&gt; Any component configuration &lt;/configuration&gt;
	///     &lt;/component&gt;
	///   &lt;/components&gt;
	///   
	/// &lt;/container&gt;
	/// ]]>
	/// </code>
	/// </example>
	public class XmlProfileBuilder : AbstractXmlProfileBuilder
	{
		protected static readonly string SUBSYSTEMS_NODE_NAME = "subsystems";
		protected static readonly string FACILITIES_NODE_NAME = "facilities";
		protected static readonly string COMPONENTS_NODE_NAME = "components";

		/// <summary>
		/// Constructs an <see cref="IContainerProfile"/>
		/// from a XML fragment.
		/// </summary>
		/// <param name="xmlContents"></param>
		/// <returns></returns>
		public virtual IContainerProfile Build(String xmlContents)
		{
			XmlNamespaceManager nsManager = new XmlNamespaceManager(new NameTable());
			XmlParserContext context = new XmlParserContext(null, nsManager, null, XmlSpace.None);
			XmlReader reader = new XmlTextReader(xmlContents, XmlNodeType.Element, context);
			return Build(reader);
		}

		/// <summary>
		/// Constructs an <see cref="IContainerProfile"/>
		/// using a <see cref="XmlReader"/>
		/// </summary>
		/// <param name="reader"></param>
		/// <returns></returns>
		public virtual IContainerProfile Build(XmlReader reader)
		{
			XmlDocument doc = new XmlDocument();
			doc.Load(reader);
			XmlNode containerNode = doc.DocumentElement.SelectSingleNode("/" + CONTAINER_NODE_NAME);
			return CreateContainerProfile(containerNode);
		}

		/// <summary>
		/// Constructs an <see cref="IContainerProfile"/>
		/// from a <see cref="XmlNode"/>
		/// </summary>
		/// <param name="containerNode"></param>
		/// <returns></returns>
		protected virtual IContainerProfile CreateContainerProfile(XmlNode containerNode)
		{
			ISubSystemProfile[] subsystems = ProcessSubSystems(containerNode.SelectSingleNode(SUBSYSTEMS_NODE_NAME));
			IFacilityProfile[] facilities = ProcessFacilities(containerNode.SelectSingleNode(FACILITIES_NODE_NAME));
			IComponentProfile[] components = ProcessComponents(containerNode.SelectSingleNode(COMPONENTS_NODE_NAME));
			IContainerProfile[] subContainers = ProcessSubContainers(containerNode.SelectNodes(CONTAINER_NODE_NAME));

			return new DefaultContainerProfile(facilities, subsystems, components, subContainers);
		}

		/// <summary>
		/// Process nested 'container' elements in the Xml.
		/// </summary>
		/// <param name="containerNodeList"></param>
		/// <returns></returns>
		protected virtual IContainerProfile[] ProcessSubContainers(XmlNodeList containerNodeList)
		{
			ArrayList containers = new ArrayList();

			foreach(XmlNode node in containerNodeList)
			{
				containers.Add(CreateContainerProfile(node));
			}

			return (IContainerProfile[]) containers.ToArray(typeof (IContainerProfile));
		}

		/// <summary>
		/// Process the elements in the 'subsystems' node.
		/// </summary>
		/// <param name="subsystemsNode"></param>
		/// <returns></returns>
		protected virtual ISubSystemProfile[] ProcessSubSystems(XmlNode subsystemsNode)
		{
			if (subsystemsNode == null) return new ISubSystemProfile[0];

			String assemblyName = ObtainDefaultAssemblyFromNode(subsystemsNode);
			ArrayList subsystems = new ArrayList();

			foreach(XmlNode node in subsystemsNode)
			{
				subsystems.Add(CreateSubSystemProfile(node, assemblyName));
			}

			return (ISubSystemProfile[]) subsystems.ToArray(typeof (ISubSystemProfile));
		}

		/// <summary>
		/// Process the elements in the 'facilities' node.
		/// </summary>
		/// <param name="facilitiesNode"></param>
		/// <returns></returns>
		protected virtual IFacilityProfile[] ProcessFacilities(XmlNode facilitiesNode)
		{
			if (facilitiesNode == null) return new IFacilityProfile[0];

			String assemblyName = ObtainDefaultAssemblyFromNode(facilitiesNode);
			ArrayList facilities = new ArrayList();

			foreach(XmlNode node in facilitiesNode)
			{
				facilities.Add(CreateFacilityProfile(node, assemblyName));
			}

			return (IFacilityProfile[]) facilities.ToArray(typeof (IFacilityProfile));
		}

		/// <summary>
		/// Process the elements in the 'components' node.
		/// </summary>
		/// <param name="componentsNode"></param>
		/// <returns></returns>
		protected virtual IComponentProfile[] ProcessComponents(XmlNode componentsNode)
		{
			if (componentsNode == null) return new IComponentProfile[0];

			String assemblyName = ObtainDefaultAssemblyFromNode(componentsNode);
			ArrayList components = new ArrayList();

			foreach(XmlNode node in componentsNode)
			{
				components.Add(CreateComponentProfile(node, assemblyName));
			}

			return (IComponentProfile[]) components.ToArray(typeof (IComponentProfile));
		}

		/// <summary>
		/// Create an <see cref="IFacilityProfile"/> based on an <see cref="XmlNode"/>.
		/// </summary>
		/// <example>
		/// Follows a sample node in XML:
		/// <code>
		/// <![CDATA[
		/// &lt;facility key="key" type="type full name" /&gt;
		/// ]]>
		/// </code>
		/// </example>
		/// <param name="facilityNode"></param>
		/// <returns></returns>
		protected virtual IFacilityProfile CreateFacilityProfile(XmlNode facilityNode, String assemblyName)
		{
			String key = ObtainNonNullAttributeValue(facilityNode, "key");
			String typeName = ObtainNonNullAttributeValue(facilityNode, "type");

			return new DefaultFacilityProfile(key, GetType(typeName, assemblyName));
		}

		/// <summary>
		/// Create an <see cref="ISubSystemProfile"/> based on an <see cref="XmlNode"/>.
		/// </summary>
		/// <example>
		/// Follows a sample node in XML:
		/// <code>
		/// <![CDATA[
		/// &lt;subsystem key="key" type="type full name" /&gt;
		/// ]]>
		/// </code>
		/// </example>
		/// <param name="subsystemNode"></param>
		/// <param name="assemblyName"></param>
		/// <returns></returns>
		protected virtual ISubSystemProfile CreateSubSystemProfile(XmlNode subsystemNode, String assemblyName)
		{
			String key = ObtainNonNullAttributeValue(subsystemNode, "key");
			String typeName = ObtainNonNullAttributeValue(subsystemNode, "type");

			return new DefaultSubSystemProfile(key, GetType(typeName, assemblyName));
		}

		/// <summary>
		/// 
		/// </summary>
		/// <example>
		/// Follows a sample node in XML:
		/// <code>
		/// <![CDATA[
		/// &lt;component key="key" service="type full name" implementation="type full name"
		///   lifestyle="optional override" activation="optional override" &gt;
		///   &lt;configuration&gt; Any component configuration &lt;/configuration&gt;
		/// &lt;/component&gt;
		/// ]]>
		/// </code>
		/// </example>
		/// <param name="componentNode"></param>
		/// <param name="assemblyName"></param>
		/// <returns></returns>
		protected virtual IComponentProfile CreateComponentProfile(XmlNode componentNode, String assemblyName)
		{
			String key = ObtainNonNullAttributeValue(componentNode, "key");
			String service = ObtainNonNullAttributeValue(componentNode, "service");
			String impl = ObtainNonNullAttributeValue(componentNode, "implementation");
			Lifestyle lifestyle = (Lifestyle) ObtainEnumValue(componentNode, "lifestyle", typeof (Lifestyle), Lifestyle.Undefined);
			Activation activation = (Activation) ObtainEnumValue(componentNode, "activation", typeof (Activation), Activation.Undefined);

			IConfiguration configuration = DefaultConfigurationSerializer.Deserialize(componentNode);

			return new DefaultComponentProfile(key, 
				GetType(service, assemblyName), GetType(impl, assemblyName), 
				lifestyle, activation, configuration);
		}
	}
}