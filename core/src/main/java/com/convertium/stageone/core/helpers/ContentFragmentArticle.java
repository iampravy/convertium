package com.convertium.stageone.core.helpers;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;
import com.adobe.cq.dam.cfm.FragmentData;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.ExporterOption;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Calendar;
import java.util.Optional;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@Exporter(name = "jackson", extensions = "json", options = {@ExporterOption(name = "SerializationFeature.WRITE_DATES_AS_TIMESTAMPS", value = "true")})
public class ContentFragmentArticle {
    private static final Logger log = LoggerFactory.getLogger(ContentFragmentArticle.class);

    @Inject
    @Self
    private Resource resource;

    private Optional<ContentFragment> contentFragment;

    @Reference
    private ResourceResolverFactory resolverFactory;

    @PostConstruct
    public void init() {
        contentFragment = Optional.ofNullable(resource.adaptTo(ContentFragment.class));
    }

/*    public String getModelTitle() {
        return contentFragment.map((ContentFragment::getTemplate).map(FragmentTemplate::getTitle)).orElse(StringUtils.EMPTY);
    }*/

    public String getArticleTitle() {
        return contentFragment.map(cf -> cf.getElement("articleTitle")).map(ContentElement::getContent).orElse(StringUtils.EMPTY);
    }

    public String getArticleContent() {
        return contentFragment.map(cf -> cf.getElement("articleContent")).map(ContentElement::getContent).orElse(StringUtils.EMPTY);
    }

    public String getContentFragmentPath() {
        return resource.getPath();
    }

    public Calendar getArticlePublishDate() {
        Optional optional = contentFragment.map(cf -> cf.getElement("articlePublishDate")).map(ContentElement::getValue).map(FragmentData::getValue);
        Calendar calendar = optional.isPresent() && optional.get() instanceof Calendar ? (Calendar) optional.get() : null;
        return calendar;
    }
}
