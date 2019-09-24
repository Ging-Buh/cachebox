package CB_Locator.Map;

import com.thebuzzmedia.sjxp.XMLParser;
import com.thebuzzmedia.sjxp.rule.DefaultRule;
import com.thebuzzmedia.sjxp.rule.IRule;
import com.thebuzzmedia.sjxp.rule.IRule.Type;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TmsMap {
    public String url;
    public String name;
    private List<IRule<Map<String, String>>> ruleList;
    public TmsMap(String file) throws Exception {
        System.setProperty("sjxp.namespaces", "false");
        Map<String, String> values = new HashMap<>();
        ruleList = new ArrayList<>();
        createCustomMultiLayerMapSourceRules();
        createMultiLayersRules();
        createCustomMapSourceRules();
        @SuppressWarnings("unchecked")
        XMLParser<Map<String, String>> parserCache = new XMLParser<>(ruleList.toArray(new IRule[0]));
        parserCache.parse(new FileInputStream(file), values);
    }

    private void createCustomMultiLayerMapSourceRules() throws Exception {

        // Basic GPX Rules

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/customMultiLayerMapSource/name") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                // String layerName = text;
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/customMultiLayerMapSource/tileType") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                // String tileType = text;
            }
        });
    }

    private void createMultiLayersRules() {
        ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/customMultiLayerMapSource/layers/customMapSource") {
            @Override
            public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values) {

                if (isStartTag) {
                    values.clear();
                } else {
                    if (name == null) {
                        name = values.get("name");
                    }
                    if (url == null) {
                        url = values.get("url");
                    }
                }

            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/customMultiLayerMapSource/layers/customMapSource/name") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("name", text);
            }
        });
        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/customMultiLayerMapSource/layers/customMapSource/minZoom") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("minZoom", text);
            }
        });
        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/customMultiLayerMapSource/layers/customMapSource/maxZoom") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("maxZoom", text);
            }
        });
        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/customMultiLayerMapSource/layers/customMapSource/tileType") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("tileType", text);
            }
        });
        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/customMultiLayerMapSource/layers/customMapSource/url") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("url", text);
            }
        });
    }

    private void createCustomMapSourceRules() {
        ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/customMapSource") {
            @Override
            public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values) {

                if (isStartTag) {
                    values.clear();
                } else {
                    if (name == null) {
                        name = values.get("name");
                    }
                    if (url == null) {
                        url = values.get("url");
                    }
                }

            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/customMapSource/name") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("name", text);
            }
        });
        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/customMapSource/minZoom") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("minZoom", text);
            }
        });
        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/customMapSource/maxZoom") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("maxZoom", text);
            }
        });
        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/customMapSource/tileType") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("tileType", text);
            }
        });
        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/customMapSource/url") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("url", text);
            }
        });
    }

}
