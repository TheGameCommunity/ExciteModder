package com.gamebuster19901.excite.modding.util;

import java.io.IOException;
import java.io.OutputStream;

public class SplitOutputStream extends OutputStream {

	private OutputStream[] outputs;
	
	public SplitOutputStream(OutputStream... consumers) {
		this.outputs = consumers;
	}
	
	public static SplitOutputStream splitSysOut(OutputStream... consumers) {
		OutputStream original = System.out;
		OutputStream[] outputs = new OutputStream[consumers.length + 1];
		outputs[0] = original;
		
		for(int i = 0; i < consumers.length; i++) {
			outputs[i + 1] = consumers[i];
		}
		
		return new SplitOutputStream(outputs);
	}
	
	public static SplitOutputStream splitErrOut(OutputStream... consumers) {
		OutputStream original = System.err;
		OutputStream[] outputs = new OutputStream[consumers.length + 1];
		outputs[0] = original;
		
		for(int i = 0; i < consumers.length; i++) {
			outputs[i + 1] = consumers[i];
		}
		
		return new SplitOutputStream(outputs);
	}
	
	@Override
	public void write(int b) throws IOException {
		for(OutputStream o : outputs) {
			o.write(b);
		}
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		for(OutputStream o : outputs) {
			o.write(b);
		}
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		for(OutputStream o : outputs) {
			o.write(b, off, len);
		}
	}

	@Override
	public void flush() throws IOException{
		for(OutputStream o : outputs) {
			o.flush();
		}
	}
	
	@Override
	public void close() throws IOException {
		for(OutputStream o : outputs) {
			o.close();
		}
	}
	
}
