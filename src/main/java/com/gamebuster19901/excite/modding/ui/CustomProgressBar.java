package com.gamebuster19901.excite.modding.ui;

import javax.swing.JProgressBar;

public class CustomProgressBar extends JProgressBar {

	@Override
	public String getString() {
		if(this.getPercentComplete() <= 0d) {
			return "Not Started";
		}
		if(this.getPercentComplete() < 1d) {
			return "Progress: " + ((int)(this.getPercentComplete() * 100)) + "%";
		}
		return "Complete";
	}
	
}
