/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.convertium.stageone.core.schedulers;

import com.convertium.stageone.core.api.models.HighlightsModel;
import com.convertium.stageone.core.helpers.ContentFragmentArticle;
import com.convertium.stageone.core.utils.CommonUtility;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.Replicator;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A simple demo for cron-job like tasks that get executed regularly.
 * It also demonstrates how property values can be set. Users can
 * set the property values in /system/console/configMgr
 */
@Designate(ocd = ArticlePublishScheduledTask.Config.class)
@Component(service = Runnable.class)
public class ArticlePublishScheduledTask implements Runnable {

    @ObjectClassDefinition(name = "Convertium Article Publish scheduled task",
            description = "Convertium Article Publish for cron-job like task with properties")
    public static @interface Config {

        @AttributeDefinition(name = "Cron-job expression")
        String scheduler_expression() default "0 1 0 ? * *";

        @AttributeDefinition(name = "Concurrent task",
                description = "Whether or not to schedule this task concurrently")
        boolean scheduler_concurrent() default false;

        @AttributeDefinition(name = "A parameter",
                description = "Can be configured in /system/console/configMgr")
        String articleFragmentPath() default "/content/dam/convertium-article-content-fragments/us/en";
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String articleFragmentPath;

    @Reference
    private QueryBuilder builder;

    private ResourceResolver resolver;

    @Reference
    private ResourceResolverFactory resolverFactory;

    private static final String ASSETS_CONTENT_ROOT = "/content/dam/convertium-article-content-fragments/us/en";

    @Reference
    private Replicator replicator;

    @Override
    public void run() {
        logger.debug("ArticlePublishScheduledTask is now running, articleFragmentPath='{}'", articleFragmentPath);
        try {
            resolver = CommonUtility.getWriterResourcerResolver(resolverFactory);
            Session session = resolver.adaptTo(Session.class);
            Resource resource = resolver.getResource(ASSETS_CONTENT_ROOT);
            if (Objects.nonNull(resource)) {
                SearchResult result = CommonUtility.getQueryResults(ASSETS_CONTENT_ROOT, builder, session);
                List<ContentFragmentArticle> list = CommonUtility.getContentFragmentArticles(resolver, result);
                List<HighlightsModel> highlightsModelList = new ArrayList<>();
                if (list != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String todaysDate = sdf.format(Calendar.getInstance().getTime());
                    for (ContentFragmentArticle article : list) {
                        String path = article.getContentFragmentPath();
                        String articleDate = sdf.format(article.getArticlePublishDate());
                        if (todaysDate.equals(articleDate)) {
                            replicator.replicate(session, ReplicationActionType.ACTIVATE, path);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("ArticlePublishScheduledTask - something went wrong ", e);
        }
    }

    @Activate
    protected void activate(final Config config) {
        articleFragmentPath = config.articleFragmentPath();
    }

}
