package com.neu.info7255.demo.dao;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonObjOps {
    RedisOps ops = new RedisOps();

    public void addPlan(JSONObject jsonObject, String key, String planCostShareKey, String linkedPlanServiceKeys){
        //plan: planCostShares, linkedPlanServices, _org, objectId, objectType, planType, creationDate
        ops.setHash(key, "planCostShares", planCostShareKey);
        ops.setHash(key, "linkedPlanServices", linkedPlanServiceKeys);
        ops.setHash(key, "_org", jsonObject.getString("_org"));
        ops.setHash(key, "objectId", jsonObject.getString("objectId"));
        ops.setHash(key, "objectType", jsonObject.getString("objectType"));
        ops.setHash(key, "planType", jsonObject.getString("planType"));
        ops.setHash(key, "creationDate", jsonObject.getString("creationDate"));
    }

    public JSONObject getPlan(String key){
        JSONObject res = new JSONObject();
        //get planCOstShares
        JSONObject planCostShares = getMembercostshare(ops.getHash(key, "planCostShares"));
        res.put("planCostShares", planCostShares);
        //get linkedPlanServices
        JSONArray linkedPlanServices  = new JSONArray();
        String linkedPlanServicesKeys = ops.getHash(key, "linkedPlanServices");
        String[] linkedPlanServicesKeysList = linkedPlanServicesKeys.substring(1, linkedPlanServicesKeys.length()-1).split(",");
        for(int i=0; i<linkedPlanServicesKeysList.length; i++){
            linkedPlanServices.put(getPlanservice(linkedPlanServicesKeysList[i]));
        }
        res.put("linkedPlanServices", linkedPlanServices);

        res.put("_org", ops.getHash(key, "_org"));
        res.put("objectId", ops.getHash(key, "objectId"));
        res.put("objectType", ops.getHash(key, "objectType"));
        res.put("planType", ops.getHash(key, "planType"));
        res.put("creationDate", ops.getHash(key, "creationDate"));
        return res;
    }

    public void addMembercostshare(JSONObject jsonObject, String key){
        //membercostshare: deductible, _org, copay, objectId, objectType
        ops.setHash(key, "deductible", String.valueOf(jsonObject.getInt("deductible")));
        ops.setHash(key, "_org", jsonObject.getString("_org"));
        ops.setHash(key, "copay", String.valueOf(jsonObject.getInt("copay")));
        ops.setHash(key, "objectId", jsonObject.getString("objectId"));
        ops.setHash(key, "objectType", jsonObject.getString("objectType"));
    }

    public JSONObject getMembercostshare(String key){
        JSONObject res = new JSONObject();
        res.put("deductible", Integer.valueOf(ops.getHash(key, "deductible")));
        res.put("_org", ops.getHash(key, "_org"));
        res.put("copay", Integer.valueOf(ops.getHash(key, "copay")));
        res.put("objectId", ops.getHash(key, "objectId"));
        res.put("objectType", ops.getHash(key, "objectType"));
        return res;
    }

    public void addService(JSONObject jsonObject, String key){
        //service: _org, objectId, objectType, name
        ops.setHash(key, "_org", jsonObject.getString("_org"));
        ops.setHash(key, "objectId", jsonObject.getString("objectId"));
        ops.setHash(key, "objectType", jsonObject.getString("objectType"));
        ops.setHash(key, "name", jsonObject.getString("name"));
    }

    public JSONObject getService(String key){
        JSONObject res = new JSONObject();
        res.put("_org", ops.getHash(key, "_org"));
        res.put("objectId", ops.getHash(key, "objectId"));
        res.put("objectType", ops.getHash(key, "objectType"));
        res.put("name", ops.getHash(key, "name"));
        return res;
    }

    public void addPlanservice(JSONObject jsonObject, String key, String linkedServiceKey, String planserviceCostSharesKey){
        //planservice: linkedService, planserviceCostShares, _org, objectId, objectType
        ops.setHash(key, "linkedService", linkedServiceKey);
        ops.setHash(key, "planserviceCostShares", planserviceCostSharesKey);
        ops.setHash(key, "_org", jsonObject.getString("_org"));
        ops.setHash(key, "objectId", jsonObject.getString("objectId"));
        ops.setHash(key, "objectType", jsonObject.getString("objectType"));
    }

    public JSONObject getPlanservice(String key){
        JSONObject res = new JSONObject();
        //get linkedService
        String linkedServiceKey = ops.getHash(key, "linkedService");
        JSONObject linkedService = getService(linkedServiceKey);
        res.put("linkedService", linkedService);
        //get planserviceCostShares
        String planserviceCostSharesKey = ops.getHash(key, "planserviceCostShares");
        JSONObject planserviceCostShares = getMembercostshare(planserviceCostSharesKey);
        res.put("planserviceCostShares", planserviceCostShares);

        res.put("_org", ops.getHash(key, "_org"));
        res.put("objectId", ops.getHash(key, "objectId"));
        res.put("objectType", ops.getHash(key, "objectType"));
        return res;
    }

    public void deletePlan(String key){
        //delete planCostShares
        String planCostSharesKey = ops.getHash(key, "planCostShares");
        ops.delHash(planCostSharesKey);
        //delete linkedPlanServices
        String linkedPlanServicesKeys = ops.getHash(key, "linkedPlanServices");
        String[] linkedPlanServicesKeysList = linkedPlanServicesKeys.substring(1, linkedPlanServicesKeys.length()-1).split(",");
        for(int i=0; i<linkedPlanServicesKeysList.length; i++){
            String linkedServiceKey = ops.getHash(linkedPlanServicesKeysList[i], "linkedService");
            ops.delHash(linkedServiceKey);
            String planserviceCostSharesKey = ops.getHash(linkedPlanServicesKeysList[i], "planserviceCostShares");
            ops.delHash(planserviceCostSharesKey);
            ops.delHash(linkedPlanServicesKeysList[i]);
        }
        ops.delHash(key);
    }



}
