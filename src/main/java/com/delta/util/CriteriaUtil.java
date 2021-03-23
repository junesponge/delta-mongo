package com.delta.util;

import com.delta.exception.NotSupportException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.delta.util.Constants.CURRENT;
import static com.delta.util.Constants.ID;

/**
 * @Desription:
 * @Author: yuzhuojun
 * @CreateDate: 2021/3/14 5:18 PM
 */
@Component
public class CriteriaUtil {

    public Query deltaWithQuery(Query query) {
        // Query
        Query finalQuery = query;
        query.getQueryObject().forEach((k, v) -> {
            if (!k.equals(ID)) {
                try {
                    java.lang.reflect.Field criteriaField = finalQuery.getClass().getDeclaredField("criteria");
                    criteriaField.setAccessible(true);
                    Map<String, CriteriaDefinition> criteria = (Map<String, CriteriaDefinition>) criteriaField.get(finalQuery);
                    CriteriaDefinition criteriaDefinition = criteria.get(k);
                    java.lang.reflect.Field keyField = criteriaDefinition.getClass().getDeclaredField("key");
                    keyField.setAccessible(true);
                    keyField.set(criteriaDefinition, CURRENT + "." + keyField.get(criteriaDefinition));
                    criteria.put(CURRENT + "." + k, criteriaDefinition);
                    criteria.remove(k);
                    criteriaField.set(finalQuery, criteria);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
        });
        return query;
    }

    public Query deltaWithProjection(Query query) throws NotSupportException {
        // Projection
        try {
            java.lang.reflect.Field fieldSpecField = query.getClass().getDeclaredField("fieldSpec");
            fieldSpecField.setAccessible(true);
            Field fieldSpec = (Field) fieldSpecField.get(query);

            if (null != fieldSpec) {
                java.lang.reflect.Field criteriaField = fieldSpec.getClass().getDeclaredField("criteria");
                criteriaField.setAccessible(true);
                Map<String, Integer> criteria = (Map<String, Integer>) criteriaField.get(fieldSpec);
                for (Iterator<Map.Entry<String, Integer>> it = criteria.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<String, Integer> e = it.next();
                    String k = e.getKey();
                    if (!k.equals(ID)) {
                        criteria.put(CURRENT + "." + k, e.getValue());
                        criteria.remove(k);
                    }
                }
                criteriaField.set(fieldSpec, criteria);

                java.lang.reflect.Field slicesField = fieldSpec.getClass().getDeclaredField("slices");
                slicesField.setAccessible(true);
                Map<String, Object> slices = (Map<String, Object>) slicesField.get(fieldSpec);
                for (Iterator<Map.Entry<String, Object>> it = slices.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<String, Object> e = it.next();
                    String k = e.getKey();
                    if (!k.equals(ID)) {
                        slices.put(CURRENT + "." + k, e.getValue());
                        slices.remove(k);
                    }
                }
                slicesField.set(fieldSpec, slices);

                java.lang.reflect.Field elemMatchsField = fieldSpec.getClass().getDeclaredField("elemMatchs");
                elemMatchsField.setAccessible(true);
                Map<String, Criteria> elemMatchs = (Map<String, Criteria>) elemMatchsField.get(fieldSpec);
                if (elemMatchs.size() > 0) {
                    throw new NotSupportException("$elemMatch in projection is not support in delta mongo.");
                }
                /*for (Iterator<Entry<String, Criteria>> it = elemMatchs.entrySet().iterator(); it.hasNext();) {
                    Entry<String, Criteria> e = it.next();
                    String k = e.getKey();
                    Criteria v= e.getValue();
                    if (!k.equals(ID)) {
                        elemMatchs.put(CURRENT + "." + k, v);
                        elemMatchs.remove(k);
                    }
                }
                elemMatchsField.set(fieldSpec, elemMatchs);*/
                java.lang.reflect.Field positionKeyField = fieldSpec.getClass().getDeclaredField("positionKey");
                positionKeyField.setAccessible(true);
                String positionKey = (String) positionKeyField.get(fieldSpec);
                if (null != positionKey) {
                    positionKeyField.set(fieldSpec, CURRENT + "." + positionKey);
                }

                fieldSpec.include(ID);

                fieldSpecField.set(query, fieldSpec);
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return query;
    }

    public Query deltaWithSort(Query query) {
        // Sort
        try {
            java.lang.reflect.Field sortField = query.getClass().getDeclaredField("sort");
            sortField.setAccessible(true);
            Sort sort = (Sort) sortField.get(query);
            java.lang.reflect.Field ordersField = sort.getClass().getDeclaredField("orders");
            ordersField.setAccessible(true);
            List<Sort.Order> orders = (List<Sort.Order>) ordersField.get(sort);
            for (Sort.Order order : orders) {
                if (!order.getProperty().equals(ID)) {
                    java.lang.reflect.Field propertyField = order.getClass().getDeclaredField("property");
                    propertyField.setAccessible(true);
                    propertyField.set(order, CURRENT + "." + order.getProperty());
                }
            }
            ordersField.set(sort, orders);
            sortField.set(query, sort);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return query;
    }

}
