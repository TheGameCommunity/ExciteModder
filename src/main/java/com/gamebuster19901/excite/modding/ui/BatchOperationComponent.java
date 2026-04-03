package com.gamebuster19901.excite.modding.ui;

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.gamebuster19901.excite.modding.concurrent.Batch.BatchedCallable;
import com.gamebuster19901.excite.modding.concurrent.BatchContainer;
import com.gamebuster19901.excite.modding.concurrent.BatchListener;

public class BatchOperationComponent<T> extends JPanel implements BatchContainer<T>, BatchListener {

	private BatchedImageComponent<T> image;
	private JLabel nameLabel;
	
	public BatchOperationComponent(BatchContainer<T> batch) {
		this(new BatchedImageComponent(batch));
		setName(batch.getName());
	}
	
	/**
	@wbp.parser.constructor
	**/
	public BatchOperationComponent(BatchedImageComponent<T> batch) {
		this.image = batch;
		setLayout(new BorderLayout(0, 0));
		
		add(batch, BorderLayout.CENTER);
		
		String name = batch.getName();
		
		nameLabel = new JLabel(name);
		nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		add(nameLabel, BorderLayout.SOUTH);
		this.setName(name);
	}
	
	@Override
	public void setName(String name) {
		super.setName(name);
		this.setToolTipText(name);
		this.nameLabel.setText(name);
	}

	@Override
	public void addBatchListener(BatchListener listener) {
		image.addBatchListener(listener);
	}

	@Override
	public void shutdownNow() throws InterruptedException {
		image.shutdownNow();
	}

	@Override
	public Collection<BatchedCallable<T>> getRunnables() {
		return image.getRunnables();
	}

	@Override
	public Collection<BatchListener> getListeners() {
		return image.getListeners();
	}

	@Override
	public void updateListeners() {
		image.updateListeners();
	}

	@Override
	public void update() {
		image.update();
		repaint();
	}
	
}
