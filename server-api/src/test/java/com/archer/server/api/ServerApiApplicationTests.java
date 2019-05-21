package com.archer.server.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest("spring.profiles.active=dev")
public class ServerApiApplicationTests {

	@Resource
	private StringRedisTemplate stringRedisTemplate;

	@Test
	public void contextLoads() throws Exception {


	}

}
