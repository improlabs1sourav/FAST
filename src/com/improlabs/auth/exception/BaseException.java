package com.improlabs.auth.exception;

public class BaseException extends Exception{

	
	private String errorId;
	private String errorMessage;
	
	public BaseException(String errorId,String errorMessage)
	{
		super();
		this.errorId=errorId;
		this.errorMessage=errorMessage;
	}
}
