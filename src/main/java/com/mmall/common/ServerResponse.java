package com.mmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * 高复用服务响应对象
 *  JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)意思是
 *  值为null时不要序列化，因为在生成错误对象时没有data，这时候就不要把data
 *  序列化进json了（有时候msg也为null）（保证序列化json的时候，如果是null的
 *  对象，key也会消失）
 * */
import java.io.Serializable;
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse<T> implements Serializable {
	private int status;
	private String msg;
	private T data;

	public ServerResponse(int status) {
		this.status = status;
	}

	public ServerResponse(int status, String msg, T data) {
		this.status = status;
		this.msg = msg;
		this.data = data;
	}

	public ServerResponse(int status, T data) {
		this.status = status;
		this.data = data;
	}

	public ServerResponse(int status, String msg) {
		this.status = status;
		this.msg = msg;
	}
	//让JSON序列化忽视
	@JsonIgnore
	public boolean isSuccess(){
		return this.status == ResponseCode.SUCCESSE.getCode();
	}

	public int getStatus() {
		return status;
	}

	public String getMsg() {
		return msg;
	}

	public T getData() {
		return data;
	}

	public static <T> ServerResponse<T> createBySuccess(){
		return new ServerResponse<T>(ResponseCode.SUCCESSE.getCode());
	}

	public static <T> ServerResponse<T> createBySuccessMessage(String msg){
		return new ServerResponse<T>(ResponseCode.SUCCESSE.getCode(),msg);
	}

	public static <T> ServerResponse<T> createBySuccess(T data){
		return new ServerResponse<T>(ResponseCode.SUCCESSE.getCode(),data);
	}

	public static <T> ServerResponse<T> createBySuccess(String msg,T data){
		return new ServerResponse<T>(ResponseCode.SUCCESSE.getCode(),msg,data);
	}

	public static <T> ServerResponse<T> createByError(){
		return new ServerResponse<T>(ResponseCode.ERROR.getCode(),
				ResponseCode.ERROR.getDesc());
	}

	public static <T> ServerResponse<T> createByErrorMessage(String errorMessage){
		return new ServerResponse<>(ResponseCode.ERROR.getCode(),errorMessage);
	}

	public static <T> ServerResponse<T> createByErrorCodeMessage(int errorCode,String errorMessage){
		return new ServerResponse<>(errorCode,errorMessage);
	}
}
