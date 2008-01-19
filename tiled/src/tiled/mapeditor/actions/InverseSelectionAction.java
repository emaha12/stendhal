/*
 *  Tiled Map Editor, (c) 2004
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjorn Lindeijer <b.lindeijer@xs4all.nl>
 *  
 *  modified for Stendhal, an Arianne powered RPG 
 *  (http://arianne.sf.net)
 *
 *  Matthias Totz &lt;mtotz@users.sourceforge.net&gt;
 */

package tiled.mapeditor.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import tiled.mapeditor.MapEditor;

/**
 * Inverses the current selection.
 * 
 * @author mtotz
 */
public class InverseSelectionAction extends AbstractAction {
	private static final long serialVersionUID = -3030827051213056224L;

	private MapEditor mapEditor;

	public InverseSelectionAction(MapEditor mapEditor) {
		super("Invert");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control I"));
		putValue(SHORT_DESCRIPTION, "Inverse of the current selection");
		this.mapEditor = mapEditor;
	}

	public void actionPerformed(ActionEvent e) {
	}
}