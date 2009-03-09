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


package drbd;

import drbd.utilities.Tools;

import drbd.gui.dialog.WizardDialog;
import drbd.gui.dialog.HostDistForUpgrade;
import drbd.gui.HostBrowser.HostInfo;

/**
 * AddDrbdUpgradeDialog.
 *
 * Show step by step dialogs that upgrades the drbd.
 *
 * @author Rasto Levrinc
 * @version $Id$
 */
public class AddDrbdUpgradeDialog {
    /** Whether the wizard was canceled. */
    private boolean canceled = false;
    /** Host on which the drbd will be upgraded. */
    private final HostInfo hostInfo;

    /**
     * Prepares a new <code>AddDrbdUpgradeDialog</code> object.
     */
    public AddDrbdUpgradeDialog(final HostInfo hostInfo) {
        this.hostInfo = hostInfo;
    }

    /**
     * Shows step by step dialogs that upgrade the drbd.
     */
    public void showDialogs() {
        WizardDialog dialog = new HostDistForUpgrade(null, hostInfo.getHost());
        while (true) {
            final WizardDialog newdialog = (WizardDialog) dialog.showDialog();
            if (dialog.isPressedCancelButton()) {
                dialog.cancelDialog();
                canceled = true;
                return;
            } else if (dialog.isPressedFinishButton()) {
                break;
            }
            dialog = newdialog;
        }
        Tools.getGUIData().getMainFrame().requestFocus();
    }

    /**
     * Returns whether the wizard was canceled.
     */
    public boolean isCanceled() {
        return canceled;
    }
}