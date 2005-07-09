
The hosts configuation files when full expanded contains multiple <host> 
element entries.  The following example shows a host element with all 
elements declared:

<hosts>

  <host href="http://www.dpml.net/repository" enabled="true">
    <priority>20</priority>
    <policy>fast</policy>
    <trusted>false</trusted>
    <class>net.dpml.transit.host.ClassicResourceHost</class>
    <index href="index.idx">
    <credentials>
      <username>fred</username>
      <password>blogs</password>
    </credentials>
    <scheme>basic</scheme> <!-- The HTTP authentication scheme -->
    <prompt>Repository</prompt>  <!-- The HTTP Realm/prompt  -->
  </host>

</hosts>

