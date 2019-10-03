package com.neu.info7255.demo.dao;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConnection {
    private static String HOST = "127.0.0.1";
    private static int PORT = 6379;
    private static int MAX_ACTIVE = 1024;
    private static int MAX_IDLE = 200;
    private static int MAX_WAIT = 10000;

    private static JedisPool jedisPool = null;

    private static void initPool(){
        try {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(MAX_ACTIVE);
            config.setMaxIdle(MAX_IDLE);
            config.setMaxWaitMillis(MAX_WAIT);

            jedisPool = new JedisPool(config, HOST, PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static Jedis getJedis() {
        try {
            if(jedisPool == null){
                initPool();
            }
            Jedis jedis = jedisPool.getResource();
//            jedis.auth("redis");
            return jedis;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
