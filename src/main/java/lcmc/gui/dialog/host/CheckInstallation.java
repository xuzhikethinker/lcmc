/*
 * This file is part of DRBD Management Console by LINBIT HA-Solutions GmbH
 * written by Rasto Levrinc.
 *
 * Copyright (C) 2009, LINBIT HA-Solutions GmbH.
 * Copyright (C) 2011-2012, Rastislav Levrinc.
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

package lcmc.gui.dialog.host;

import lcmc.data.Host;
import lcmc.data.ConfigData;
import lcmc.utilities.Tools;
import lcmc.utilities.MyButton;
import lcmc.utilities.ExecCallback;
import lcmc.utilities.SSH;
import lcmc.gui.SpringUtilities;
import lcmc.gui.widget.Widget;
import lcmc.gui.dialog.WizardDialog;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;

/**
 * An implementation of a dialog where
 * drbd/heartbeat/pacemaker/openais/corosync etc. installation is checked.
 *
 * @author Rasto Levrinc
 * @version $Id$
 *
 */
final class CheckInstallation extends DialogHost {
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Next dialog object. */
    private WizardDialog nextDialogObject = null;

    /** Checking drbd label. */
    private final JLabel drbdLabel = new JLabel(
          ": " + Tools.getString("Dialog.Host.CheckInstallation.CheckingDrbd"));
    /** Checking corosync/openais/pacemaker label. */
    private final JLabel pmLabel = new JLabel(
          ": " + Tools.getString("Dialog.Host.CheckInstallation.CheckingPm"));
    /** Checking heartbeat/pacemaker label. */
    private final JLabel hbPmLabel = new JLabel(
          ": " + Tools.getString("Dialog.Host.CheckInstallation.CheckingHbPm"));

    /** Install/Upgrade drbd button. */
    private final MyButton drbdButton = new MyButton(
            Tools.getString("Dialog.Host.CheckInstallation.DrbdInstallButton"));
    /** Install corosync/openais/pacemaker button. */
    private final MyButton pmButton = new MyButton(
            Tools.getString("Dialog.Host.CheckInstallation.PmInstallButton"));
    /** Install heartbeat/pacemaker button. */
    private final MyButton hbPmButton = new MyButton(
            Tools.getString("Dialog.Host.CheckInstallation.HbPmInstallButton"));
    /** Corosync/Openais/Pacemaker installation method. */
    private Widget pmInstMethodWi;
    /** Heartbeat/pacemaker installation method. */
    private Widget hbPmInstMethodWi;
    /** DRBD installation method. */
    private Widget drbdInstMethodWi;

    /** Checking icon. */
    private static final ImageIcon CHECKING_ICON =
        Tools.createImageIcon(
                Tools.getDefault("Dialog.Host.CheckInstallation.CheckingIcon"));
    /** Not installed icon. */
    private static final ImageIcon NOT_INSTALLED_ICON =
        Tools.createImageIcon(
            Tools.getDefault("Dialog.Host.CheckInstallation.NotInstalledIcon"));
    /** Already installed icon. */
    private static final ImageIcon INSTALLED_ICON =
        Tools.createImageIcon(
              Tools.getDefault("Dialog.Host.CheckInstallation.InstalledIcon"));
    /** Upgrade available icon. */
    private static final ImageIcon UPGR_AVAIL_ICON =
        Tools.createImageIcon(
              Tools.getDefault("Dialog.Host.CheckInstallation.UpgrAvailIcon"));

    private static final String PM_PREFIX = "PmInst";
    private static final String HBPM_PREFIX = "HbPmInst";
    private static final String DRBD_PREFIX = "DrbdInst";

    /** Auto options for testing. */
    private static final String PM_AUTO_OPTION = "pminst";
    private static final String HBPM_AUTO_OPTION = "hbinst";
    private static final String DRBD_AUTO_OPTION = "drbdinst";

