package com.lukete.datagit;

import org.springframework.boot.SpringApplication;

public class TestDatagitApplication {

	public static void main(String[] args) {
		SpringApplication.from(Main::main).with(TestcontainersConfiguration.class).run(args);
	}

}