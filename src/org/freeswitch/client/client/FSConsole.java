package org.freeswitch.client.client;

import org.freeswitch.client.client.client.UI;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public class FSConsole implements EntryPoint {

	private UI ui = new UI();

	public void onModuleLoad() {
		RootPanel.get().add(ui.getWidget());
		ui.setStatusText("Welcome.");
	}

}
