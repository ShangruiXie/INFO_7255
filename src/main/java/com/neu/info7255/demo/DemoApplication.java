package com.neu.info7255.demo;

import com.neu.info7255.demo.service.EventListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import javax.servlet.Filter;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
//        ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);
//        context.addApplicationListener(new EventListener());
        SpringApplication.run(DemoApplication.class, args);

//        SpringApplication app = new SpringApplication(DemoApplication.class);
//        app.addListeners(new EventListener());
//        app.run(args);
    }

//    @Bean
//    public Filter filter(){
//        ShallowEtagHeaderFilter filter=new ShallowEtagHeaderFilter();
//        return filter;
//    }
}
