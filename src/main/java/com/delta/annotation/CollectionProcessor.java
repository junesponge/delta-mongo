package com.delta.annotation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;

/**
 * @Desription:
 * @Author: yuzhuojun
 * @CreateDate: 2021/3/11 12:39 AM
 */
@Configuration
public class CollectionProcessor implements BeanPostProcessor {

    private static final String CONTROLLER = "controller";
    private static final String COLLECTION = "collection";

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        if (clazz.getPackage().getName().endsWith(CONTROLLER)) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                Collection collection = field.getAnnotation(Collection.class);
                if (null != collection) {
                    try {
                        Field collectionField = field.getType().getDeclaredField(COLLECTION);
                        collectionField.setAccessible(true);
                        field.setAccessible(true);
                        collectionField.set(field.get(bean), collection.value());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        return bean;
    }

}
