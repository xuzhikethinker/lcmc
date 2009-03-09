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


package drbd.gui.dialog;

import drbd.data.Host;
import drbd.utilities.Tools;
import drbd.gui.SpringUtilities;
import drbd.gui.GuiComboBox;

import javax.swing.JLabel;
import javax.swing.SpringLayout;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.awt.Component;

import java.net.UnknownHostException;
import java.net.InetAddress;

/**
 * An implementation of a dialog where entered ip or the host is looked up
 * with dns.
 *
 * @author Rasto Levrinc
 * @version $Id$
 *
 */
public class HostConfiguration extends DialogHost {
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Maximum hops. */
    private static final int MAX_HOPS = Tools.getDefaultInt("MaxHops");
    /** Name field. */
    private GuiComboBox nameField;
    /** Hostname fields. */
    private GuiComboBox[] hostnameField =
                            new GuiComboBox[MAX_HOPS];
    /** Ip fields. */
    private GuiComboBox[] ipCombo =
                            new GuiComboBox[MAX_HOPS];
    /** Hostnames. */
    private String[] hostnames = new String[MAX_HOPS];
    /** Whether the hostname was ok. */
    private boolean hostnameOk = false;
    /** Width of the combo boxes. */
    private static final int COMBO_BOX_WIDTH = 120;
    /** DNS timeout. */
    private static final int DNS_TIMEOUT = 5000;

    /**
     * Prepares a new <code>HostConfiguration</code> object.
     */
    public HostConfiguration(final WizardDialog previousDialog,
                             final Host host) {
        super(previousDialog, host);
    }

    /**
     * Returns the previous dialog.
     */
    public WizardDialog getPreviousDialog() {
        //Tools.getGUIData().renameSelectedHostTab(getHost().getName());
        return super.getPreviousDialog();
    }

    /**
     * Finishes the dialog and stores the values.
     */
    protected void finishDialog() {
        final String name = nameField.getStringValue().trim();
        getHost().setHostname(Tools.join(",", hostnames, getHops()));
        getHost().setName(name);
        final int hops = getHops();
        String[] ipsA = new String[hops];
        for (int i = 0; i < hops; i++) {
            ipsA[i] = ipCombo[i].getStringValue();
        }
        getHost().setIp(Tools.join(",", ipsA));
    }

    /**
     * Returns the next dialog. Depending on if the host is already connected
     * it is the HostSSH or it is skipped and HostDevices is the next dialog.
     */
    public WizardDialog nextDialog() {
        if (hostnameOk) {
            if (getHost().isConnected()) {
                return new HostDevices(this, getHost());
            } else {
                return new HostSSH(this, getHost());
            }
        } else {
            return this;
        }
    }

    /**
     * Checks the fields and if they are correct the buttons will be enabled.
     */
    protected void checkFields(final GuiComboBox field) {
        boolean isValid = false;
        final String name = nameField.getStringValue().trim();
        if (hostnameOk) {
            isValid = (name.length() > 0);
        }
        buttonClass(nextButton()).setEnabled(isValid);
    }

    /**
     * Returns the title of the dialog. It is defined as
     * Dialog.HostConfiguration.Title in TextResources.
     */
    protected String getHostDialogTitle() {
        return Tools.getString("Dialog.HostConfiguration.Title");
    }

    /**
     * Returns the description of the dialog. It is defined as
     * Dialog.HostConfiguration.Description in TextResources.
     */
    protected String getDescription() {
        return Tools.getString("Dialog.HostConfiguration.Description");
    }

    /**
     * Checks the dns entries for all the hosts.
     * This assumes that getHost().hostnameEntered was set.
     */
    protected boolean checkDNS(final int hop, final String hostnameEntered) {
        InetAddress[] addresses = null;
        try {
            addresses = InetAddress.getAllByName(hostnameEntered);
        } catch (UnknownHostException e) {
            return false;
        }
        String hostname = null;
        String ip = null;
        Tools.debug(this, "addresses.length: " + addresses.length + "a: "
                          + addresses[0].getHostAddress());
        if (addresses.length == 0) {
            Tools.debug(this, "lookup failed");
            // lookup failed;
            return false;
        } else if (addresses.length == 1) {
            ip = addresses[0].getHostAddress();
            /* if user entered ip, reverse lookup is needed.
             * Making reverse lookup even if user entered a some of the
             * hostnames since it can be different than canonical name.
             */
            try {
                hostname =
                    addresses[0].getByName(ip).getHostName();
            } catch (UnknownHostException e) {
                Tools.appError("HostConfiguration.Unknown.Host", "", e);
                return false;
            }
        } else {
            // user entered hostname that has many addresses
            hostname = hostnameEntered;
            ip = getHost().getIp(hop);
            if (ip == null) {
                ip = addresses[0].getHostAddress();
            }
        }
        String[] items = new String[addresses.length];
        for (int i = 0; i < addresses.length; i++) {
            items[i] = addresses[i].getHostAddress();
        }
        //getHost().setIp(ip);
        getHost().setIps(hop, items);
        hostnames[hop] = hostname;

        hostnameField[hop].setValue(hostname);
        Tools.debug(this, "got " + hostname + " (" + ip + ")", 1);
        return true;
    }

    /**
     * Class that implements check dns thread.
     */
    class CheckDNSThread extends Thread {
        /** Number of hops. */
        private final int hop;
        /** Host names as entered by user. */
        private final String hostnameEntered;

        /**
         * Prepares a new <code>CheckDNSThread</code> object, with number of
         * hops and host names delimited with commas.
         */
        public CheckDNSThread(final int hop, final String hostnameEntered) {
            super();
            this.hop = hop;
            this.hostnameEntered = hostnameEntered;
        }

