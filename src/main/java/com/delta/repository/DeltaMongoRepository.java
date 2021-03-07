package com.delta.repository;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.delta.util.DataMap;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @Desription:
 * @Author: yuzhuojun
 * @CreateDate: 2021/3/5 12:32 AM
 */
// @Component
public class DeltaMongoRepository {

    public static void main(String[] args) {
        String connection = "mongodb://localhost:27017/test";
        MongoDatabaseFactory mongoDbFactory = new SimpleMongoClientDatabaseFactory(connection);
        DeltaMongoRepository repo = new DeltaMongoRepository(new MongoTemplate(mongoDbFactory), "test");

        JSONObject json = new JSONObject();
        json.put("_id", "6044e99225751213a8d83ff3");
        JSONObject a1 = new JSONObject();
        JSONObject a2 = new JSONObject();
        JSONObject a33 = new JSONObject();
        a33.put("a44", "a44");
        a2.put("a3", "a3-change");
        a2.put("a33", a33);
        a1.put("a2", a2);
        json.put("a1", a1);
        JSONObject b1 = new JSONObject();
        b1.put("b2", "b2");
        json.put("b1", b1);
        JSONObject c1 = new JSONObject();
        c1.put("c2", "c2");
        json.put("c1", c1);
        JSONObject d1 = new JSONObject();
        JSONArray d1a = new JSONArray();
        d1a.add("d1a-change");
        d1.put("d1a", d1a);
        json.put("d1", d1);

//        repo.insert(json);
//        System.out.println(repo.findById("6044e99225751213a8d83ff3"));
//        repo.update(json);
        Date date = repo.parseStringDate("2021-03-07T22:56:18Z");
        Date date1 = new Date(date.getTime() + 1);
        System.out.println(repo.findByIdAndDate("6044e99225751213a8d83ff3", date1));
    }

    private static final String ID = "_id";
    private static final String CURRENT_VALUE = "currentValue";
    private static final String EXIST_DATA = "existData";
    private static final String NON_EXIST_DATA = "nonExistData";
    private static final String EDITED_TIME = "editedTime";

    public DeltaMongoRepository(MongoTemplate mongoTemplate, String collection) {
        this.mongoTemplate = mongoTemplate;
        this.collection = collection;
    }

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    // @Autowired
    private MongoTemplate mongoTemplate;

    private String collection;

    private String formatDateString(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return localDateTime.format(dateTimeFormatter);
    }

    private Date parseStringDate(String date) {
        LocalDateTime localDateTime = LocalDateTime.parse(date, dateTimeFormatter);
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public JSONObject insert(JSONObject data) {
        JSONObject json = new JSONObject();
        data.put(EDITED_TIME, this.formatDateString(new Date()));
        json.put(CURRENT_VALUE, data);
        return this.mongoTemplate.insert(json, this.collection);
    }

    public JSONObject findById(String id) {
        return this.findRawDataById(id).getJSONObject(CURRENT_VALUE).fluentPut(ID, id);
    }

    public JSONObject findByIdAndDate(String id, Date date) {
        JSONObject rawData = this.findRawDataById(id);
        rawData.remove(ID);
        rawData = this.sortByDateKey(rawData);

        // The data has not been created
        Entry<String, Object> entry = (Entry<String, Object>) rawData.entrySet().toArray()[rawData.size() - 1];
        boolean hasNotCreated = ((rawData.size() > 1
                && date.getTime() < this.parseStringDate(entry.getKey()).getTime())
                || (rawData.size() == 1
                && date.getTime() < this.parseStringDate(String.valueOf(((Map) entry.getValue()).get(EDITED_TIME))).getTime()));
        if (hasNotCreated) {
            return new JSONObject();
        }

        DataMap targetValue = null;
        for (Iterator<Entry<String, Object>> it = rawData.entrySet().iterator(); it.hasNext();) {
            Entry<String, Object> e = it.next();
            String k = e.getKey();
            DataMap<String, Map> v = new DataMap((Map) e.getValue());
            if (CURRENT_VALUE.equals(k)) {
                targetValue = v;
                if (date.getTime() >= this.parseStringDate(v.getString(EDITED_TIME)).getTime()) {
                    break;
                }
            } else {
                if (date.getTime() < this.parseStringDate(k).getTime()) {
                    targetValue.decrease(v.get(NON_EXIST_DATA));
                    targetValue.increase(v.get(EXIST_DATA));
                } else if (date.getTime() >= this.parseStringDate(k).getTime()) {
                    targetValue.decrease(v.get(NON_EXIST_DATA));
                    targetValue.increase(v.get(EXIST_DATA));
                    targetValue.put(EDITED_TIME, k);
                    break;
                }
            }
        }
        targetValue.put(ID, id);
        return targetValue.toJSON();
    }

    private JSONObject findRawDataById(String id) {
        ObjectId objectId = new ObjectId(id);
        Map rawData = this.mongoTemplate.findById(objectId, Map.class, this.collection);
        return new JSONObject(rawData);
    }

    public void update(JSONObject data) {
        ObjectId objectId = new ObjectId(data.getString(ID));
        JSONObject rawData = this.findRawDataById(objectId.toString());
        data.remove(ID);
        rawData.remove(ID);

        data.put(EDITED_TIME, this.formatDateString(new Date()));

        DataMap currentValue = DataMap.parse(rawData.getJSONObject(CURRENT_VALUE));

        List<Map> updates = currentValue.compare(data);

        rawData.put(currentValue.getString(EDITED_TIME), new LinkedHashMap<String, Object>(){{
            put(EXIST_DATA, updates.get(0));
            put(NON_EXIST_DATA, updates.get(1));
        }});
        rawData.put(CURRENT_VALUE, data);

        Update update = new Update();
        Document document = Document.parse(JSON.toJSONString(rawData, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.WriteMapNullValue));
        for (Entry<String, Object> entry : document.entrySet()) {
            update.set(entry.getKey(), entry.getValue());
        }
        Query query = new Query(Criteria.where(ID).is(objectId));

        this.mongoTemplate.updateFirst(query, update, this.collection);
    }

    private JSONObject sortByDateKey(JSONObject data) {
        List<Entry<String, Object>> list = new ArrayList<>(data.entrySet());
        Collections.sort(list, (o1, o2) -> o2.getKey().compareTo(o1.getKey()));
        return new JSONObject(list.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue, (key1,key2) -> key2, LinkedHashMap::new)));
    }
}