    /** Drbd icon: checking ... */
    private final JLabel drbdIcon = new JLabel(CHECKING_ICON);
    /** Corosync/openais/pacemaker icon: checking ... */
    private final JLabel pmIcon = new JLabel(CHECKING_ICON);
    /** Heartbeat/pacemaker icon: checking ... */
    private final JLabel hbPmIcon = new JLabel(CHECKING_ICON);
    /** Whether drbd installation was ok. */
    private boolean drbdOk = false;
    /** Whether corosync/openais/pacemaker installation was ok. */
    private boolean pmOk = false;
    /** Whether heartbeat/pacemaker installation was ok. */
    private boolean hbPmOk = false;
    /** Label of heartbeat that can be with or without pacemaker. */
    private final JLabel hbPmJLabel = new JLabel("Pcmk/Heartbeat");
    /** Label of pacemaker that can be with corosync or openais. */
    private final JLabel pmJLabel = new JLabel("Pcmk/Corosync");

    /** Prepares a new <code>CheckInstallation</code> object. */
    CheckInstallation(final WizardDialog previousDialog,
                      final Host host) {
        super(previousDialog, host);
    }

    /** Inits dialog. */
    @Override
    protected void initDialog() {
        super.initDialog();
        enableComponentsLater(new JComponent[]{});
    }

