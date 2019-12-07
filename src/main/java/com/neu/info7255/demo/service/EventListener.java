package com.neu.info7255.demo.service;

import com.neu.info7255.demo.controller.ESOps;
import com.neu.info7255.demo.dao.RedisOps;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class EventListener implements ApplicationListener<ESEvent> {

    @Override
    public void onApplicationEvent(ESEvent esEvent) {
        RedisOps Rops = new RedisOps();
        ESOps esops = new ESOps();
        String req = Rops.rpoplpush();
        try {
            String response = esops.putCreateDoc(req);
            System.out.println(response);
            System.out.println("consume items in queue");
            Rops.lrem(req);
            if(Rops.llen() > 0){
                String restReq = Rops.rpop();
                String restResponse = esops.putCreateDoc(req);
                System.out.println(restResponse);
                System.out.println("consume items in pending queue");
            }
        } catch (IOException e) {
            System.out.println("action failed");
            e.printStackTrace();
        }
    }
}
