package com.buchi.service.dogapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DogApiResponse {

    private String id;
    private String url;
    private Integer width;
    private Integer height;

    @JsonProperty("breeds")
    private List<Breed> breeds;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Breed {
        private String name;
        private String temperament;
        private String life_span;

        @JsonProperty("weight")
        private Measurement weight;

        @Getter
        @Setter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Measurement {
            private String imperial;
            private String metric;
        }
    }
}