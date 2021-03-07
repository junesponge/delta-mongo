package com.delta.util;

import com.alibaba.fastjson.JSONObject;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * @Desription:
 * @Author: yuzhuojun
 * @CreateDate: 2021/3/5 5:43 PM
 */
@NoArgsConstructor
public class DataMap<K, V> extends HashMap<K, V> {

    public DataMap(Map map) {
        this.putAll(map);
    }

    public static <K, V> DataMap<K, V> parse(JSONObject o) {
        return new DataMap<>(o);
    }

    public static <K, V> DataMap<K, V> parse(String jsonString) {
        return DataMap.parse(JSONObject.parseObject(jsonString));
    }

    public <T> T get(String key, Class<T> clazz) {
        Object v = super.get(key);
        if (null != v) {
            return (T) v;
        } else {
            return null;
        }
    }

    public String getString(String key) {
        return this.get(key, String.class);
    }

    public Object getByPath(String path) {
        String[] paths = path.split("\\.");
        Object o = this;
        for (String name : paths) {
            if (o instanceof Map) {
                if (name.indexOf("[") == -1) {
                    o = ((Map) o).get(name);
                } else {
                    int beginBracketIndex = name.indexOf("[");
                    int endBracketIndex = name.indexOf("]");
                    int index = Integer.valueOf(name.substring(beginBracketIndex + 1, endBracketIndex));
                    name = name.substring(0, beginBracketIndex);
                    o = ((List) ((Map) o).get(name)).get(index);
                }
            }
        }
        return o;
    }

    public <T> T getByPath(String path, Class<T> clazz) {
        return (T) this.getByPath(path);
    }

    public void putByPath(String path, V value) {
        String[] paths = path.split("\\.");
        if (paths.length > 1) {
            Map m = (Map) this.get(paths[0]);
            if (null == m) {
                m = new DataMap<K, V>();
                this.put((K) paths[0], (V) m);
            }
            this.putByPath(path.substring(path.indexOf(".") + 1), value, m);
        } else {
            this.put((K) path, value);
        }
    }

    private void putByPath(String path, Object value, Map m) {
        String[] paths = path.split("\\.");
        if (paths.length > 1) {
            Map n = (Map) m.get(paths[0]);
            if (null == n) {
                n = new DataMap<K, V>();
                m.put(paths[0], n);
            }
            this.putByPath(path.substring(path.indexOf(".") + 1), value, n);
        } else {
            m.put(path, value);
        }
    }

    public DataMap increase(Map increaseMap) {
        if (null == increaseMap) {
            return this;
        }

        increaseMap.forEach((k, v) -> {
            Object cv = this.get(k);
            if (null != cv && cv instanceof Map && v instanceof Map) {
                this.increase((Map) cv, (Map) v);
            } else {
                this.put((K) k, (V) v);
            }
        });

        return this;
    }

    private Map increase(Map currentMap, Map increaseMap) {
        increaseMap.forEach((k, v) -> {
            Object cv = currentMap.get(k);
            if (null != cv && cv instanceof Map && v instanceof Map) {
                this.increase((Map) cv, (Map) v);
            } else {
                currentMap.put(k, v);
            }
        });

        return currentMap;
    }

    public DataMap decrease(Map decreaseMap) {
        if (null == decreaseMap) {
            return this;
        }

        decreaseMap.forEach((k, v) -> {
            Object cv = this.get(k);
            if (null != cv && cv instanceof Map && v instanceof Map) {
                Map decreased = this.decrease((Map) cv, (Map) v);
                if (decreased.size() == 0) {
                    this.remove(k);
                }
            } else {
                this.remove(k);
            }
        });

        return this;
    }

    private Map decrease(Map currentMap, Map decreaseMap) {
        decreaseMap.forEach((k, v) -> {
            Object cv = currentMap.get(k);
            if (null != cv && cv instanceof Map && v instanceof Map) {
                Map decreased = this.decrease((Map) cv, (Map) v);
                if (decreased.size() == 0) {
                    currentMap.remove(k);
                }
            } else {
                currentMap.remove(k);
            }
        });

        return currentMap;
    }

    /**
     * Return result
     * list[0] this exist but param map not exist
     * list[1] param map exist but this not exist
     * @param map
     * @return
     */
    public List<Map> compare(Map map) {
        List<Map> resultList = new ArrayList<Map>();
        Map resultMap1 = this.compare(new LinkedHashMap(), this, map);
        resultList.add(resultMap1);
        Map resultMap2 = this.compare(new LinkedHashMap(), map, this);
        resultList.add(resultMap2);
        return resultList;
    }

    private Map compare(Map resultMap, Map map1, Map map2) {
        for (Iterator<Entry> it = map1.entrySet().iterator(); it.hasNext();) {
            Entry e1 = it.next();
            Object k1 = e1.getKey();
            Object v1 = e1.getValue();
            Object v2 = map2.get(k1);
            if (null == v2) {
                resultMap.put(k1, v1);
            } else if (!v1.equals(v2)) {
                if (v1 instanceof Map && v2 instanceof Map) {
                    Map resultChildMap1 = new LinkedHashMap();
                    resultMap.put(k1, resultChildMap1);
                    this.compare(resultChildMap1, (Map) v1, (Map) v2);
                    if (resultChildMap1.size() == 0) {
                        resultMap.remove(k1);
                    }
                } else {
                    resultMap.put(k1, v1);
                }
            }
        }
        return resultMap;
    }

    public JSONObject toJSON() {
        return new JSONObject((Map<String, Object>) this);
    }
}
