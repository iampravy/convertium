package com.convertium.stageone.core.utils;

import com.convertium.stageone.core.helpers.ContentFragmentArticle;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.*;
import java.util.stream.Collectors;

public class CommonUtility {

    private static final Logger log = LoggerFactory.getLogger(CommonUtility.class);

    /**
     * Need to configure UserMapperService - writeservice
     *
     * @param resolverFactory a service factory object
     * @return resourceResolverWriter  writer object
     */
    public static ResourceResolver getWriterResourcerResolver(ResourceResolverFactory resolverFactory) {
        ResourceResolver resourceResolverWriter = null;
        Map<String, Object> param = new HashMap<String, Object>();
        param.put(ResourceResolverFactory.SUBSERVICE, "writeservice");
        try {
            resourceResolverWriter = resolverFactory.getServiceResourceResolver(param);
        } catch (LoginException re) {
            log.error("Error at ArticleFormServlet - getWriterResourcerResolver", re);
        }
        return resourceResolverWriter;
    }

    public static SearchResult getQueryResults(String rootPath, QueryBuilder builder, Session session) {

        Map<String, String> predicateMap = new HashMap<>();
        predicateMap.put("path", rootPath);
        predicateMap.put("type", "dam:Asset");
        predicateMap.put("boolproperty", "jcr:content/contentFragment");
        predicateMap.put("boolproperty.value", "true");
        predicateMap.put("property", "jcr:content/data/cq:model");
        predicateMap.put("property.1_value", "/conf/convertium/settings/dam/cfm/models/article-model");
        predicateMap.put("orderby", "@jcr:content/data/master/publishDate");
        predicateMap.put("orderby.sort", "desc");
        predicateMap.put("p.limit", "10");

        Query query = builder.createQuery(PredicateGroup.create(predicateMap), session);

        return query.getResult();
    }

    public static List<ContentFragmentArticle> getContentFragmentArticles(ResourceResolver resolver, SearchResult result) {
        List<ContentFragmentArticle> articles = result.getHits().stream().
                map(hit -> {
                    try {
                        return resolver.resolve(hit.getPath()).adaptTo(ContentFragmentArticle.class);
                    } catch (RepositoryException re) {
                        log.error("Error collecting search results", re);
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());
        if (Objects.nonNull(articles)) {
            return articles;
        }
        return new ArrayList<>();
    }

}
