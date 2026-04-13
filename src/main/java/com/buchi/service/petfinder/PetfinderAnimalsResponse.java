package com.buchi.service.petfinder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PetfinderAnimalsResponse {

    private List<PetfinderAnimal> animals;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PetfinderAnimal {

        private Long id;
        private String type;
        private String gender;
        private String size;
        private String age;
        private String name;
        private String description;
        private String status;

        @JsonProperty("environment")
        private Environment environment;

        @JsonProperty("photos")
        private List<Photo> photos;

        @Getter
        @Setter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Environment {
            private Boolean children;
        }

        @Getter
        @Setter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Photo {
            private String small;
            private String medium;
            private String large;
            private String full;
        }
    }
}