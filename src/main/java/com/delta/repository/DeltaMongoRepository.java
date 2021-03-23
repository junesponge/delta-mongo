package com.delta.repository;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.delta.exception.NotSupportException;
import com.delta.util.CriteriaUtil;
import com.delta.util.DataMap;
import com.delta.util.DateTimeUtil;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.delta.util.Constants.*;

/**
 * @Desription:
 * @Author: yuzhuojun
 * @CreateDate: 2021/3/5 12:32 AM
 */
@Component
public class DeltaMongoRepository {

    public static void main(String[] args) {
        String connection = "mongodb://localhost:27017/test";
        MongoDatabaseFactory mongoDbFactory = new SimpleMongoClientDatabaseFactory(connection);
//        DeltaMongoRepository repo = new DeltaMongoRepository();
        DeltaMongoRepository repo = new DeltaMongoRepository(new MongoTemplate(mongoDbFactory), "test");
        repo.criteriaUtil = new CriteriaUtil();

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
//        JSONObject b1 = new JSONObject();
//        b1.put("b2", "b2");
//        json.put("b1", b1);
//        JSONObject c1 = new JSONObject();
//        c1.put("c2", "c2");
//        json.put("c1", c1);
//        JSONObject d1 = new JSONObject();
//        JSONArray d1a = new JSONArray();
//        d1a.add("d1a-change");
//        d1a.add("d1b-change");
//        d1.put("d1a", d1a);
//        json.put("d1", d1);

//        repo.insert(json);
//        System.out.println(repo.findById("6044e99225751213a8d83ff3"));
//          System.out.println(repo.findAll());
        Document queryDocument = new Document();
        queryDocument.put("a1.a2.a3", "a3-change");
        queryDocument.put("b1.b2", "b2");
        Document fieldsDocument = new Document();
        /*fieldsDocument.put("c1.c2", true);
        fieldsDocument.put("b1", true);*/
        fieldsDocument.put("a1", true);
        fieldsDocument.put("b1", true);
       /* BasicQuery query = new BasicQuery(queryDocument, fieldsDocument);
        Document sortDocument = new Document();
        sortDocument.put("b1.b2", -1);
        query.setSortObject(sortDocument);*/
        Query query = new Query();
        query.addCriteria(Criteria.where(ID).is(new ObjectId("6045c5721dd50c3b86954439")));
        query.addCriteria(Criteria.where("a1.a2.a3").is("a3-change"));
        query.addCriteria(Criteria.where("b1.b2").is("b3"));
        query.addCriteria(Criteria.where("b1.b22").gt(5));
//        query.fields().include("a1");
//        query.fields().elemMatch("c1", Criteria.where("c1k").is("c1v"));
        query.with(Sort.by(Sort.Order.asc("a1.a2.a3")));

        System.out.println(repo.findByQuery(query));
//        repo.update(json);
//        Date date = repo.parseStringDate("2021-03-07T22:56:18Z");
//        Date date1 = new Date(date.getTime() + 1);
//        System.out.println(repo.findByIdAndDate("6044e99225751213a8d83ff3", date1));
    }

    public DeltaMongoRepository() {}

    public DeltaMongoRepository(MongoTemplate mongoTemplate, String collection) {
        this.mongoTemplate = mongoTemplate;
        this.collection = collection;
    }

    @Autowired
    private CriteriaUtil criteriaUtil;

    @Autowired
    private MongoTemplate mongoTemplate;

    private String collection;

    public JSONObject insert(JSONObject data) {
        JSONObject json = new JSONObject();
        data.put(EDITED_TIME, DateTimeUtil.formatDateString(new Date()));
        json.put(CURRENT, data);
        JSONObject inserted = this.mongoTemplate.insert(json, this.collection);
        return this.getCurrent(inserted);
    }

    public JSONArray findAll() {
        return this.findByQuery(null);
    }

    public JSONObject findById(String id) {
        return this.getCurrent(this.findRawDataById(id));
    }

    public JSONObject findByIdAndDate(String id, Date date) {
        JSONObject rawData = this.findRawDataById(id);
        return this.findByDate(rawData, date);
    }

    private JSONObject findRawDataById(String id) {
        ObjectId objectId = new ObjectId(id);
        Map rawData = this.mongoTemplate.findById(objectId, Map.class, this.collection);
        return new JSONObject(rawData);
    }

