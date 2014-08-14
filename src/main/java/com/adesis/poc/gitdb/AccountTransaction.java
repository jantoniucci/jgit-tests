package com.adesis.poc.gitdb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountTransaction {

	String id;
	String date;
	String description;
	Double ammount;
	Double balance;
	
}
