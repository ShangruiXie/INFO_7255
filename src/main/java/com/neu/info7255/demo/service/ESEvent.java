package com.neu.info7255.demo.service;

import com.neu.info7255.demo.dao.RedisOps;
import org.springframework.context.ApplicationEvent;

public class ESEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    public ESEvent(Object source, String req) {
        super(source);
        RedisOps ops = new RedisOps();
        ops.lpush(req);
        System.out.println("push to queue");
    }
}
