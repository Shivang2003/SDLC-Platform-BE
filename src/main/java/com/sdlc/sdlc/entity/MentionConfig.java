
package com.sdlc.sdlc.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Document(collection = "mention_configs")
public class MentionConfig {

    @Id
    private String id;

    @JsonProperty("requiredMentions")
    private Map<String, MentionDetails> mentions;

    @Data
    public static class MentionDetails {
        private String mentionType;
        private boolean click;
        private String searchParam;
        private String displayText;
        private String baseURL;
        private List<ShowValue> showValues;
        private String mentionTextColor;
        private String mentionBgColor;
        private List<Map<String, Object>> values;
    }

    @Data
    public static class ShowValue {
        private String item;
        private Map<String, String> style;
        private String icon;
    }
}
