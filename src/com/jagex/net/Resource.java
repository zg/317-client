package com.jagex.net;

import com.jagex.link.Cacheable;

public class Resource extends Cacheable {

	private int age;

	private byte[] data;
	private int file;
	private boolean mandatory = true;
	private int type;

	public int getAge() {
		return age;
	}

	public byte[] getData() {
		return data;
	}

	public int getFile() {
		return file;
	}

	public int getType() {
		return type;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public void setFile(int file) {
		this.file = file;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public void setType(int type) {
		this.type = type;
	}

}