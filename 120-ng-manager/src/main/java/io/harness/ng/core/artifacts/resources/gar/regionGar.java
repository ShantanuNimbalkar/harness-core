package io.harness.ng.core.artifacts.resources.gar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
public class regionGar {
  String name;
  String value;
  public regionGar(String name, String value) {
    this.name = name;
    this.value = value;
  }
}
