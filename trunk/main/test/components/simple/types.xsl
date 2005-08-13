<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:lxslt="http://xml.apache.org/xslt"
    xmlns:redirect="http://xml.apache.org/xalan/redirect"
    extension-element-prefixes="redirect">

  <xsl:output method="html" indent="yes" encoding="US-ASCII"/>
  <xsl:decimal-format decimal-separator="." grouping-separator="," />

  <xsl:param name="output.dir" select="'.'"/>

  <xsl:template match="type">
     <html>
       <head>
         <title>Component Type</title>
       </head>
       <body>
       <xsl:apply-templates/>
       </body>
     </html>
   </xsl:template>

  <xsl:template match="m__descriptor">
    <h3>Info</h3>
    <table>
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="m__name">
    <tr><td>Name:</td><td><xsl:apply-templates/></td></tr>
  </xsl:template>

  <xsl:template match="m__classname">
    <tr><td>Class:</td><td><xsl:apply-templates/></td></tr>
  </xsl:template>

  <xsl:template match="m__version">
    <tr><td>Version:</td><td><xsl:apply-templates/></td></tr>
  </xsl:template>

  <xsl:template match="m__major">
    <xsl:apply-templates/>.
  </xsl:template>

  <xsl:template match="m__minor">
    <xsl:apply-templates/>.
  </xsl:template>

  <xsl:template match="m__micro">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="m__lifestyle">
    <tr><td>Lifestyle:</td><td><xsl:apply-templates/></td></tr>
  </xsl:template>

  <xsl:template match="m__collection">
    <tr><td>Collection Policy:</td><td><xsl:apply-templates/></td></tr>
  </xsl:template>

  <xsl:template match="m__threadsafe">
    <tr><td>Threadsafe:</td><td><xsl:apply-templates/></td></tr>
  </xsl:template>

  <xsl:template match="m__native">
    <tr><td>Native:</td><td><xsl:apply-templates/></td></tr>
  </xsl:template>

  <xsl:template match="m__context">
    <h3>Context</h3>
    <table>
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="m__entries">
    <table>
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="net.dpml.composition.info.EntryDescriptor">
    <td><xsl:apply-templates/></td>
  </xsl:template>

  <xsl:template match="m__key">
    <tr><td><xsl:apply-templates/></td></tr>
  </xsl:template>

  <xsl:template match="m__optional">
    <tr><td>Optional:</td><td><xsl:apply-templates/></td></tr>
  </xsl:template>

  <xsl:template match="m__volatile">
    <tr><td>Volatile:</td><td><xsl:apply-templates/></td></tr>
  </xsl:template>

  <xsl:template match="m__services">
    <h3>Services</h3>
    <table>
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="net.dpml.part.component.ServiceDescriptor">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="m__parts">
    <h3>Parts</h3>
    <table>
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="net.dpml.part.PartReference">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="m__operations">
    <td>
      <table>
        <xsl:apply-templates/>
      </table>
    </td>
  </xsl:template>


</xsl:stylesheet>

