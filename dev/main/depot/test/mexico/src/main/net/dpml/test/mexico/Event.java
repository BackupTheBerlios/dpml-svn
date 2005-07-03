
package net.dpml.test.mexico; 

import java.util.EventObject;

public class Event extends EventObject
{
    private int m_count;

    public Event( Object source, int count )
    {
        super( source );
        m_count = count;
    }

    public int getCount()
    {
        return m_count;
    }
}
