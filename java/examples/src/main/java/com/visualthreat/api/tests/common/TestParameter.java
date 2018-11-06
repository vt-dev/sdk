package com.visualthreat.api.tests.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TestParameter<T> {
  private String name;
  private String description;
  @JsonIgnore
  private Class<T> classTag;
  private String type;
  private List<T> values = Collections.emptyList();
  private T defaultValue;

  public TestParameter(String name, String description, Class<T> classTag, T defaultValue) {
    this.name = name;
    this.description = description;
    this.classTag = classTag;
    this.type = classTag.getSimpleName();
    if(classTag.isEnum()) {
      this.values = Arrays.asList(classTag.getEnumConstants());
    }

    this.defaultValue = defaultValue;
  }

  public TestParameter<T> withDefault(T defaultValue) {
    return new TestParameter(this.name, this.description, this.classTag, defaultValue);
  }

  public String getName() {
    return this.name;
  }

  public String getDescription() {
    return this.description;
  }

  public Class<T> getClassTag() {
    return this.classTag;
  }

  public String getType() {
    return this.type;
  }

  public List<T> getValues() {
    return this.values;
  }

  public T getDefaultValue() {
    return this.defaultValue;
  }

  public String toString() {
    return "TestParameter(name=" + this.getName() + ", description=" + this.getDescription() + ", classTag=" + this.getClassTag() + ", type=" + this.getType() + ", values=" + this.getValues() + ", defaultValue=" + this.getDefaultValue() + ")";
  }

  public boolean equals(Object o) {
    if(o == this) {
      return true;
    } else if(!(o instanceof TestParameter)) {
      return false;
    } else {
      TestParameter other = (TestParameter)o;
      if(!other.canEqual(this)) {
        return false;
      } else {
        String this$name = this.getName();
        String other$name = other.getName();
        if(this$name == null) {
          if(other$name != null) {
            return false;
          }
        } else if(!this$name.equals(other$name)) {
          return false;
        }

        return true;
      }
    }
  }

  protected boolean canEqual(Object other) {
    return other instanceof TestParameter;
  }

  public int hashCode() {
    byte result = 1;
    String $name = this.getName();
    int result1 = result * 59 + ($name == null?43:$name.hashCode());
    return result1;
  }
}
