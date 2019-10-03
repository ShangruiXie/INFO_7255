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
        JSONObject planCostShares = new JSONObject(jedis.hget(key,"planCostShares"));
        JSONArray linkedPlanServices = new JSONArray(jedis.hget(key, "linkedPlanServices"));
        res.put("planCostShares", planCostShares);
        res.put("linkedPlanServices",linkedPlanServices);
        res.put("_org", jedis.hget(key, "_org"));
        res.put("objectId", jedis.hget(key, "objectId"));
        res.put("objectType", jedis.hget(key, "objectType"));
        res.put("planType", jedis.hget(key, "planType"));
        res.put("creationDate", jedis.hget(key, "creationDate"));
        return res;
    }


}
