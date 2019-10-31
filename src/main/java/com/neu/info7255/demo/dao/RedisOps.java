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




}
