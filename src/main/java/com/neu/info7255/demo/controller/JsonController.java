package com.neu.info7255.demo.controller;

import com.neu.info7255.demo.dao.RedisOps;
import com.neu.info7255.demo.service.ESEvent;
import com.neu.info7255.demo.validator.JsonSchemaValidator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.request.WebRequest;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

@RestController
public class JsonController {

    @Autowired
    ApplicationEventPublisher eventPublisher;


    @RequestMapping(path = "/demo", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity create(@RequestBody String reqJSON, WebRequest request) throws FileNotFoundException {
        JSONObject req = new JSONObject(reqJSON);
        RedisOps ops = new RedisOps();
        //validate the JSON schema
        JsonSchemaValidator schemaValidator = new JsonSchemaValidator();
        boolean res = schemaValidator.validateSchema(req);
        if(res == false){
            return ResponseEntity
                    .badRequest()
                    .body("Wrong Format");
        }
        String key = req.getString("objectId");
        //set hashcode as etag.
        String etag = getMD5(req.toString());
        //check etag
        String ifNoneMatch = request.getHeader("If-None-Match");
        if(request.checkNotModified(ifNoneMatch)){
            return null;
        }
        /*
        Store the following JSON string into Redis
        JSON contents: planCostShares,linkedPlanServices,_org,objectId,objectType,planType,creationDate
         */
        JSONObject planCostShares = req.getJSONObject("planCostShares");
        JSONArray linkedPlanServices = req.getJSONArray("linkedPlanServices");
        ops.setHash(key, "planCostShares", JSONObject.valueToString(planCostShares));
        ops.setHash(key, "linkedPlanServices", linkedPlanServices.toString());
        ops.setHash(key, "_org", req.getString("_org"));
        ops.setHash(key, "objectId", req.getString("objectId"));
        ops.setHash(key, "objectType", req.getString("objectType"));
        ops.setHash(key, "planType", req.getString("planType"));
        ops.setHash(key, "creationDate", req.getString("creationDate"));

        ESEvent event = new ESEvent(this, reqJSON);
        eventPublisher.publishEvent(event);


        return ResponseEntity
                .created(URI.create(request.getContextPath()))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                .eTag(etag)
                .body(reqJSON);
    }

    @RequestMapping(path = "/demo/es/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity getES(@PathVariable("id") String id, WebRequest request) throws IOException {
        ESOps ops = new ESOps();
        String res = ops.getDoc(id);

        if(res == null){
            return ResponseEntity
                    .notFound()
                    .build();
        }

        String ifNoneMatch = request.getHeader("If-None-Match");
        if(request.checkNotModified(ifNoneMatch)){
            return null;
        }
        String etag = getMD5(res);
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                .eTag(etag)
                .body(res);

    }

    @RequestMapping(path = "/demo/{key}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity get(@PathVariable("key") String key, WebRequest request) {
        RedisOps ops = new RedisOps();
        JSONObject res = ops.getHash(key);

        if(res == null){
            return ResponseEntity
                    .notFound()
                    .build();
        }

        String ifNoneMatch = request.getHeader("If-None-Match");
        if(request.checkNotModified(ifNoneMatch)){
//            System.out.println(ifNoneMatch);
            return null;
        }

//        String etag = String.valueOf(res.toString().hashCode());
        String etag = getMD5(res.toString());
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                .eTag(etag)
                .body(res.toString());

    }

    @RequestMapping(path = "/demo/{key}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public ResponseEntity update(@PathVariable("key") String key, WebRequest request, @RequestBody String reqJSON) throws FileNotFoundException {
        RedisOps ops = new RedisOps();
        JSONObject res = ops.getHash(key);

        //check key whether exists
        if(res == null || key.equals("")){
            return ResponseEntity
                    .notFound()
                    .build();
        }


        //validate the JSON schema
        JSONObject req = new JSONObject(reqJSON);
        JsonSchemaValidator schemaValidator = new JsonSchemaValidator();
        boolean validRes = schemaValidator.validateSchema(req);
        if(validRes == false){
            return ResponseEntity
                    .badRequest()
                    .body("Wrong Format");
        }


        //set hashcode as etag.
//        String etag = String.valueOf(req.toString().hashCode());
        String etag = getMD5(req.toString());

        //check etag
        String ifNoneMatch = request.getHeader("If-None-Match");
        if(request.checkNotModified(ifNoneMatch)){
//            System.out.println(ifNoneMatch);
            return null;
        }


        /*
        Store the following JSON string into Redis
        JSON contents: planCostShares,linkedPlanServices,_org,objectId,objectType,planType,creationDate
         */

        JSONObject planCostShares = req.getJSONObject("planCostShares");
        JSONArray linkedPlanServices = req.getJSONArray("linkedPlanServices");
        ops.setHash(key, "planCostShares", JSONObject.valueToString(planCostShares));
        ops.setHash(key, "linkedPlanServices", linkedPlanServices.toString());
        ops.setHash(key, "_org", req.getString("_org"));
        ops.setHash(key, "objectId", req.getString("objectId"));
        ops.setHash(key, "objectType", req.getString("objectType"));
        ops.setHash(key, "planType", req.getString("planType"));
        ops.setHash(key, "creationDate", req.getString("creationDate"));

        return ResponseEntity
                .created(URI.create(request.getContextPath()))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                .eTag(etag)
                .body(reqJSON);
    }

    @RequestMapping(path = "/demo/{key}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public ResponseEntity delete(@PathVariable("key") String key, WebRequest request) {
        RedisOps ops = new RedisOps();
        JSONObject res = ops.getHash(key);

        //check key whether exists
        if(res == null){
            return ResponseEntity
                    .notFound()
                    .build();
        }

        ops.delHash(key);

        return ResponseEntity
                .noContent()
                .build();
    }

    public static String getMD5(String message) {
        String md5str = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] input = message.getBytes();
            byte[] buff = md.digest(input);
            md5str = bytesToHex(buff);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return md5str;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuffer md5str = new StringBuffer();
        int digital;
        for (int i = 0; i < bytes.length; i++) {
            digital = bytes[i];

            if (digital < 0) {
                digital += 256;
            }
            if (digital < 16) {
                md5str.append("0");
            }
            md5str.append(Integer.toHexString(digital));
        }
        return md5str.toString().toLowerCase();
    }

}
