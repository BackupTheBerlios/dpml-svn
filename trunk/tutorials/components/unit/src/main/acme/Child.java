package acme;

import java.net.URI;

public interface Child 
{
    public void start( URI configurationURI ) throws Exception;
    public void stop() throws Exception;
}
