package com.gamebuster19901.excite.modding.ui;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class JTextAreaOutputStream extends OutputStream {

	private final JTextArea dest;
	
	public JTextAreaOutputStream(JTextArea dest) {
		this.dest = dest;
	}
	
	@Override
	public void write(int b) throws IOException {
		write(new byte[]{(byte)b}, 0, 1);
	}
	
	@Override
	public void write(byte[] buf, int off, int len) throws IOException {
		String text = new String(buf, off, len);
		SwingUtilities.invokeLater(() -> {
			dest.append(text);
		});
	}
	
}
