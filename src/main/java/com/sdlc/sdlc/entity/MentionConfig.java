
package com.sdlc.sdlc.entity;

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

    private Map<String, MentionDetails> mentions;

    @Data
    public static class MentionDetails {
        private String mentionType;
        private boolean click;
        private String searchParam;
        private List<String> showValues;
        private String mentionTextColor;
        private String mentionBgColor;
    }
}
