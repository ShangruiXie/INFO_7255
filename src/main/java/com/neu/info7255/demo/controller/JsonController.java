package com.neu.info7255.demo.controller;

import com.neu.info7255.demo.dao.RedisConnection;
import com.neu.info7255.demo.dao.RedisOps;
import com.neu.info7255.demo.validator.JsonSchemaValidator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;


import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

@RestController
public class JsonController {

    @RequestMapping(path = "/demo", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity create(@RequestBody String reqJSON, WebRequest request) throws FileNotFoundException {
        JSONObject req = new JSONObject(reqJSON);
        //validate the JSON schema
//        JsonSchemaValidator schemaValidator = new JsonSchemaValidator();
//        boolean res = schemaValidator.validateSchema(user);


        //set hashcode as etag.
        String etag = String.valueOf(req.toString().hashCode());
//        System.out.println(etag);

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
        String key = req.getString("objectId");

        JSONObject planCostShares = req.getJSONObject("planCostShares");
        JSONArray linkedPlanServices = req.getJSONArray("linkedPlanServices");

        RedisOps ops = new RedisOps();
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

        String etag = String.valueOf(res.toString().hashCode());
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                .eTag(etag)
                .body(res.toString());

    }

    @RequestMapping(path = "/demo/{key}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public ResponseEntity update(@PathVariable("key") String key, WebRequest request, @RequestBody String reqJSON){
        RedisOps ops = new RedisOps();
        JSONObject res = ops.getHash(key);

        //check key whether exists
        if(res == null){
            return ResponseEntity
                    .notFound()
                    .build();
        }

        JSONObject req = new JSONObject(reqJSON);
        //validate the JSON schema
//        JsonSchemaValidator schemaValidator = new JsonSchemaValidator();
//        boolean res = schemaValidator.validateSchema(user);

        //set hashcode as etag.
        String etag = String.valueOf(req.toString().hashCode());

        //check etag
        String ifNoneMatch = request.getHeader("If-None-Match");
        if(request.checkNotModified(ifNoneMatch)){
            return null;
        }

        //update ops


        return ResponseEntity
                .created(URI.create(request.getContextPath()))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                .eTag(etag)
                .body(reqJSON);
    }

    @RequestMapping(path = "/demo/{key}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public ResponseEntity update(@PathVariable("key") String key, WebRequest request) {
        RedisOps ops = new RedisOps();
        JSONObject res = ops.getHash(key);

        //check key whether exists
        if(res == null){
            return ResponseEntity
                    .notFound()
                    .build();
        }

        //delete ops

        return ResponseEntity
                .noContent()
                .build();
    }
}
