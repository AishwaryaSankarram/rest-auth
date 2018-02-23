package com.carma.geoconfig.geoconfig.service.utils;

import java.util.Map;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class QueryUtil {

	public Query getQuery(Map<String, Object> params) {
        Query query = new Query();
        for (String key : params.keySet()) {
            Criteria c = null;
//            if (key.equals("alias")) {
//                c = Criteria.where(key).in((Collection<String>)params.get(key));
//            } else {
                c = Criteria.where(key).is(params.get(key));
//            }
            query.addCriteria(c);
        }
        return query;
    }
}
