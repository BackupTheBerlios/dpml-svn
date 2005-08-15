<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:lxslt="http://xml.apache.org/xslt"
    xmlns:redirect="http://xml.apache.org/xalan/redirect"
    extension-element-prefixes="redirect">

  <xsl:output method="html" indent="yes" encoding="US-ASCII"/>
  <xsl:decimal-format decimal-separator="." grouping-separator="," />

  <xsl:param name="output.dir" select="'.'"/>
  <xsl:param name="basepath" select="'.'"/>
  <xsl:param name="package" select="'.'"/>
  <xsl:param name="classname" select="'.'"/>

  <xsl:template match="type">
    <html>
      <head>
        <title>Type: <xsl:value-of select="$classname"/></title>
        <link rel="stylesheet" type="text/css" href="{$basepath}/stylesheet.css"/>
      </head>
      <body>
        <xsl:apply-templates select="m__descriptor"/>
        <h4>Services</h4>
        <xsl:apply-templates select="m__services"/>
        <h4>Context</h4>
        <xsl:apply-templates select="m__context"/>
        <h4>Parts</h4>
        <xsl:apply-templates select="m__parts"/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="m__descriptor">
    <p><xsl:value-of select="$package"/></p>
    <h3><xsl:value-of select="$classname"/></h3>
    <!--<h3><xsl:apply-templates select="m__classname"/></h3>-->
    <h4>Info</h4>
    <table>
      <tr><td>Version:</td><td><xsl:apply-templates select="m__version"/></td></tr>
      <tr><td>Name:</td><td><xsl:value-of select="m__name"/></td></tr>
      <tr><td>Lifestyle:</td><td><xsl:value-of select="m__lifestyle"/></td></tr>
      <tr><td>Thread-safe:</td><td><xsl:value-of select="m__threadsafe"/></td></tr>
      <tr><td>Collection:</td><td><xsl:apply-templates select="m__collection"/></td></tr>
    </table>
  </xsl:template>

  <xsl:template match="m__version">
    <xsl:value-of select="m__major"/>.<xsl:value-of select="m__minor"/>.<xsl:value-of select="m__micro"/>
  </xsl:template>

  <xsl:template match="m__collection">
      <xsl:if test="node() = '2'">hard</xsl:if>
      <xsl:if test="node() = '1'">soft</xsl:if>
      <xsl:if test="node() = '0'">weak</xsl:if>
      <xsl:if test="node() = '-1'">undefined</xsl:if>
  </xsl:template>

  <xsl:template match="m__context">
    <xsl:apply-templates select="m__entries"/>
  </xsl:template>

  <xsl:template match="m__entries">
    <table>
      <xsl:for-each select="net.dpml.composition.info.EntryDescriptor">
      <tr>
        <td>
          <xsl:value-of select="m__key"/>
        </td>
        <td>
          <xsl:apply-templates select="m__optional"/>
        </td>
        <td>
          <xsl:value-of select="m__classname"/>
        </td>
      </tr>
      </xsl:for-each>
    </table>
  </xsl:template>

  <xsl:template match="net.dpml.composition.info.EntryDescriptor">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="m__key">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="m__optional">
    <xsl:if test="node() = 'true'">optional</xsl:if>
    <xsl:if test="node() = 'false'">required</xsl:if>
  </xsl:template>

  <xsl:template match="m__volatile">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="m__services">
    <table>
      <xsl:for-each select="net.dpml.part.component.ServiceDescriptor">
      <tr>
        <td>
          <xsl:value-of select="m__classname"/>
        </td>
        <!-- leave the service version out of the picture fr now -->
        <!--
        <td>
          <xsl:apply-templates select="m__version"/>
        </td>
        -->
      </tr>
      </xsl:for-each>
    </table>
  </xsl:template>

  <xsl:template match="m__parts">
    <table>
      <xsl:for-each select="net.dpml.part.PartReference">
      <tr>
        <td>
          <xsl:value-of select="m__key"/>
        </td>
        <td>
          <xsl:apply-templates select="m__part"/>
        </td>
      </tr>
      </xsl:for-each>
    </table>
  </xsl:template>

  <xsl:template match="m__part">
    <xsl:variable name="cname">
      <xsl:value-of select="m__classname"/>
    </xsl:variable>
    <a>
      <xsl:attribute name="href">
        <xsl:value-of select="$basepath"/>
        <xsl:text>/</xsl:text>
        <xsl:value-of select="translate($cname,'.','/')"/>
        <xsl:text>.html</xsl:text>
      </xsl:attribute>
      <xsl:value-of select="$cname"/>
    </a>
  </xsl:template>

</xsl:stylesheet>

