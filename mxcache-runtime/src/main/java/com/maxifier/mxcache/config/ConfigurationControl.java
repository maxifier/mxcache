package com.maxifier.mxcache.config;

import com.maxifier.mxcache.mbean.ConfigurationControlMXBean;
import com.maxifier.mxcache.mbean.RuleInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 16.08.2010
 * Time: 14:29:46
 */
public class ConfigurationControl implements ConfigurationControlMXBean {
    private final MxCacheConfigProviderImpl provider;

    public ConfigurationControl(MxCacheConfigProviderImpl provider) {
        this.provider = provider;
    }

    @Override
    public List<RuleInfo> getRules() {
        List<RuleInfo> res = new ArrayList<RuleInfo>();
        for (RuleWithSelector rule : provider.getRules()) {
            List<Selector> sel = rule.getSelectors();
            String[] selectors = new String[sel.size()];
            int i = 0;
            for (Selector selector : sel) {
                selectors[i++] = selector.toString();
            }
            res.add(new RuleInfo(rule.getName(), rule.getSource(), selectors, rule.getTrackDependency(), rule.getStrategy().toString()));
        }
        return res;
    }
}
