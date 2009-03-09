/*
 * This file is part of DRBD Management Console by LINBIT HA-Solutions GmbH
 * written by Rasto Levrinc.
 *
 * Copyright (C) 2009, LINBIT HA-Solutions GmbH.
 *
 * DRBD Management Console is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * DRBD Management Console is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with drbd; see the file COPYING.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 */


package drbd.data;

import drbd.data.resources.BlockDevice;
import drbd.data.resources.Network;
import drbd.utilities.Tools;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;

import java.awt.Color;

import drbd.gui.ClusterTab;
import drbd.gui.ClusterBrowser;

/**
 * This class holds cluster data and implementation of cluster related
 * methods.
 *
 * @author Rasto Levrinc
 * @version $Id$
 *
 */
public class Cluster {
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Name of the cluster. */
    private String name = null;
    /** Haclient password. */
    private String hbPasswd = "hacluster";
    /** Hosts that belong to this cluster. */
    private final Set<Host> hosts = new LinkedHashSet<Host>();
    /** Cluster tab of this cluster. */
    private ClusterTab clusterTab = null;
    /** Cluster browser of this cluster. */
    private ClusterBrowser clusterBrowser;
    /** Default colors of the hosts. */
    private final Color[] hostColors = {
                                  Color.YELLOW,
                                  Color.ORANGE,
                                  new Color(184, 207, 229), /* blue */
                                  Color.PINK,
                                  Color.WHITE,
                                 };

    /**
     * Prepares a new <code>Cluster</code> object.
     */
    public Cluster() {
        /* do nothing */
    }

    /**
     * Prepares a new <code>Cluster</code> object.
     */
    public Cluster(final String name) {
        this.name = name;
    }

    /**
     * Creates a new cluster browser object.
     */
    public final void createClusterBrowser() {
        clusterBrowser = new ClusterBrowser(this);
        clusterBrowser.initClusterResources();
    }

    /**
     * Sets name of this cluster.
     */
    public final void setName(final String name) {
        this.name = name;
    }

    /**
     * returns resource tree for this cluster.
     */
    public final ClusterBrowser getBrowser() {
        return clusterBrowser;
    }

    /**
     * Adds host to hosts, that are part of this cluster.
     */
    public final void addHost(final Host host) {
        final int id = hosts.size();
        if (id < hostColors.length) {
            host.setColor(hostColors[id]);
        }
        hosts.add(host);
    }

    /**
     * Gets set of hosts that are part of this cluster.
     */
    public final Set<Host> getHosts() {
        return hosts;
    }

    /**
     * Gets set of hosts that are part of this cluster as an array of strings.
     */
    public final Host[] getHostsArray() {
        return hosts.toArray(new Host [hosts.size()]);
    }

    /**
     * Removes all hosts.
     */
    public final void clearHosts() {
        hosts.clear();
    }

    /**
     * Returns number of hosts.
     */
    public final int hostsCount() {
        return hosts.size();
    }

    /**
     * Gets name of this cluster.
     */
    public final String getName() {
        return name;
    }

    /**
     * Sets cluster panel, that contains host views.
     */
    public final void setClusterTab(final ClusterTab clusterTab) {
        this.clusterTab = clusterTab;
    }

    /**
     * Gets cluster panel.
     */
    public final ClusterTab getClusterTab() {
        return clusterTab;
    }

    /**
     * Gets block devices that are common on all hosts in the cluster.
     * The block devices, that are already in the heartbeat or are used by
     * drbd are not returned.
     */
    public final List<String> getCommonBlockDevices() {
        List<String> blockDevicesNamesIntersection = null;

        for (final Host host : hosts) {
            blockDevicesNamesIntersection =
                                host.getBlockDevicesNamesIntersection(
                                                blockDevicesNamesIntersection);
        }

        final List<String> commonBlockDevices = new ArrayList<String>();
        for (final String i : blockDevicesNamesIntersection) {
            commonBlockDevices.add(i);
        }
        return commonBlockDevices;
    }

    /**
     * Returns block device objects of all hosts.
     */
    public final BlockDevice[] getHostBlockDevices(final String device) {
        final List<BlockDevice> list = new ArrayList<BlockDevice>();
        for (final Host host : hosts) {
            final BlockDevice bd = host.getBlockDevice(device);
            list.add(bd);
        }
        return list.toArray(new BlockDevice [list.size()]);
    }

    /**
     * Returns true if cluster contains the host.
     */
    public final boolean contains(final String hostName) {
        for (final Host host : hosts) {
            if (hostName.equals(host.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets networks that are common on all hosts in the cluster.
     */
    public final Network[] getCommonNetworks() {
        List<String> networksIntersection = null;
        for (final Host host : hosts) {
            networksIntersection =
                            host.getNetworksIntersection(networksIntersection);
        }

        final List<Network> commonNetworks = new ArrayList<Network>();
        for (final String netIp : networksIntersection) {
            final List<String> ips = new ArrayList<String>();
            for (final Host host : hosts) {
                ips.addAll(host.getIpsFromNetwork(netIp));
            }
            final Network network =
                    new Network(netIp, ips.toArray(new String[ips.size()]));
            commonNetworks.add(network);
        }

        return commonNetworks.toArray(new Network [commonNetworks.size()]);
    }

    /**
     * Gets filesystems that are common on all hosts in the cluster.
     */
    public final String[] getCommonFileSystems() {
        List<String> intersection = null;
        for (final Host host : hosts) {
            intersection = Tools.getIntersection(host.getFileSystemsList(),
                                                 intersection);
        }
        return intersection.toArray(new String[intersection.size()]);
    }

    /**
     * Gets mount points that are common on all hosts in the cluster.
     */
    public final String[] getCommonMountPoints() {
        List<String> intersection = null;

        for (final Host host : hosts) {
            intersection = Tools.getIntersection(host.getMountPointsList(),
                                                 intersection);
        }
        return intersection.toArray(new String[intersection.size()]);
    }

    /**
     * Returns the color for graph for the specified host.
     */
    public final Color getHostColor(final String node) {
        if (node == null) {
            return Tools.getDefaultColor("HeartbeatGraph.FillPaintStopped");
        }
        for (final Host host : hosts) {
            if (node.equals(host.getName())) {
                return host.getColor();
            }
        }
        return Color.WHITE;
        // TODO: checking against name is wrong
        //Tools.appError("Error in getHostColor");
        //return null;
    }

    /**
     * Returns the host object with the specified name.
     */
    public final Host getHostByName(final String name) {
        for (final Host host : hosts) {
            if (name.equals(host.getName())) {
                return host;
            }
        }
        return null;
    }

    /**
     * Sets the haclient password.
     */
    public final void setHbPasswd(final String hbPasswd) {
        this.hbPasswd = hbPasswd;
    }

    /**
     * Returns the haclient password.
     */
    public final String getHbPasswd() {
        return hbPasswd;
    }
}