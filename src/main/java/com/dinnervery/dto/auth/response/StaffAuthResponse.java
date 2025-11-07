package com.dinnervery.dto.auth.response;

import lombok.Value;

@Value
public class StaffAuthResponse {
	Long staffId;
	String loginId;
	String name;
	String task;
	String token;
}

