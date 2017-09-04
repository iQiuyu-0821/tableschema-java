package io.frictionlessdata.tableschema;

import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.hamcrest.CoreMatchers.instanceOf;
import org.joda.time.DateTime;
import static org.junit.Assert.assertThat;
import org.junit.rules.TemporaryFolder;

/**
 *
 * 
 */
public class SchemaTest {
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();
     
    @Test
    public void testCreateSchemaFromValidSchemaJson(){ 
        JSONObject schemaJsonObj = new JSONObject();
       
        schemaJsonObj.put("fields", new JSONArray());
        Field nameField = new Field("id", Field.FIELD_TYPE_INTEGER);
        schemaJsonObj.getJSONArray("fields").put(nameField.getJson());
        
        Schema validSchema = new Schema(schemaJsonObj);
        Assert.assertTrue(validSchema.validate());
    }
    
    @Test
    public void testCreateSchemaFromInvalidSchemaJson(){  
        JSONObject schemaJsonObj = new JSONObject();
       
        schemaJsonObj.put("fields", new JSONArray());
        Field nameField = new Field("id", Field.FIELD_TYPE_INTEGER);
        Field invalidField = new Field("coordinates", "invalid");
        schemaJsonObj.getJSONArray("fields").put(nameField.getJson());
        schemaJsonObj.getJSONArray("fields").put(invalidField.getJson());
        
        Schema invalidSchema = new Schema(schemaJsonObj);
        Assert.assertFalse(invalidSchema.validate());
    }
     
    @Test
    public void testAddValidField(){
        Field nameField = new Field("id", Field.FIELD_TYPE_INTEGER);
        Schema validSchema = new Schema();
        validSchema.addField(nameField);
        
        Assert.assertEquals(1, validSchema.getFields().size()); 
    }
    
    @Test
    public void testAddInvalidField(){
        Field idField = new Field("id", Field.FIELD_TYPE_INTEGER);
        Field invalidField = new Field("title", "invalid");
        Field geopointField = new Field("coordinates", Field.FIELD_TYPE_GEOPOINT); 
        
        Schema validSchema = new Schema();
        validSchema.addField(idField);
        validSchema.addField(invalidField); // will be ignored
        validSchema.addField(geopointField);
        
        Assert.assertEquals(2, validSchema.getFields().size());
        Assert.assertNull(validSchema.getField("title"));
        Assert.assertNotNull(validSchema.getField("id"));
        Assert.assertNotNull(validSchema.getField("coordinates"));
    }
    
    @Test
    public void hasField(){
        Schema schema = new Schema();
        Assert.assertFalse(schema.hasFields());
        
        Field idField = new Field("id", Field.FIELD_TYPE_INTEGER);
        schema.addField(idField);
        Assert.assertTrue(schema.hasFields());
    }
    
    
    @Test
    public void testCastRow() throws Exception{
        Schema schema = new Schema();
        
        // String
        Field fieldString = new Field("fieldString", Field.FIELD_TYPE_STRING);
        schema.addField(fieldString);
        
        // Integer
        Field fieldInteger = new Field("fieldInteger", Field.FIELD_TYPE_INTEGER);
        schema.addField(fieldInteger);
        
        // Boolean
        Field fieldBoolean = new Field("fieldBoolean", Field.FIELD_TYPE_BOOLEAN);
        schema.addField(fieldBoolean);

        // Object
        Field fieldObject = new Field("fieldObject", Field.FIELD_TYPE_OBJECT);
        schema.addField(fieldObject);
        
        // Array
        Field fieldArray = new Field("fieldArray", Field.FIELD_TYPE_ARRAY);
        schema.addField(fieldArray);
        
        // Date
        Field fieldDate = new Field("fieldDate", Field.FIELD_TYPE_DATE);
        schema.addField(fieldDate);
        
        // Time
        Field fieldTime = new Field("fieldTime", Field.FIELD_TYPE_TIME);
        schema.addField(fieldTime);
        
        // Datetime
        Field fieldDatetime = new Field("fieldDatetime", Field.FIELD_TYPE_DATETIME);
        schema.addField(fieldDatetime);
        
        // Year
        Field fieldYear = new Field("fieldYear", Field.FIELD_TYPE_YEAR);
        schema.addField(fieldYear);
        
        // Yearmonth
        Field fieldYearmonth = new Field("fieldYearmonth", Field.FIELD_TYPE_YEARMONTH);
        schema.addField(fieldYearmonth);
        
        // Duration
        Field fieldDuration = new Field("fieldDuration", Field.FIELD_TYPE_DURATION);
        schema.addField(fieldDuration);
        
        // Number
        // TODO: Implement
        
        // Geopoint
        // TODO: Implement
        
        // Geojson
        // TODO: Implement
        
        
        String[] row = new String[]{
            "John Doe", // String
            "25", // Integer
            "T", // Boolean
            "{\"one\": 1, \"two\": 2, \"three\": 3}", // Object
            "[1,2,3,4]", // Array
            "2008-08-30", // Date
            "14:22:33", // Time
            "2008-08-30T01:45:36.123Z", // Datetime
            "2008", // Year
            "2008-08", // Yearmonth
            "P2DT3H4M"  // Duration
            // Number
            // Geopoint
            // Geojson
        };
        
        Object[] castRow = schema.castRow(row);
       
        assertThat(castRow[0], instanceOf(String.class));
        assertThat(castRow[1], instanceOf(Integer.class));
        assertThat(castRow[2], instanceOf(Boolean.class));
        assertThat(castRow[3], instanceOf(JSONObject.class));
        assertThat(castRow[4], instanceOf(JSONArray.class));
        assertThat(castRow[5], instanceOf(DateTime.class));
        assertThat(castRow[6], instanceOf(DateTime.class));
        assertThat(castRow[7], instanceOf(DateTime.class));
        assertThat(castRow[8], instanceOf(Integer.class));
        assertThat(castRow[9], instanceOf(DateTime.class));
        assertThat(castRow[10], instanceOf(Duration.class));
    }
    
