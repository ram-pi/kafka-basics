package com.github.prametta.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Beer {

    @JsonProperty
    public String hop;

    @JsonProperty
    public String malt;

    @JsonProperty
    public String name;

    @JsonProperty
    public String style;

    @JsonProperty
    public String yeast;
}
