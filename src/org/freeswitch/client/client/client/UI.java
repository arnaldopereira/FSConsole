package org.freeswitch.client.client.client;

import org.freeswitch.client.client.FSCommand;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class UI {
	private Widget rootWidget;
	private TextBox statusBox;
	private final MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
	private final SuggestBox suggestBox = new SuggestBox(oracle);
	private final HTML commandStatus = new HTML("");

	public UI() {
		/*
		 * 5: atualizar lista de comandos periodicamente 6: fazer
		 * polling dos logs, periodicamente 7: deixar xml com a configuração no
		 * servidor. o usuário poderá editar o xml manualmente. criar uma tab
		 * pra visualização da configuraçao atual.
		 */
		VerticalPanel vPanel = new VerticalPanel();
		vPanel.setVerticalAlignment(VerticalPanel.ALIGN_TOP);
		vPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);

		// head image
		vPanel.add(buildHeader());

		// add tab panel and status bar
		vPanel.add(buildContent());
		vPanel.add(buildStatusBox());

		rootWidget = vPanel;
		
		FSCommand.runCommand(suggestBox.getText(), new HandleCommandResponse());
		FSCommand.eventRequestListener();
	}

	public Widget getWidget() {
		return rootWidget;
	}

	public void setCommandStatus(String html) {
		commandStatus.setHTML(html);
	}
	
	public Integer getWindowWidth() {
		return Window.getClientWidth();
	}

	public Integer getWindowHeight() {
		return Window.getClientHeight();
	}

	public String getWindowWidthStr() {
		return Integer.toString(getWindowWidth());
	}

	public String getWindowHeightStr() {
		return Integer.toString(getWindowHeight());
	}

	public void setStatusText(String str) {
		this.statusBox.setText(str);
	}

	private Widget buildHeader() {
		return new HTML("header");
	}

	/**
	 * Callback class to handle the response of the commands. On success, set
	 * statusHtml with the command output. On failure, shows a message.
	 */
	private class HandleCommandResponse implements AsyncCallback<String> {
		public void onSuccess(String response) {
			setCommandStatus("<pre>" + response + "</pre>");
		}

		public void onFailure(Throwable response) {
			setStatusText("ERROR: " + response.getMessage());
		}
	}

	/**
	 * Callback class to handle the response of a specific command, which
	 * retrieves the list of available fsapi commands and add them to the
	 * suggestion box.
	 * 
	 * On success, add the command and its arguments to the oracle's wordlist.
	 * On failure, shows a message box.
	 */
	private class HandleCompleteResponse implements AsyncCallback<String> {
		public void onSuccess(String response) {
			String[] resLines = response.split("\n");

			/* 'ugly' would be a nickname for this parsing routine. */
			for (int i = 0; i < resLines.length; i++) {
				String[] completion = resLines[i].split(",");
				String wholeCmd = completion[1];

				if (completion[1] == "sticky")
					continue;

				for (int j = 2; j <= (completion.length - 1); j++)
					wholeCmd += " " + completion[j];

				if (wholeCmd.length() == 0)
					continue;

				oracle.add(wholeCmd);
			}
		}

		public void onFailure(Throwable response) {
			Window.alert("Failed to get autocomplete list. Error: "	+ response.getMessage());
		}
	}

	private class HandleEventSink implements AsyncCallback<String> {
		public void onSuccess(String response) {
			Window.alert("response: " + response);
			/*
			String[] resLines = response.split("\n");

			for (int i = 0; i < resLines.length; i++) {
				String[] completion = resLines[i].split(",");
				String wholeCmd = completion[1];

				if (completion[1] == "sticky")
					continue;

				for (int j = 2; j <= (completion.length - 1); j++)
					wholeCmd += " " + completion[j];

				if (wholeCmd.length() == 0)
					continue;

				oracle.add(wholeCmd);
			}
			*/
		}

		public void onFailure(Throwable response) {
			Window.alert("Failed to get autocomplete list. Error: "
					+ response.getMessage());
		}
	}

	private Widget buildCommandContent() {
		final VerticalPanel vPanel = new VerticalPanel();
		final HorizontalPanel hPanel = new HorizontalPanel();
		final Button sendButton = new Button();

		// suggest box
		suggestBox.setSize(Double.toString(getWindowWidth() * .4), "25");
		HorizontalPanel suggestPanel = new HorizontalPanel();
		suggestPanel.setSpacing(10);
		suggestPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		suggestPanel.add(new HTML("<b>Enter command:</b>"));
		suggestPanel.add(suggestBox);
		suggestPanel.add(sendButton);

		/**
		 * When ENTER is pressed, sends the fsapi command to FreeSWITCH. ESCAPE
		 * should clean the command input field.
		 */
		suggestBox.addKeyboardListener(new KeyboardListenerAdapter() {
			@Override
			public void onKeyDown(Widget sender, char keyCode, int modifiers) {
				if (keyCode == KEY_ENTER) {
					FSCommand.runCommand(suggestBox.getText(),
							new HandleCommandResponse());
				} else if (keyCode == KEY_ESCAPE) {
					suggestBox.setText("");
				}
			}
		});
		
		// configure command feedback area
		commandStatus.setStyleName("status-area");
		commandStatus.setSize(Double.toString(getWindowWidth() * .9), Double.toString(getWindowHeight() * .6));

		// setup vpanel
		vPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
		vPanel.setSpacing(10);
		hPanel.setSpacing(10);
		vPanel.add(commandStatus);
		vPanel.add(hPanel);

		hPanel.add(suggestPanel);
		sendButton.setText("Send");

		/**
		 * When the user clicks on the SEND button, guess what?
		 */
		sendButton.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				FSCommand.runCommand(suggestBox.getText(),
						new HandleCommandResponse());
			}
		});

		// fills autocomplete list
		FSCommand.runCommand("show complete", new HandleCompleteResponse());

		return (Widget) vPanel;
	}

	private Widget buildContent() {
		final DecoratedTabPanel tabPanel = new DecoratedTabPanel();
		tabPanel.setWidth(getWindowWidthStr() + "px");
		tabPanel.setAnimationEnabled(true);

		// command tab
		tabPanel.add(buildCommandContent(), new HTML("command"));
		tabPanel.selectTab(0);

		// log tab
		tabPanel.add(new HTML("Logs"), "Logs");

		return (Widget) tabPanel;
	}

	private Widget buildStatusBox() {
		this.statusBox = new TextBox();

		this.statusBox.setReadOnly(true);
		this.statusBox.setMaxLength(120);
		this.statusBox.setWidth(getWindowWidthStr());
		this.statusBox.setText("Up");

		return (Widget) this.statusBox;
	}
}
