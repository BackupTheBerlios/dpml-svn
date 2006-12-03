package acme;

import java.net.URI;
import java.util.logging.Logger;

public class ChildImpl implements Child 
{
    private boolean started;
    private Logger  logger;
    
    public ChildImpl( Logger logger )
    {
        this.started = false;
        this.logger  = logger;
    }
    
    public void start( URI configurationURI ) throws Exception {
        if ( !this.started ){
            this.started = true;
            this.logger.info( "starting with: " + configurationURI );
        } else {
            throw new Exception( "already started!" );
        }
    }

    public void stop() throws Exception {
        if ( this.started ){
            this.started = false;
            this.logger.info( "stopping" );
        } else {
            throw new Exception("not started!" );
        }
    }

}
