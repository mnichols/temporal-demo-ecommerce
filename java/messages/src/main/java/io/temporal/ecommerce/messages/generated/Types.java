package io.temporal.ecommerce.messages.generated;

import java.util.Map;

public class Types {
  
  public static class ValidateRequestInput {
    private String value;
  
    public ValidateRequestInput(Map<String, Object> args) {
      if (args != null) {
        this.value = (String) args.get("value");
      }
    }
  
    public String getValue() { return this.value; }
    public void setValue(String value) { this.value = value; }
  }
  
}
