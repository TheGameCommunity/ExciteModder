package com.gamebuster19901.excite.modding.unarchiver.concurrent;

public enum DecisionType {

	PROCEED, //Proceed with the process
	SKIP, //Don't proceed with the process, we know we don't know how to handle it
	IGNORE; //Don't proceed with the process, an unexpected exception occurred.
	
}
