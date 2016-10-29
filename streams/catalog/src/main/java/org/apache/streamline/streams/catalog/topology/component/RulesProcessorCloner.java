package org.apache.streamline.streams.catalog.topology.component;

import org.apache.streamline.streams.catalog.RuleInfo;
import org.apache.streamline.streams.catalog.service.StreamCatalogService;
import org.apache.streamline.streams.layout.component.impl.RulesProcessor;
import org.apache.streamline.streams.layout.component.rule.Rule;

import java.util.List;

/**
 * Created by schendamaraikannan on 10/27/16.
 */
public final class RulesProcessorCloner implements Cloner<RulesProcessor> {
    public RulesProcessor clone(RulesProcessor rulesProcessor, StreamCatalogService streamCatalogService, Long topologyId) {
        List<Rule> rules = rulesProcessor.getRules();
        for (Rule rule : rules) {
            try {
                RuleInfo newRuleInfo = streamCatalogService.getRule(rule.getId()).getClone(topologyId);
                streamCatalogService.addRule(topologyId, newRuleInfo);
                rules.add(newRuleInfo.getRule());
            } catch (Exception e) {
                throw new RuntimeException(
                        String.format(
                                "Unexpected exeption thrown while fetching the rules from from Rules Processor: %s",
                                rulesProcessor.getId()),
                        e);
            }
        }

        RulesProcessor newRulesProcessor = new RulesProcessor();
        newRulesProcessor.setRules(rules);
        RulesProcessor clonedRulesProcessor = (RulesProcessor) new StreamlineProcessorCloner()
                .clone(newRulesProcessor, streamCatalogService, topologyId);
        return clonedRulesProcessor;
    }
}
