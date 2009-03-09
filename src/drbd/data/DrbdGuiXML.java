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

import drbd.utilities.Tools;
import drbd.gui.TerminalPanel;
//import drbd.utilities.States;

import java.io.IOException;
import java.io.OutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


/**
 * This class parses xml from drbdsetup and drbdadm, stores the
 * information in the hashes and provides methods to get this
 * information.
 * The xml is obtained with drbdsetp xml command and drbdadm dump-xml.
 *
 * @author Rasto Levrinc
 * @version $Id$
 */
public class DrbdGuiXML extends XML {
    /** Host name attribute string. */
    private static final String HOST_NAME_ATTR = "name";
    /** Cluster name attribute string. */
    private static final String CLUSTER_NAME_ATTR = "name";
    /** Heartbeat password attribute string. */
    private static final String HB_PASSWD_ATTR = "hbpw";
    /** Name of the host node. */
    private static final String HOST_NODE_STRING = "host";

    /**
     * Saves data about clusters and hosts to the supplied output stream.
     */
    public final String saveXML(final OutputStream outputStream)
    throws IOException {
        final String encoding = "UTF-8";
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;

        try {
             db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
             assert false;
        }
        final Document doc = db.newDocument();
        final Element root = (Element) doc.appendChild(
                                                doc.createElement("drbdgui"));
        final Element hosts = (Element) root.appendChild(
                                                doc.createElement("hosts"));
        for (final Host host : Tools.getConfigData().getHosts().getHostSet()) {
            final String hostName = host.getName();
            final String ip = host.getIp();
            final String username = host.getUsername();
            final Element hostNode = (Element) hosts.appendChild(
                                        doc.createElement(HOST_NODE_STRING));
            hostNode.setAttribute(HOST_NAME_ATTR, hostName);
            if (ip != null) {
                final Node ipNode = (Element) hostNode.appendChild(
                                                       doc.createElement("ip"));

                ipNode.appendChild(doc.createTextNode(ip));
            }
            if (username != null) {
                final Node usernameNode =
                                (Element) hostNode.appendChild(
                                                    doc.createElement("user"));

                usernameNode.appendChild(doc.createTextNode(username));
            }

        }
        final Element clusters = (Element) root.appendChild(
                                                doc.createElement("clusters"));

        final Set<Cluster> clusterSet =
                        Tools.getConfigData().getClusters().getClusterSet();
        for (final Cluster cluster : clusterSet) {
            final String clusterName = cluster.getName();
            final String hbPasswd = cluster.getHbPasswd();
            final Element clusterNode = (Element) clusters.appendChild(
                                                doc.createElement("cluster"));
            clusterNode.setAttribute(CLUSTER_NAME_ATTR, clusterName);
            clusterNode.setAttribute(HB_PASSWD_ATTR, hbPasswd);
            for (final Host host : cluster.getHosts()) {
                final String hostName = host.getName();
                final Element hostNode =
                                (Element) clusterNode.appendChild(
                                        doc.createElement(HOST_NODE_STRING));
                hostNode.appendChild(doc.createTextNode(hostName));
            }
        }

        final TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = null;
        try {
            t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty(OutputKeys.METHOD, "xml");
            t.setOutputProperty(OutputKeys.ENCODING, encoding);
        } catch (TransformerConfigurationException tce) {
            assert false;
        }
        final DOMSource doms = new DOMSource(doc);
        final StreamResult sr = new StreamResult(outputStream);
        try {
            t.transform(doms, sr);
        } catch (TransformerException te) {
            final IOException ioe = new IOException();
            ioe.initCause(te);
            throw ioe;
        }
        return "";
    }

    /**
     * Loads info from xml that is supplied as an argument to the internal
     * data objects.
     */
    public final void loadXML(final String xml) {
        final Document document = getXMLDocument(xml);
        /* get root <drbdgui> */
        final Node rootNode = getChildNode(document, "drbdgui");
        final Map<String, Host> hostMap = new HashMap<String, Host>();
        if (rootNode != null) {
            /* hosts */
            final Node hostsNode = getChildNode(rootNode, "hosts");
            if (hostsNode != null) {
                final NodeList hosts = hostsNode.getChildNodes();
                if (hosts != null) {
                    for (int i = 0; i < hosts.getLength(); i++) {
                        final Node hostNode = hosts.item(i);
                        if (hostNode.getNodeName().equals(HOST_NODE_STRING)) {
                            final Node ipNode = getChildNode(hostNode, "ip");
                            final String ip = getText(ipNode);
                            final Node usernameNode = getChildNode(hostNode,
                                                                   "user");
                            final String username = getText(usernameNode);
                            final String nodeName =
                                                getAttribute(hostNode,
                                                             HOST_NAME_ATTR);
                            final Host host = new Host();
                            //host.setHostnameEntered(nodeName);
                            //host.setIp(ip);
                            //host.setName(nodeName);
                            host.setHostname(nodeName);
                            Tools.getConfigData().addHostToHosts(host);

                            //Tools.getGUIData().addHostTab(host);
                            final TerminalPanel terminalPanel = new TerminalPanel(host);
                            Tools.getGUIData().setTerminalPanel(terminalPanel);
                            host.setIp(ip);
                            host.setUsername(username);
                            hostMap.put(nodeName, host);
                        }
                    }
                }
            }

            /* clusters */
            final Node clustersNode = getChildNode(rootNode, "clusters");
            if (clustersNode != null) {
                final NodeList clusters = clustersNode.getChildNodes();
                if (clusters != null) {
                    for (int i = 0; i < clusters.getLength(); i++) {
                        final Node clusterNode = clusters.item(i);
                        if (clusterNode.getNodeName().equals("cluster")) {
                            final String clusterName =
                                               getAttribute(clusterNode,
                                                            CLUSTER_NAME_ATTR);

                            final String hbPasswd =
                                                  getAttribute(clusterNode,
                                                               HB_PASSWD_ATTR);

                            final Cluster cluster = new Cluster();
                            cluster.setName(clusterName);
                            cluster.setHbPasswd(hbPasswd);
                            Tools.getConfigData().addClusterToClusters(cluster);
                            Tools.getGUIData().addClusterTab(cluster);
                            loadClusterHosts(clusterNode, cluster, hostMap);

                            final Runnable runnable = new Runnable() {
                                public void run() {
                                    for (final Host host : cluster.getHosts()) {
                                        host.waitOnLoading();
                                        //States.wait("load:" + host.getName(),
                                        //            0);
                                    }
                                    cluster.getClusterTab().addClusterView();
                                    cluster.getClusterTab().requestFocus();
                                }
                            };
                            final Thread thread = new Thread(runnable);
                            thread.start();
                        }
                    }
                }
            }
        }
    }

    /**
     * Loads info about hosts from the specified cluster to the internal data
     * objects.
     */
    private boolean loadClusterHosts(final Node clusterNode,
                                     final Cluster cluster,
                                     final Map<String, Host> hostMap) {
        final NodeList hosts = clusterNode.getChildNodes();
        final boolean ok = true;
        if (hosts != null) {
            for (int i = 0; i < hosts.getLength(); i++) {
                final Node hostNode = hosts.item(i);
                if (hostNode.getNodeName().equals(HOST_NODE_STRING)) {
                    final String nodeName = getText(hostNode);
                    final Host host = hostMap.get(nodeName);
                    if (host != null) {
                        host.setIsLoading();
                        //States.init("load:" + host.getName());
                        host.setCluster(cluster);
                        cluster.addHost(host);
                        //host.getHostViewPanel().connect();
                        host.connect();
                    }
                }
            }
        }
        return ok; // TODO: is not used?
    }
}