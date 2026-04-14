package com.bookingnwt.analyticsservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL",
	"spring.datasource.driverClassName=org.h2.Driver",
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"spring.cloud.config.enabled=false",
	"eureka.client.enabled=false"
})
class AnalyticsServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
