/*
 * Copyright 2005 Stephen McConnell
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dpml.depot.desktop;

import java.awt.Component;
import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import net.dpml.depot.Handler;

import net.dpml.station.Station;
import net.dpml.station.Application;
import net.dpml.profile.ApplicationRegistry;
import net.dpml.profile.ApplicationProfile;

import net.dpml.transit.Transit;
import net.dpml.transit.Repository;
import net.dpml.transit.Logger;

/**
 * Application registry root tree node. 
 */
public final class ApplicationRegistryTreeNode extends GroupTreeNode
{
    private static final int ID_COLUMN = 0;
    private static final int CODEBASE_COLUMN = 1;
    private static final int COLUMN_COUNT = 2;

    private final Logger m_logger;

    public ApplicationRegistryTreeNode( Logger logger ) throws Exception
    {
        super( "" );

        m_logger = logger;
    }


    private Logger getLogger()
    {
        return m_logger;
    }

}