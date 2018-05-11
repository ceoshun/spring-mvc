package com.ngn.mvc.provider;

import java.util.UUID;

public class IdProvider {
	public static String createUUIDId() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString().replaceAll("-", "");
	}
}
