package com.neu.info7255.demo.dao;

import org.json.JSONArray;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

public class RedisOps {

    public void setHash(String key, String field, String value){
        Jedis jedis = RedisConnection.getJedis();
        jedis.hset(key, field, value);
        jedis.close();
    }

    public String getHash(String key, String field){
        Jedis jedis = RedisConnection.getJedis();
        String res;
        if(jedis.keys(key).size() == 0){
            return null;
        }else {
            res = jedis.hget(key, field);
        }
        jedis.close();
        return res;
    }

    public boolean getKey(String key){
        Jedis jedis = RedisConnection.getJedis();
        Boolean res;
        if(jedis.keys(key).size() == 0){
            res = false;
        }else {
            res = true;
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
