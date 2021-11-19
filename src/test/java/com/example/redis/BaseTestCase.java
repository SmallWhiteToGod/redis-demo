package com.example.redis;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = {"classpath:app-core.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class BaseTestCase extends AbstractJUnit4SpringContextTests {

}