    @Test
    public void testCastRowWithInvalidLength() throws Exception{
        Schema schema = new Schema();
        
        Field fieldString = new Field("name", Field.FIELD_TYPE_STRING);
        schema.addField(fieldString);
        
        Field fieldInteger = new Field("id", Field.FIELD_TYPE_INTEGER);
        schema.addField(fieldInteger);
        
        String[] row = new String[]{"John Doe", "25", "T"}; // length is 3 instead of 2.
        
        exception.expect(InvalidCastException.class);
        schema.castRow(row);
    }
    
    @Test
    public void testCastRowWithInvalidValue() throws Exception{
        Schema schema = new Schema();
        
        Field fieldString = new Field("name", Field.FIELD_TYPE_STRING);
        schema.addField(fieldString);
        
        Field fieldInteger = new Field("id", Field.FIELD_TYPE_INTEGER);
        schema.addField(fieldInteger);
        
        String[] row = new String[]{"John Doe", "25 String"};
        
        exception.expect(InvalidCastException.class);
        schema.castRow(row);
    }
    
    @Test
    public void testWrite() throws Exception{
        File createdFile = folder.newFile("test_schema.json");
        
        Schema createdSchema = new Schema(); 
        
        Map<String, Object> intFieldConstraints = new HashMap();
        intFieldConstraints.put(Field.CONSTRAINT_KEY_REQUIRED, true);
                
        Field intField = new Field("id", Field.FIELD_TYPE_INTEGER, Field.FIELD_FORMAT_DEFAULT, null, null, intFieldConstraints);
        createdSchema.addField(intField);
        
        Map<String, Object> stringFieldConstraints = new HashMap();
        stringFieldConstraints.put(Field.CONSTRAINT_KEY_MIN_LENGTH, 36);
        stringFieldConstraints.put(Field.CONSTRAINT_KEY_MAX_LENGTH, 45);
        
        Field stringField = new Field("name", Field.FIELD_TYPE_STRING, Field.FIELD_FORMAT_DEFAULT, "the title", "the description", stringFieldConstraints);
        createdSchema.addField(stringField);
        
        createdSchema.write(createdFile.getAbsolutePath());
        
        Schema readSchema = new Schema(createdFile.getAbsolutePath());
        
        Assert.assertEquals(readSchema.getField("id").getType(), Field.FIELD_TYPE_INTEGER);
        Assert.assertEquals(readSchema.getField("id").getFormat(), Field.FIELD_FORMAT_DEFAULT);
        Assert.assertEquals(readSchema.getField("id").getTitle(), "");
        Assert.assertEquals(readSchema.getField("id").getDescription(), "");
        Assert.assertTrue((boolean)readSchema.getField("id").getConstraints().get(Field.CONSTRAINT_KEY_REQUIRED));
        
        Assert.assertEquals(readSchema.getField("name").getType(), Field.FIELD_TYPE_STRING);
        Assert.assertEquals(readSchema.getField("name").getFormat(), Field.FIELD_FORMAT_DEFAULT);
        Assert.assertEquals(readSchema.getField("name").getTitle(), "the title");
        Assert.assertEquals(readSchema.getField("name").getDescription(), "the description");
        Assert.assertEquals(36, readSchema.getField("name").getConstraints().get(Field.CONSTRAINT_KEY_MIN_LENGTH));
        Assert.assertEquals(45, readSchema.getField("name").getConstraints().get(Field.CONSTRAINT_KEY_MAX_LENGTH));
    }

}