    /** Inits the dialog. */
    @Override
    protected void initDialogAfterVisible() {
        drbdOk = false;
        pmOk = false;
        hbPmOk = false;

        nextDialogObject = new Finish(this, getHost());
        final CheckInstallation thisClass = this;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                drbdButton.setBackgroundColor(
                                 Tools.getDefaultColor("ConfigDialog.Button"));
                drbdButton.setEnabled(false);
                drbdInstMethodWi.setEnabled(false);
                pmButton.setEnabled(false);
                pmInstMethodWi.setEnabled(false);
                hbPmButton.setEnabled(false);
                hbPmInstMethodWi.setEnabled(false);
            }
        });
        drbdButton.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (drbdOk) {
                        getHost().setDrbdWillBeUpgraded(true);
                    }
                    final InstallMethods im =
                                  (InstallMethods) drbdInstMethodWi.getValue();
                    getHost().setDrbdInstallMethod(im.getIndex());
                    final String button = e.getActionCommand();
                    if (!drbdOk || button.equals(Tools.getString(
                  "Dialog.Host.CheckInstallation.DrbdCheckForUpgradeButton"))) {
                        if (im.isLinbitMethod()) {
                            nextDialogObject =
                                new DrbdLinbitAvailPackages(thisClass,
                                                            getHost());
                        } else if (im.isSourceMethod()) {
                           nextDialogObject =
                               new DrbdAvailSourceFiles(thisClass, getHost());
                        } else {
                            // TODO: this only when there is no drbd installed
                            nextDialogObject = new DrbdCommandInst(
                                                        thisClass, getHost());
                            getHost().setDrbdInstallMethod(im.getIndex());
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                buttonClass(nextButton()).pressButton();
                            }
                        });
                    } else {
                        nextDialogObject =
                                 new LinbitLogin(thisClass, getHost());
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                buttonClass(nextButton()).pressButton();
                            }
                        });
                    }
                }
            }
        );

        hbPmButton.setBackgroundColor(
                               Tools.getDefaultColor("ConfigDialog.Button"));
        hbPmButton.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    nextDialogObject = new HeartbeatInst(thisClass, getHost());
                    final InstallMethods im =
                                   (InstallMethods) hbPmInstMethodWi.getValue();
                    getHost().setHbPmInstallMethod(im.getIndex());
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            buttonClass(nextButton()).pressButton();
                        }
                    });
                }
            }
        );

        pmButton.setBackgroundColor(
                               Tools.getDefaultColor("ConfigDialog.Button"));
        pmButton.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    nextDialogObject = new PacemakerInst(thisClass, getHost());
                    final InstallMethods im =
                                (InstallMethods) pmInstMethodWi.getValue();
                    getHost().setPmInstallMethod(im.getIndex());
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            buttonClass(nextButton()).pressButton();
                        }
                    });
                }
            }
        );

        getHost().execCommand("DrbdCheck.version",
                         getProgressBar(),
                         new ExecCallback() {
                             @Override
                             public void done(final String ans) {
                                 checkDrbd(ans);
                             }
                             @Override
                             public void doneError(
                                                         final String ans,
                                                         final int exitCode) {
                                 checkDrbd(""); // not installed
                             }
                         },
                         null,   /* ConvertCmdCallback */
                         false,  /* outputVisible */
                         SSH.DEFAULT_COMMAND_TIMEOUT);
    }

    /**
     * Checks whether drbd is installed and starts heartbeat/pacemaker check.
     */
    void checkDrbd(final String ans) {
        if ("".equals(ans) || "\n".equals(ans)) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    drbdLabel.setText(": " + Tools.getString(
                            "Dialog.Host.CheckInstallation.DrbdNotInstalled"));
                    drbdIcon.setIcon(NOT_INSTALLED_ICON);
                    final String toolTip = getInstToolTip(DRBD_PREFIX, "1");
                    drbdInstMethodWi.setToolTipText(toolTip);
                    drbdButton.setToolTipText(toolTip);
                    drbdButton.setEnabled(true);
                    drbdInstMethodWi.setEnabled(true);
                }
            });
        } else {
            drbdOk = true;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    drbdLabel.setText(": " + ans.trim());
                    if (getHost().isDrbdUpgradeAvailable(ans.trim())) {
                        drbdIcon.setIcon(UPGR_AVAIL_ICON);
                        drbdButton.setText(Tools.getString(
                          "Dialog.Host.CheckInstallation.DrbdUpgradeButton"));
                        drbdButton.setEnabled(true);
                        drbdInstMethodWi.setEnabled(true);
                    } else {
                        drbdButton.setText(Tools.getString(
                     "Dialog.Host.CheckInstallation.DrbdCheckForUpgradeButton"
                        ));
                        if (false) {
                            // TODO: disabled
                            drbdButton.setEnabled(true);
                            drbdInstMethodWi.setEnabled(true);
                        }
                        drbdIcon.setIcon(INSTALLED_ICON);
                    }
                }
            });
        }
        getHost().execCommand("HbCheck.version",
                         getProgressBar(),
                         new ExecCallback() {
                             @Override
                             public void done(final String ans) {
                                 Tools.debug(this, "ans: " + ans, 2);
                                 checkAisHbPm(ans);
                             }
                             @Override
                             public void doneError(final String ans,
                                                   final int exitCode) {
                                 done("");
                             }
                         },
                         null,   /* ConvertCmdCallback */
                         false,
                         SSH.DEFAULT_COMMAND_TIMEOUT); /* outputVisible */
    }

    /**
     * Checks whether heartbeat/pacemaker is installed and starts
     * openais/pacemaker check.
     */
    void checkAisHbPm(final String ans) {
        getHost().setPacemakerVersion(null);
        getHost().setOpenaisVersion(null);
        getHost().setHeartbeatVersion(null);
        getHost().setCorosyncVersion(null);
        if (!"".equals(ans) && !"\n".equals(ans)) {
            for (final String line : ans.split("\n")) {
                getHost().parseInstallationInfo(line);
            }
        }
        final String aisVersion = getHost().getOpenaisVersion();
        final String corosyncVersion = getHost().getCorosyncVersion();
        String hbVersion = getHost().getHeartbeatVersion();
        if (hbVersion == null
            && (getHost().getPacemakerVersion() == null
                || (corosyncVersion == null && aisVersion == null))) {
            final InstallMethods hbim =
                                  (InstallMethods) hbPmInstMethodWi.getValue();
            if (hbim != null) {
                hbPmButton.setEnabled(true);
                hbPmInstMethodWi.setEnabled(true);
                final String toolTip =
                                  getInstToolTip(HBPM_PREFIX, hbim.getIndex());
                hbPmInstMethodWi.setToolTipText(toolTip);
                hbPmButton.setToolTipText(toolTip);
            }
            final InstallMethods pmim =
                                    (InstallMethods) pmInstMethodWi.getValue();
            if (pmim != null) {
                pmButton.setEnabled(true);
                pmInstMethodWi.setEnabled(true);
                final String aisToolTip =
                                  getInstToolTip(PM_PREFIX, pmim.getIndex());
                pmInstMethodWi.setToolTipText(aisToolTip);
                pmButton.setToolTipText(aisToolTip);
            }
        }
        if (hbVersion == null) {
            /* hb */
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    hbPmIcon.setIcon(NOT_INSTALLED_ICON);
                    hbPmLabel.setText(": " + Tools.getString(
                             "Dialog.Host.CheckInstallation.HbPmNotInstalled"));
                }
            });
        } else {
            hbPmOk = true;
            String text;
            if ("2.1.3".equals(hbVersion)
                && "sles10".equals(getHost().getDistVersion())) {
                /* sles10 heartbeat 2.1.3 looks like hb 2.1.4 */
                hbVersion = "2.1.4";
                text = "2.1.3 (2.1.4)";
            } else {
                text = hbVersion;
            }
            final String hbVersionText = text;
            getHost().setHeartbeatVersion(hbVersion);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (getHost().getPacemakerVersion() == null
                        || getHost().getHeartbeatVersion().equals(
                                            getHost().getPacemakerVersion())) {
                        hbPmJLabel.setText("Heartbeat");
                        hbPmLabel.setText(": " + hbVersionText);
                    } else {
                        hbPmLabel.setText(": "
                                          + getHost().getPacemakerVersion()
                                          + "/" + hbVersionText);
                    }
                    hbPmIcon.setIcon(INSTALLED_ICON);
                }
            });
        }
        if (getHost().getPacemakerVersion() == null
            || (aisVersion == null && corosyncVersion == null)) {
            /* corosync */
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    pmIcon.setIcon(NOT_INSTALLED_ICON);
                    pmLabel.setText(": " + Tools.getString(
                            "Dialog.Host.CheckInstallation.PmNotInstalled"));
                    pmJLabel.setText("Pcmk/Corosync");
                }
            });
        } else {
            pmOk = true;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    String coroAisVersion = "no";
                    pmIcon.setIcon(INSTALLED_ICON);
                    if (corosyncVersion != null) {
                        pmJLabel.setText("Pcmk/Corosync");
                        coroAisVersion = corosyncVersion;
                    } else if (aisVersion != null) {
                        pmJLabel.setText("Pcmk/AIS");
                        coroAisVersion = aisVersion;
                    }
                    pmJLabel.repaint();
                    pmLabel.setText(": "
                                       + getHost().getPacemakerVersion() + "/"
                                       + coroAisVersion);
                }
            });
        }

        if (drbdOk && (hbPmOk || pmOk)) {
            progressBarDone();
            nextButtonSetEnabled(true);
            enableComponents();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    answerPaneSetText(Tools.getString(
                                       "Dialog.Host.CheckInstallation.AllOk"));
                }
            });
            if (Tools.getConfigData().getAutoOptionHost("drbdinst") != null
                || Tools.getConfigData().getAutoOptionHost("hbinst") != null
                || Tools.getConfigData().getAutoOptionHost("pminst") != null) {
                Tools.sleep(1000);
                pressNextButton();
            }
        } else {
            progressBarDoneError();
            Tools.debug(this, "drbd: " + drbdOk
                              + ", ais/pm: " + pmOk
                              + ", hb/pm: " + hbPmOk, 2);
            printErrorAndRetry(Tools.getString(
                                "Dialog.Host.CheckInstallation.SomeFailed"));
        }
        if (!drbdOk
            && Tools.getConfigData().getAutoOptionHost("drbdinst") != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Tools.sleep(1000);
                    drbdButton.pressButton();
                }
            });
        } else if (!hbPmOk
            && Tools.getConfigData().getAutoOptionHost("hbinst") != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Tools.sleep(1000);
                    hbPmButton.pressButton();
                }
            });
        } else if (!pmOk
            && Tools.getConfigData().getAutoOptionHost("pminst") != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Tools.sleep(1000);
                    pmButton.pressButton();
                }
            });
        }
    }

    /** Returns the next dialog object. It is set dynamicaly. */
    @Override
    public WizardDialog nextDialog() {
        return nextDialogObject;
    }

    /**
     * Returns the title of the dialog. It is defined as
     * Dialog.Host.CheckInstallation.Title in TextResources.
     */
    @Override
    protected String getHostDialogTitle() {
        return Tools.getString("Dialog.Host.CheckInstallation.Title");
    }

    /**
     * Returns the description of the dialog. It is defined as
     * Dialog.Host.CheckInstallation.Description in TextResources.
     */
    @Override
    protected String getDescription() {
        return Tools.getString("Dialog.Host.CheckInstallation.Description");
    }

    /**
     * Returns the pane, that checks the installation of different
     * components and provides buttons to update or upgrade.
     */
    private JPanel getInstallationPane() {
        final JPanel pane = new JPanel(new SpringLayout());
        pmInstMethodWi = getInstallationMethods(
                            PM_PREFIX,
                            Tools.getConfigData().isStagingPacemaker(),
                            Tools.getConfigData().getLastHbPmInstalledMethod(),
                            PM_AUTO_OPTION,
                            pmButton);

        hbPmInstMethodWi = getInstallationMethods(
                            HBPM_PREFIX,
                            Tools.getConfigData().isStagingPacemaker(),
                            Tools.getConfigData().getLastHbPmInstalledMethod(),
                            HBPM_AUTO_OPTION,
                            hbPmButton);
        drbdInstMethodWi = getInstallationMethods(
                            DRBD_PREFIX,
                            Tools.getConfigData().isStagingDrbd(),
                            Tools.getConfigData().getLastDrbdInstalledMethod(),
                            DRBD_AUTO_OPTION,
                            drbdButton);
        final String lastInstalled =
                          Tools.getConfigData().getLastInstalledClusterStack();
        if (lastInstalled != null) {
            if (ConfigData.HEARTBEAT_NAME.equals(lastInstalled)) {
                pmJLabel.setForeground(Color.LIGHT_GRAY);
                pmLabel.setForeground(Color.LIGHT_GRAY);
            } else if (ConfigData.COROSYNC_NAME.equals(lastInstalled)) {
                hbPmJLabel.setForeground(Color.LIGHT_GRAY);
                hbPmLabel.setForeground(Color.LIGHT_GRAY);
            }
        }
        pane.add(hbPmJLabel);
        pane.add(hbPmLabel);
        pane.add(hbPmIcon);
        pane.add(hbPmInstMethodWi);
        pane.add(hbPmButton);
        pane.add(pmJLabel);
        pane.add(pmLabel);
        pane.add(pmIcon);
        pane.add(pmInstMethodWi);
        pane.add(pmButton);
        pane.add(new JLabel("Drbd"));
        pane.add(drbdLabel);
        pane.add(drbdIcon);
        pane.add(drbdInstMethodWi);
        pane.add(drbdButton);

        SpringUtilities.makeCompactGrid(pane, 3, 5,  //rows, cols
                                              1, 1,  //initX, initY
                                              1, 1); //xPad, yPad
        return pane;
    }

    /** Returns input pane with installation pane and answer pane. */
    @Override
    protected JComponent getInputPane() {
        final JPanel pane = new JPanel(new SpringLayout());
        pane.add(getInstallationPane());
        pane.add(getProgressBarPane());
        pane.add(getAnswerPane(Tools.getString(
                                    "Dialog.Host.CheckInstallation.Checking")));
        SpringUtilities.makeCompactGrid(pane, 3, 1,  //rows, cols
                                              0, 0,  //initX, initY
                                              0, 0); //xPad, yPad

        return pane;
    }

    /** Enable skip button. */
    @Override
    protected boolean skipButtonEnabled() {
        return true;
    }
}
