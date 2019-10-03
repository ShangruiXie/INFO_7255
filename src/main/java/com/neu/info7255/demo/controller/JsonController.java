package com.neu.info7255.demo.controller;

import com.neu.info7255.demo.dao.RedisConnection;
import com.neu.info7255.demo.dao.RedisOps;
import com.neu.info7255.demo.validator.JsonSchemaValidator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.io.FileNotFoundException;

@RestController
public class JsonController {

    @RequestMapping(path = "/user", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> create(@RequestBody String reqJSON) throws FileNotFoundException {
        JSONObject req = new JSONObject(reqJSON);
//        JsonSchemaValidator schemaValidator = new JsonSchemaValidator();
//        boolean res = schemaValidator.validateSchema(user);


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

//        if(res == false)
//            return new ResponseEntity<>("error", HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping(path = "/user/{key}", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> get(@PathVariable("key") String key) {
        RedisOps ops = new RedisOps();
        JSONObject res = ops.getHash(key);
        return new ResponseEntity<>(res.toString(), HttpStatus.OK);
    }
}
