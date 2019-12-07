package com.neu.info7255.demo.controller;

import com.neu.info7255.demo.dao.JsonObjOps;
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
import org.springframework.web.context.request.WebRequest;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
public class JsonController {

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @RequestMapping(path = "/plan", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity create(@RequestBody String reqJSON, WebRequest request) throws FileNotFoundException {
        JSONObject req = new JSONObject(reqJSON);
        //validate the JSON schema
        JsonSchemaValidator schemaValidator = new JsonSchemaValidator();
        boolean res = schemaValidator.validateSchema(req);
        if(res == false){
            return ResponseEntity
                    .badRequest()
                    .body("Wrong Format");
        }
        //set hashcode as etag.
        String etag = getMD5(req.toString());
        //check etag
        String ifNoneMatch = request.getHeader("If-None-Match");
        if(request.checkNotModified(ifNoneMatch)){
            return null;
        }
        /*
        Store the following JSON string into Redis
        the key is "obj type" + "obj id"
         */
        JsonObjOps jsonStore = new JsonObjOps();
        //add planCostShares
        JSONObject planCostShares = req.getJSONObject("planCostShares");
        String planCostSharesKey = planCostShares.getString("objectType") + "__" + planCostShares.getString("objectId");
        jsonStore.addMembercostshare(planCostShares, planCostSharesKey);
        //add linkedPlanService
        JSONArray linkedPlanServices = req.getJSONArray("linkedPlanServices");
        String linkedPlanServicesKey = "[";
        for(int i = 0; i < linkedPlanServices.length(); i++){
            JSONObject planservice = linkedPlanServices.getJSONObject(i);
            //store linkedService
            JSONObject linkedService = planservice.getJSONObject("linkedService");
            String linkedServiceKey = linkedService.getString("objectType") + "__" + linkedService.getString("objectId");
            jsonStore.addService(linkedService, linkedServiceKey);
            //store planserviceCostShares
            JSONObject planserviceCostShares = planservice.getJSONObject("planserviceCostShares");
            String planserviceCostSharesKey = planserviceCostShares.getString("objectType") + "__" + planserviceCostShares.getString("objectId");
            jsonStore.addMembercostshare(planserviceCostShares, planserviceCostSharesKey);
            //store linkedPlanService
            String planserviceKey = planservice.getString("objectType") + "__" + planservice.getString("objectId");
            jsonStore.addPlanservice(planservice, planserviceKey, linkedServiceKey, planserviceCostSharesKey);
            //get linkedPlanService key list
            if(i == linkedPlanServices.length() - 1){
                linkedPlanServicesKey += planserviceKey;
            }else {
                linkedPlanServicesKey += planserviceKey + ",";
            }
        }
        linkedPlanServicesKey += "]";
        //add plan
        String planKey = req.getString("objectType") + "__" + req.getString("objectId");
        jsonStore.addPlan(req, planKey, planCostSharesKey, linkedPlanServicesKey);

        ESEvent event = new ESEvent(this, reqJSON);
        eventPublisher.publishEvent(event);

        return ResponseEntity
                .created(URI.create(request.getContextPath()))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                .eTag(etag)
                .body(reqJSON);
    }

    @RequestMapping(path = "/plan/es/{id}", method = RequestMethod.GET, produces = "application/json")
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





    @RequestMapping(path = "/plan/{key}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity get(@PathVariable("key") String key, WebRequest request) {
        RedisOps ops = new RedisOps();
        //check key whether is exist
        key = "plan__" + key;
        Boolean isKey = ops.getKey(key);
        if(isKey == false){
            return ResponseEntity
                    .notFound()
                    .build();
        }
        String ifNoneMatch = request.getHeader("If-None-Match");
        if(request.checkNotModified(ifNoneMatch)){
            return null;
        }
        JsonObjOps jsonGet = new JsonObjOps();
        JSONObject res = jsonGet.getPlan(key);
        String etag = getMD5(res.toString());
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                .eTag(etag)
                .body(res.toString());

    }

    @RequestMapping(path = "/plan/{key}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public ResponseEntity update(@PathVariable("key") String key, WebRequest request, @RequestBody String reqJSON) throws FileNotFoundException {
        RedisOps ops = new RedisOps();
        //check key whether is exist
        key = "plan__" + key;
        Boolean isKey = ops.getKey(key);
        if(isKey == false){
            return ResponseEntity
                    .notFound()
                    .build();
        }
        JSONObject req = new JSONObject(reqJSON);
        //validate the JSON schema
        JsonSchemaValidator schemaValidator = new JsonSchemaValidator();
        boolean res = schemaValidator.validateSchema(req);
        if(res == false){
            return ResponseEntity
                    .badRequest()
                    .body("Wrong Format");
        }
        //set hashcode as etag.
        String etag = getMD5(req.toString());
        //check etag
        String ifNoneMatch = request.getHeader("If-None-Match");
        if(request.checkNotModified(ifNoneMatch)){
            return null;
        }
        /*
        update the following JSON string into Redis
        the key is "obj type" + "obj id"
         */
        JsonObjOps jsonStore = new JsonObjOps();
        //delete old plan
        jsonStore.deletePlan(key);
        //add planCostShares
        JSONObject planCostShares = req.getJSONObject("planCostShares");
        String planCostSharesKey = planCostShares.getString("objectType") + "__" + planCostShares.getString("objectId");
        jsonStore.addMembercostshare(planCostShares, planCostSharesKey);
        //add linkedPlanService
        JSONArray linkedPlanServices = req.getJSONArray("linkedPlanServices");
        String linkedPlanServicesKey = "[";
        for(int i = 0; i < linkedPlanServices.length(); i++){
            JSONObject planservice = linkedPlanServices.getJSONObject(i);
            //store linkedService
            JSONObject linkedService = planservice.getJSONObject("linkedService");
            String linkedServiceKey = linkedService.getString("objectType") + "__" + linkedService.getString("objectId");
            jsonStore.addService(linkedService, linkedServiceKey);
            //store planserviceCostShares
            JSONObject planserviceCostShares = planservice.getJSONObject("planserviceCostShares");
            String planserviceCostSharesKey = planserviceCostShares.getString("objectType") + "__" + planserviceCostShares.getString("objectId");
            jsonStore.addMembercostshare(planserviceCostShares, planserviceCostSharesKey);
            //store linkedPlanService
            String planserviceKey = planservice.getString("objectType") + "__" + planservice.getString("objectId");
            jsonStore.addPlanservice(planservice, planserviceKey, linkedServiceKey, planserviceCostSharesKey);
            //get linkedPlanService key list
            if(i == linkedPlanServices.length() - 1){
                linkedPlanServicesKey += planserviceKey;
            }else {
                linkedPlanServicesKey += planserviceKey + ",";
            }
        }
        linkedPlanServicesKey += "]";
        //add plan
        String planKey = req.getString("objectType") + "__" + req.getString("objectId");
        jsonStore.addPlan(req, planKey, planCostSharesKey, linkedPlanServicesKey);


        return ResponseEntity
                .created(URI.create(request.getContextPath()))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                .eTag(etag)
                .body(reqJSON);
    }

    @RequestMapping(path = "/plan/{key}", method = RequestMethod.PATCH, produces = "application/json")
    @ResponseBody
    public ResponseEntity patch(@PathVariable("key") String key, WebRequest request, @RequestBody String reqJSON) throws FileNotFoundException {
        RedisOps ops = new RedisOps();
        //check key whether is exist
        key = "plan__" + key;
        Boolean isKey = ops.getKey(key);
        if(isKey == false){
            return ResponseEntity
                    .notFound()
                    .build();
        }
//        JSONObject req = new JSONObject(reqJSON);
//        //validate the JSON schema
//        JsonSchemaValidator schemaValidator = new JsonSchemaValidator();
//        boolean res = schemaValidator.validateSchema(req);
//        if(res == false){
//            return ResponseEntity
//                    .badRequest()
//                    .body("Wrong Format");
//        }

        //check etag
        String ifNoneMatch = request.getHeader("If-None-Match");
        if(request.checkNotModified(ifNoneMatch)){
            return null;
        }
        //patch
        JsonObjOps jsonPatch = new JsonObjOps();
        JSONObject req = new JSONObject(reqJSON);
        //op could be add, replace, remove, move, copy, test
        jsonPatch.patchPlan(req, key);

        //set etag
        JsonObjOps jsonGet = new JsonObjOps();
        JSONObject res = jsonGet.getPlan(key);
        String etag = getMD5(res.toString());


        JSONObject patRes = jsonGet.getPlan(key);
        ESEvent event = new ESEvent(this, patRes.toString());
        eventPublisher.publishEvent(event);

        return ResponseEntity
                .created(URI.create(request.getContextPath()))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                .eTag(etag)
                .body(res.toString());
    }

    @RequestMapping(path = "/plan/{key}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public ResponseEntity delete(@PathVariable("key") String key, WebRequest request) {
        RedisOps ops = new RedisOps();
        //check key whether is exist
        key = "plan__" + key;
        Boolean isKey = ops.getKey(key);
        if(isKey == false){
            return ResponseEntity
                    .notFound()
                    .build();
        }
        String ifNoneMatch = request.getHeader("If-None-Match");
        if(request.checkNotModified(ifNoneMatch)){
            return null;
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
