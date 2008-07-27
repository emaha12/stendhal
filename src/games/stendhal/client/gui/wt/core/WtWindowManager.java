/* $Id$ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.wt.core;

import games.stendhal.client.stendhal;
import games.stendhal.client.gui.ManagedWindow;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import marauroa.common.io.Persistence;

import org.apache.log4j.Logger;

/**
 * This manager keeps track of all the windows and their positions/ minimized
 * state.
 * 
 * @author mtotz
 */
public class WtWindowManager {

	/** the logger instance. */
	private static final Logger logger = Logger
			.getLogger(WtWindowManager.class);

	/** filename for the settings persistence. */
	private static final String FILE_NAME = "windows.properties";

	/** the saved window positions. */
	private Properties properties;

	/** the instance. */
	private static WtWindowManager instance;

	/** maps the window names to their configs. */
	private final Map<String, WindowConfiguration> configs = new HashMap<String, WindowConfiguration>();

	/** no public constructor. */
	private WtWindowManager() {
		// try to read the configurations from disk
		read();
	}

	/** @return the windowmanagers instance. */
	public static WtWindowManager getInstance() {
		if (instance == null) {
			instance = new WtWindowManager();
		}
		return instance;
	}

	/**
	 * Sets default window properties. These are used only when there are no
	 * properties known for this panel.
	 * 
	 * @param name
	 * @param minimized
	 * @param x
	 * @param y
	 */
	public void setDefaultProperties(final String name, final boolean minimized, final int x,
			final int y) {
		if (!configs.containsKey(name)) {
			final WindowConfiguration config = new WindowConfiguration(name);
			config.readFromProperties(properties, minimized, x, y, true);
			configs.put(name, config);
		}
	}

	/** saves the current settings to a file. */
	public void save() {
		final StringBuilder buf = new StringBuilder();
		for (final WindowConfiguration config : configs.values()) {
			buf.append(config.writeToPropertyString());
		}
		for (final Object key : properties.keySet()) {
			if (key.toString().startsWith("config.")) {
				buf.append(key.toString() + "=" + properties.get(key) + "\n");
			}
		}

		try {
			final OutputStream os = Persistence.get().getOutputStream(true,
					"stendhal", FILE_NAME);
			final OutputStreamWriter writer = new OutputStreamWriter(os);
			writer.append(buf.toString());
			writer.close();
		} catch (final IOException e) {
			// ignore exception
			logger.error("Can't write " + stendhal.STENDHAL_FOLDER + FILE_NAME,
					e);
		}
	}

	/** Reads the current settings from a file. */
	public void read() {
		properties = new Properties();
		try {
			final InputStream is = Persistence.get().getInputStream(true, "stendhal",
					FILE_NAME);
			properties.load(is);
			is.close();
		} catch (final IOException e) {
			// ignore exception
		}
	}

	/**
	 * @param panel
	 * @return the config. If it does not exist yet, a new one is created.
	 */
	private WindowConfiguration getConfig(final ManagedWindow panel) {
		final String name = panel.getName();
		WindowConfiguration winC = configs.get(name);
		if (winC == null) {
			winC = new WindowConfiguration(name);
			winC.readFromProperties(properties, panel);
			configs.put(name, winC);
		}
		return winC;
	}

	/**
	 * Returns a property.
	 * 
	 * @param key
	 *            Key to look up
	 * @param defaultValue
	 *            default value which is returned if the key is not in the
	 *            configuration file
	 * @return value
	 */
	// Hack: enables other parts of the program to read from this configuration
	// file
	public String getProperty(final String key, final String defaultValue) {
		return properties.getProperty("config." + key, defaultValue);
	}

	/**
	 * Returns a property.
	 * 
	 * @param key
	 *            Key to look up
	 * @param defaultValue
	 *            default value which is returned if the key is not in the
	 *            configuration file
	 * @return value
	 */
	// Hack: enables other parts of the program to read from this configuration
	// file
	public String setProperty(final String key, final String defaultValue) {
		return properties.getProperty("config." + key, defaultValue);
	}

	/**
	 * Formats the window with the saved config. Nothing happens when this
	 * windows config is not known.
	 * 
	 * @param panel
	 */
	public void formatWindow(final ManagedWindow panel) {
		final WindowConfiguration config = getConfig(panel);
		if (config == null) {
			// window not supervised
			return;
		}

		panel.moveTo(config.x, config.y);
		panel.setMinimized(config.minimized);
		panel.setVisible(config.visible);
	}

	/**
	 * the panel was moved, so update the internal representation.
	 * 
	 * @param panel
	 * @param x
	 * @param y
	 */
	public void moveTo(final ManagedWindow panel, final int x, final int y) {
		final WindowConfiguration config = getConfig(panel);
		config.x = x;
		config.y = y;
	}

	/**
	 * the panels minimized state changed, update the internal representation.
	 * 
	 * @param panel
	 * @param state
	 */
	public void setMinimized(final ManagedWindow panel, final boolean state) {
		final WindowConfiguration config = getConfig(panel);

		config.minimized = state;
	}

	public void setVisible(final ManagedWindow panel, final boolean state) {
		final WindowConfiguration config = getConfig(panel);

		config.visible = state;
	}

	/** encapsulates the configuration of a window. */
	private class WindowConfiguration {

		/** name of the window. */
		public String name;

		/** minimized state of the window. */
		public boolean minimized;

		/** is the window visible? */
		public boolean visible;

		/** x-pos. */
		public int x;

		/** y-pos. */
		public int y;

		public WindowConfiguration(final String name) {
			this.name = name;
		}

		/**
		 * @return string to be stored as property
		 */
		public String writeToPropertyString() {
			return "window." + name + ".minimized=" + minimized + "\n"
					+ "window." + name + ".visible=" + visible + "\n"
					+ "window." + name + ".x=" + x + "\n" + "window." + name
					+ ".y=" + y + "\n";
		}

		/** returns to config as a property string. */
		@Override
		public String toString() {
			return writeToPropertyString();
		}

		/**
		 * adds all props to the property.
		 * 
		 * @param props
		 */
		public void writeToProperties(final Properties props) {
			props.put("window." + name + ".minimized", minimized);
			props.put("window." + name + ".visible", visible);
			props.put("window." + name + ".x", x);
			props.put("window." + name + ".y", y);
		}

		/**
		 * reads the config from the properties.
		 * 
		 * @param props
		 * @param defaultMinimized
		 * @param defaultX
		 * @param defaultY
		 * @param defaultVisible
		 */
		public void readFromProperties(final Properties props,
				final boolean defaultMinimized, final int defaultX, final int defaultY,
				final boolean defaultVisible) {
			minimized = Boolean.parseBoolean(props.getProperty("window." + name
					+ ".minimized", Boolean.toString(minimized)));
			visible = Boolean.parseBoolean(props.getProperty("window." + name
					+ ".visible", Boolean.toString(defaultVisible)));
			x = Integer.parseInt(props.getProperty("window." + name + ".x",
					Integer.toString(defaultX)));
			y = Integer.parseInt(props.getProperty("window." + name + ".y",
					Integer.toString(defaultY)));
		}

		/**
		 * reads the config from the properties.
		 * 
		 * @param props
		 * @param defaults
		 */
		public void readFromProperties(final Properties props, final ManagedWindow defaults) {
			readFromProperties(props, defaults.isMinimized(), defaults.getX(),
					defaults.getY(), defaults.isVisible());
		}

	}

}
