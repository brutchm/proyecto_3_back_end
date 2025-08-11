package com.project.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.project.demo.logic.entity.auth.GoogleAuthService;

@SpringBootTest
@ActiveProfiles("test")
class DemoApplicationTests {

	@MockBean
	private GoogleAuthService googleAuthService;

	@Test
	void contextLoads() {
	}

}
