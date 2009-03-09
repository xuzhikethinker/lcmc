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

import drbd.utilities.Tools;
import javax.swing.SpringLayout;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import drbd.gui.SpringUtilities;
import java.awt.Point;

/**
 * An implementation of an About dialog.
 *
 * @author Rasto Levrinc
 * @version $Id$
 */
public class About extends ConfigDialog {
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Inits the dialog and enables all the components.
     */
    protected final void initDialog() {
        super.initDialog();
        enableComponents();
    }

    /**
     * Gets the title of the dialog as string.
     */
    protected final String getDialogTitle() {
        return Tools.getString("Dialog.About.Title") + Tools.getRelease();
    }

    /**
     * Returns description for dialog. This can be HTML defined in
     * TextResource.
     */
    protected final String getDescription() {
        return Tools.getString("Dialog.About.Description");
    }

    /**
     * Returns the content of the about dialog.
     */
    protected final JComponent getInputPane() {
        final JPanel pane = new JPanel(new SpringLayout());
        final JScrollPane sp =
                      getAnswerPane(Tools.getString("Dialog.About.Licences"));

        pane.add(sp);
        //SwingUtilities.invokeLater(new Runnable() {
        //    public void run() {
        //        sp.getVerticalScrollBar().setValue(0);
        //    }
        //});
        SpringUtilities.makeCompactGrid(pane, 1, 1,  //rows, cols
                                              1, 1,  //initX, initY
                                              1, 1); //xPad, yPad
        return pane;
    }
}