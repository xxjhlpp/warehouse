package com.example.knowledge;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.knowledge.mapper")
public class KnowledgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(KnowledgeApplication.class, args);
	}

}
