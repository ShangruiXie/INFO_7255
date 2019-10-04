package com.neu.info7255.demo.validator;

import com.fasterxml.jackson.databind.JsonNode;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;

public class JsonSchemaValidator {
    public boolean validateSchema(JSONObject data) throws FileNotFoundException {
        InputStream inputStream = getClass().getResourceAsStream("/json/use case schema.json");
        JSONObject schemaJson = new JSONObject(new JSONTokener(inputStream));
        Schema schema = SchemaLoader.load(schemaJson);
        try {
            schema.validate(data);
            return true;
        }catch (ValidationException e){
            System.out.println(e.getErrorMessage());
        }
        return false;
    }
}
