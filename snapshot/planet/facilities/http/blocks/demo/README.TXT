
HTTP DEMO README
----------------

The demo project demonstrates the setup of a http server and context within which 
he handle the serving of static and dynamic content.  The block for this project is
declared with the build.xml file and covers the inclusion of the http server block, 
the inclusion of a context containing a static handler, redirect handler, model 
handler and a number of error handlers.  Finally, the block contains a component
that we use to handle some dynamic content:

    <x:block name="demo">
      <x:include name="server" artifact="block:avalon/http/avalon-http-server#SNAPSHOT"/>
      <x:include name="static-context" artifact="block:avalon/http/avalon-http-static#SNAPSHOT"/>
      <x:component name="hello" class="org.apache.avalon.playground.HelloComponent"/>
    </x:block>

The block is configured using the a targets file.  The file includes three target 
overrides, one dealing with the context definition, the second demonstrating some
redirection capability, and the third demonstrating http path to component path 
mapping.

The static content handler is configured to use the current directory as the default
root content basedir (signified by the context-path and resource-base element value).

  <target path="/demo/static-context/context">
    <configuration>
      <context-path>/</context-path>
      <resource-base>.</resource-base>
    </configuration>
  </target>

The following target overrides the configuration for a url forwarding handler.  It 
simply forwards a 'from' path to a 'to' path.  For example, if you enter a url such
as http://localhost/src/java you will be redirected to http://localhost/src/main.

  <target path="/demo/static-context/forward-handler">
    <configuration>
      <forwards>
         <forward>
           <from>/src/java</from>
           <to>/src/main</to>
         </forward>
      </forwards>
    </configuration>
  </target>

The following target override directs incomming urls with the path '/hello' to the 
component at the address '/demo/hello'.

  <target path="/demo/static-context/model-handler">
    <parameters>
      <parameter name="context-path" value="/hello"/>
      <parameter name="target" value="/demo/hello"/>
    </parameters>
  </target>

</targets>

To build the server do the following:

  $ cd avalon                          <--- your root avalon/trunk directory
  $ ant                                <--- build al of avalon
  $ cd planet\facilities\http\blocks 
  $ ant
  $ cd demo

To test the server you can either execute the generated block from the local file system
of from magic's repository.

Executing the block from the local filesystem:

  $ merlin target\deliverables\blocks\avalon-http-demo-SNAPSHOT.block -config etc\config.xml

Executing the block from the repository (the following assumes that you have merlin.repository 
referencing the magic repository)

  $ merlin artifact:block:avalon/http/avalon-http-demo#SNAPSHOT -config etc\config.xml

Open a browser and enter the following url:

  http://localhost:8080

You should see the contents of the local directory in your browser.  

  http://localhost:8080/hello

You should see a simple "Hello" message created by the HelloComponent.

SJM
