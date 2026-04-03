package com.gamebuster19901.excite.modding.ui;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTabbedPane;

/**
 * A custom tabbed pane component that extends {@link javax.swing.JTabbedPane} and provides additional functionalities for managing tabs.
 * 
 * This class offers a convenience layer on top of the standard `JTabbedPane` by:
 *	* Encapsulating tab information within a {@link Tab} record.
 *	* Providing methods for adding and retrieving tabs using `Tab` objects.
 *	* Propagating `IndexOutOfBoundsException` for methods that interact with tabs to ensure consistent error handling.
 * 
 * @see javax.swing.JTabbedPane
 * @see Tab
 */
public class EJTabbedPane extends JTabbedPane {

	/**
	 * Constructs a new EJTabbedPane with the default tab placement (TOP).
	 */
	public EJTabbedPane() {
		super();
	}

	/**
	 * Constructs a new EJTabbedPane with the specified tab placement.
	 * 
	 * @param tabPlacement The placement of the tabs. This can be one of the following values from {@link javax.swing.JTabbedPane}:
	 *	* {@link JTabbedPane#TOP}
	 *	* {@link JTabbedPane#BOTTOM}
	 *	* {@link JTabbedPane#LEFT}
	 *	* {@link JTabbedPane#RIGHT}
	 * @throws IllegalArgumentException if the tabPlacement is not a valid value.
	 */
	public EJTabbedPane(int tabPlacement) throws IllegalArgumentException {
		super(tabPlacement);
	}

	/**
	 * Constructs a new EJTabbedPane with the specified tab placement and layout policy.
	 * 
	 * @param tabPlacement The placement of the tabs. Refer to {@link #EJTabbedPane(int)} for valid values.
	 * @param tabLayoutPolicy The layout policy for the tabs. This can be one of the following values from {@link javax.swing.JTabbedPane}:
	 *	* {@link JTabbedPane#WRAP_TAB_LAYOUT}
	 *	* {@link JTabbedPane#SCROLL_TAB_LAYOUT}
	 * @throws IllegalArgumentException if the tabPlacement or tabLayoutPolicy is not a valid value.
	 */
	public EJTabbedPane(int tabPlacement, int tabLayoutPolicy) throws IllegalArgumentException {
		super(tabPlacement, tabLayoutPolicy);
	}

	/**
	 * Adds a new tab to the EJTabbedPane.
	 * 
	 * This method extracts the title and component from the provided {@link Tab} object and calls the underlying `JTabbedPane` method to add the tab.
	 * 
	 * @param tab The tab object containing the information for the new tab.
	 * @return The same `tab` object that was provided as input.
	 */
	public Tab addTab(Tab tab) {
		this.addTab(tab.title, tab.icon, tab.component, tab.tip);
		return tab;
	}

	/**
	 * Inserts a new tab at the specified index within the EJTabbedPane.
	 * 
	 * This method uses the information from the provided {@link Tab} object (title, icon, component, tooltip) to call the underlying `JTabbedPane` method for insertion.
	 * 
	 * @param tab The tab object containing the information for the new tab.
	 * @param index The index at which to insert the tab.
	 * @return The same `tab` object that was provided as input.
	 * @throws IndexOutOfBoundsException if the index is invalid.
	 */
	public Tab putTab(Tab tab, int index) throws IndexOutOfBoundsException {
		this.insertTab(tab.title, tab.icon, tab.component, tab.tip, index);
		return tab;
	}

	/**
	 * Retrieves a {@link Tab} object from the EJTabbedPane by its title.
	 * 
	 * This method first finds the index of the tab with the matching title and then calls the `getTab(int index)` method.
	 * 
	 * @param title The title of the tab to retrieve.
	 * @return A `Tab` object representing the tab with the specified title, or null if the tab is not found.
	 */
	public Tab getTab(String title) throws IndexOutOfBoundsException {
		int index = this.indexOfTab(title);
		return getTab(index);
	}

	/**
	 * Retrieves a {@link Tab} object from the EJTabbedPane by its index.
	 * 
	 * This method extracts the title, icon, component, and tooltip from the underlying `JTabbedPane` at the specified index and creates a new `Tab` object with this information.
	 * 
	 * @param index The index of the tab to retrieve.
	 * @return A `Tab` object representing the tab at the specified index, or null if the index is invalid.
	 */
	public Tab getTab(int index) {
		if(index < 0 || index > this.getComponentCount()) {
			return null;
		}
		String title = this.getTitleAt(index);
		Icon icon = this.getIconAt(index);
		Component component = this.getComponentAt(index);
		String tip = this.getToolTipTextAt(index);
		return new Tab(title, icon, component, tip);
	}
	
	public Tab getSelectedTab() {
		return getTab(getSelectedIndex());
	}
	
	public int getIndex(Tab tab) {
		if(tab.title != null) {
			return this.indexOfTab(tab.title);
		}
		if(tab.icon != null) {
			return this.indexOfTab(tab.icon);
		}
		throw new IllegalArgumentException("Tab has null title and null icon, cannot obtain index");
	}
	
	public void setSelectedTab(Tab tab) {
		this.setSelectedIndex(getIndex(tab));
		this.invalidate();
	}
	
	@Override
	protected void fireStateChanged() {
		super.fireStateChanged();
	}

	/**
	 * A record that encapsulates information about a single tab.
	 */
	public static final record Tab(String title, Icon icon, Component component, String tip) {

		/**
		 * Constructs a new Tab object with the specified title and component.
		 * 
		 * @param title The title of the tab.
		 * @param component The component to be displayed in the tab.
		 */
		public Tab(String title, Component component) {
			this(title, null, component, null);
		}

		/**
		 * Constructs a new Tab object with the specified title, icon, and component.
		 * 
		 * @param title The title of the tab.
		 * @param icon The icon to be displayed on the tab.
		 * @param component The component to be displayed in the tab.
		 */
		public Tab(String title, Icon icon, Component component) {
			this(title, icon, component, null);
		}
	}
}