    public JSONArray findByQuery(Query query) {
        JSONArray rawDataByQuery = this.findRawDataByQuery(query);
        return new JSONArray(rawDataByQuery.stream().map(o -> this.getCurrent(new JSONObject((Map) o))).collect(Collectors.toList()));
    }

    public JSONArray findByQueryAndDate(Query query, Date date) {
        JSONArray rawData = this.findRawDataByQuery(query);
        JSONArray jsonArray = new JSONArray(rawData.stream().map(o -> this.findByDate(new JSONObject((Map) o), date)).collect(Collectors.toList()));
        jsonArray.removeIf(Predicate.isEqual(null));
        return jsonArray;
    }

    private JSONArray findRawDataByQuery(Query query) {
        try {
            if (null == query) {
                Document fieldsObject = new Document();
                fieldsObject.put(ID, true);
                fieldsObject.put(CURRENT, true);
                query = new BasicQuery(new Document(), fieldsObject);
            } else {
                query = this.criteriaUtil.deltaWithQuery(query);
                query = this.criteriaUtil.deltaWithProjection(query);
                query = this.criteriaUtil.deltaWithSort(query);
            }
        } catch (NotSupportException e) {
            e.printStackTrace();
        }
        return new JSONArray().fluentAddAll(this.mongoTemplate.find(query, Map.class, this.collection));
    }

    private JSONObject findByDate(JSONObject rawData, Date date) {
        String id = new String(rawData.getString(ID));
        rawData.remove(ID);
        rawData = this.sortByDateKey(rawData);

        // The data has not been created
        Entry<String, Object> entry = (Entry<String, Object>) rawData.entrySet().toArray()[rawData.size() - 1];
        boolean hasNotCreated = ((rawData.size() > 1
                && date.getTime() < DateTimeUtil.parseStringDate(entry.getKey()).getTime())
                || (rawData.size() == 1
                && date.getTime() < DateTimeUtil.parseStringDate(String.valueOf(((Map) entry.getValue()).get(EDITED_TIME))).getTime()));
        if (hasNotCreated) {
            return null;
        }

        DataMap targetValue = null;
        for (Iterator<Entry<String, Object>> it = rawData.entrySet().iterator(); it.hasNext();) {
            Entry<String, Object> e = it.next();
            String k = e.getKey();
            DataMap<String, Map> v = new DataMap((Map) e.getValue());
            if (CURRENT.equals(k)) {
                targetValue = v;
                if (date.getTime() >= DateTimeUtil.parseStringDate(v.getString(EDITED_TIME)).getTime()) {
                    break;
                }
            } else {
                if (date.getTime() < DateTimeUtil.parseStringDate(k).getTime()) {
                    targetValue.merge(v.get(ABSENT), v.get(PRESENT));
                } else if (date.getTime() >= DateTimeUtil.parseStringDate(k).getTime()) {
                    targetValue.merge(v.get(ABSENT), v.get(PRESENT));
                    targetValue.put(EDITED_TIME, k);
                    break;
                }
            }
        }
        targetValue.put(ID, id);
        return targetValue.toJSON();
    }

    public void update(JSONObject data) {
        ObjectId objectId = new ObjectId(data.getString(ID));
        JSONObject rawData = this.findRawDataById(objectId.toString());
        data.remove(ID);
        rawData.remove(ID);

        data.put(EDITED_TIME, DateTimeUtil.formatDateString(new Date()));

        DataMap currentValue = DataMap.parse(rawData.getJSONObject(CURRENT));

        List<Map> updates = currentValue.compare(data);

        rawData.put(currentValue.getString(EDITED_TIME), new LinkedHashMap<String, Object>(){{
            put(PRESENT, updates.get(0));
            put(ABSENT, updates.get(1));
        }});
        rawData.put(CURRENT, data);

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

    private JSONObject SerializeObjectId(JSONObject data) {
        SerializeConfig config = new SerializeConfig();
        config.put(ObjectId.class, (jsonSerializer, o, o1, type, i) -> {
            SerializeWriter out = jsonSerializer.getWriter();
            if (o == null) {
                jsonSerializer.getWriter().writeNull();
                return;
            }
            out.write("\"" + ((ObjectId) o).toString() + "\"");
        });
        return JSONObject.parseObject(JSON.toJSONString(data, config));
    }

    private JSONObject getCurrent(JSONObject data) {
        return data.getJSONObject(CURRENT).fluentPut(ID, data.get(ID).toString());
    }
}