        /**
         * Runs the check dns thread.
         */
        public void run() {
            answerPaneSetText(
                        Tools.getString("Dialog.HostConfiguration.DNSLookup"));
            hostnameOk = checkDNS(hop, hostnameEntered);
            if (hostnameOk) {
                answerPaneSetText(
                    Tools.getString("Dialog.HostConfiguration.DNSLookupOk"));
            } else {
                printErrorAndRetry(
                    Tools.getString("Dialog.HostConfiguration.DNSLookupError"));
            }
            if (hostnameOk) {
                nameField.setEnabled(true);
            }
            final String[] items = getHost().getIps(hop);
            if (items != null) {
                ipCombo[hop].reloadComboBox(getHost().getIp(hop), items);

                if (items.length > 1) {
                    ipCombo[hop].setEnabled(true);
                }
            }
        }
    }

    /**
     * Returns number of hops.
     */
    protected int getHops() {
        final String hostnameEntered = getHost().getHostnameEntered();
        return Tools.charCount(hostnameEntered, ',') + 1;
    }

    /**
     * Inits dialog and starts dns check for every host.
     */
    protected void initDialog() {
        super.initDialog();
        enableComponentsLater(new JComponent[]{buttonClass(nextButton())});

        nameField.setEnabled(false);
        if (getHost().getIp() == null) {
            final CheckDNSThread[] checkDNSThread =
                            new CheckDNSThread[MAX_HOPS];
            getProgressBar().start(DNS_TIMEOUT);
            for (int i = 0; i < getHops(); i++) {
                final String hostnameEntered =
                                getHost().getHostnameEntered().split(",")[i];
                hostnameField[i].setEnabled(false);
                checkDNSThread[i] = new CheckDNSThread(i, hostnameEntered);
                checkDNSThread[i].setPriority(Thread.MIN_PRIORITY);
                checkDNSThread[i].start();
            }


            final Thread thread = new Thread(
                new Runnable() {
                    public void run() {
                        for (int i = 0; i < getHops(); i++) {
                            try {
                                checkDNSThread[i].join();
                            } catch (java.lang.InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        progressBarDone();
                        SwingUtilities.invokeLater(new Runnable() { public void run() {
                            getHost().setHostname(Tools.join(",", hostnames, getHops()));
                            nameField.requestFocus();
                            nameField.selectAll();
                            checkFields(nameField);
                            addCheckField(nameField);
                            nameField.setValue(getHost().getName());
                            enableComponents();
                        } });
                    }
                });
            thread.start();
        } else {
            getHost().setHostname(Tools.join(",", hostnames, getHops()));
            nameField.requestFocus();
            nameField.selectAll();
            checkFields(nameField);
            addCheckField(nameField);
            nameField.setValue(getHost().getName());
            for (int i = 0; i < getHops(); i++) {
                hostnameField[i].setEnabled(false);
            }
            enableComponents();
            hostnameOk = true;
        }
    }

    /**
     * Returns input pane where names of host or more hosts delimited with
     * comma, can be entered.
     */
    protected JComponent getInputPane() {
        final int hops = getHops();
        final JPanel inputPane = new JPanel(new SpringLayout());
        inputPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        /* Name */
        final JLabel nameLabel = new JLabel(
                            Tools.getString("Dialog.HostConfiguration.Name"));

        inputPane.add(nameLabel);
        final String regexp = "^[\\w.-]+$";
        nameField = new GuiComboBox(getHost().getName(),
                                    null,
                                    null,
                                    regexp,
                                    COMBO_BOX_WIDTH);
        nameField.selectAll();
        inputPane.add(nameField);
        nameField.setBackground(getHost().getName(), true);
        for (int i = 0; i < hops - 1; i++) {
            inputPane.add(new JLabel(""));
        }

        /* Host/Hosts */
        final JLabel hostnameLabel = new JLabel(
                        Tools.getString("Dialog.HostConfiguration.Hostname"));
        inputPane.add(hostnameLabel);
        final String hostname = getHost().getHostname();
        if (hostname == null || Tools.charCount(hostname, ',') == 0) {
            hostnames[0] = hostname;
        } else {
            hostnames = hostname.split(",");
        }
        for (int i = 0; i < hops; i++) {
            hostnameField[i] = new GuiComboBox(hostnames[i],
                                               null,
                                               null,
                                               null,
                                               COMBO_BOX_WIDTH);

            inputPane.add(hostnameField[i]);
        }

        final JLabel ipLabel = new JLabel(
                            Tools.getString("Dialog.HostConfiguration.Ip"));
        inputPane.add(ipLabel);

        for (int i = 0; i < hops; i++) {
            if (getHost().getIp(i) == null) {
                getHost().setIps(i, null);
            }
            ipCombo[i] = new GuiComboBox(getHost().getIp(i),
                                         getHost().getIps(i),
                                         GuiComboBox.Type.COMBOBOX,
                                         null,
                                         COMBO_BOX_WIDTH);

            inputPane.add(ipCombo[i]);
            ipCombo[i].setEnabled(false);
        }

        SpringUtilities.makeCompactGrid(inputPane, 3, 1 + hops, // rows, cols
                                                   1, 1,  // initX, initY
                                                   1, 1); // xPad, yPad
        final JPanel pane = new JPanel(new SpringLayout());
        pane.add(getProgressBarPane());
        pane.add(inputPane);
        pane.add(getAnswerPane(""));
        SpringUtilities.makeCompactGrid(pane, 3, 1,  //rows, cols
                                              1, 1,  //initX, initY
                                              1, 1); //xPad, yPad
        return pane;
    }
}