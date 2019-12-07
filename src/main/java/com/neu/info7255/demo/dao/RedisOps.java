package com.neu.info7255.demo.dao;

import com.neu.info7255.demo.controller.JsonController;
import org.json.JSONArray;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.List;

public class RedisOps {

    public void setHash(String key, String field, String value){
        Jedis jedis = RedisConnection.getJedis();
        jedis.hset(key, field, value);
        jedis.close();
    }

    public JSONObject getHash(String key){
        Jedis jedis = RedisConnection.getJedis();
        JSONObject res = new JSONObject();
        /*
        JSON contents:
            planCostShares
            linkedPlanServices
            _org
            objectId
            objectType
            planType
            creationDate
         */
        if(jedis.keys(key).size() == 0){
            return null;
        }else {
            JSONObject planCostShares = new JSONObject(jedis.hget(key, "planCostShares"));
            JSONArray linkedPlanServices = new JSONArray(jedis.hget(key, "linkedPlanServices"));
            res.put("planCostShares", planCostShares);
            res.put("linkedPlanServices", linkedPlanServices);
            res.put("_org", jedis.hget(key, "_org"));
            res.put("objectId", jedis.hget(key, "objectId"));
            res.put("objectType", jedis.hget(key, "objectType"));
            res.put("planType", jedis.hget(key, "planType"));
            res.put("creationDate", jedis.hget(key, "creationDate"));
        }
        jedis.close();
        return res;
    }

    public int delHash(String key){
        Jedis jedis = RedisConnection.getJedis();
        jedis.del(key);
        jedis.close();
        return 1;
    }

    public void lpush(String value){
        Jedis jedis = RedisConnection.getJedis();
        try{
            jedis.lpush("queue", value);
        }finally {
            jedis.close();
        }
    }

    public String rpoplpush(){
        Jedis jedis = RedisConnection.getJedis();
        try{
            String res = jedis.rpoplpush("queue", "pending");
            return res;
        }finally {
            jedis.close();
        }
    }

    public void lrem(String value){
        Jedis jedis = RedisConnection.getJedis();
        try{
            jedis.lrem("pending", 0, value);
        }finally {
            jedis.close();
        }
    }

    public Long llen(){
        Jedis jedis = RedisConnection.getJedis();
        try{
           Long len = jedis.llen("pending");
           return len;
        }finally {
            jedis.close();
        }
    }

    public String rpop(){
        Jedis jedis = RedisConnection.getJedis();
        try{
            String res = jedis.rpop("pending");
            return res;
        }finally {
            jedis.close();
        }
    }




}
