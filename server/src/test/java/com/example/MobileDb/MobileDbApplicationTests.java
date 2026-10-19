package com.example.MobileDb;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import com.example.MobileDb.config.CustomerDbConfig;
import com.example.MobileDb.config.KisenyiDbConfig;

@SpringBootTest
class MobileDbApplicationTests {

	@Test
	void contextLoads() {
		
	}
	}
	






/*@SpringBootTest
class MobileDbApplicationTests {

	@Test
	void contextLoads() {
	}



@SpringBootTest(classes = {MobileDbApplication.class, KisenyiDbConfig.class})
@SpringBootTest(classes =  KisenyiDbConfig.class)
class KisenyiDbApplicationTests {
    @Test
    void contextLoads() {}
}

@SpringBootTest(classes = {MobileDbApplication.class, CustomerDbConfig.class})
@SpringBootTest(classes =  CustomerDbConfig.class)
class CustomerDbApplicationTests {
    @Test
    void contextLoads() {}
}





@SpringBootTest(classes = {MobileDbApplication.class, CustomerDbConfig.class, KisenyiDbConfig.class})
@SpringBootTest(classes = {MobileDbApplication.class, CustomerDbConfig.class})
@SpringBootTest(classes =  CustomerDbConfig.class)
@SpringBootTest(classes =  KisenyiDbConfig.class)
*/
