package com.visualthreat.api.tests.common;

import com.fasterxml.jackson.annotation.JsonGetter;
import java.beans.ConstructorProperties;
import java.util.List;

public class TestPoint {
  public static int DEFAULT_SORT_ORDER = 1000;
  private final String id;
  private final String name;
  private final String description;
  private final TestCategory category;
  private final List<TestParameter> params;
  private final List<String> depends;
  private final int sortOrder;

  @ConstructorProperties({"id", "name", "description", "category", "params", "depends", "sortOrder"})
  public TestPoint(String id, String name, String description, TestCategory category, List<TestParameter> params, List<String> depends, int sortOrder) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.category = category;
    this.params = params;
    this.depends = depends;
    this.sortOrder = sortOrder;
  }

  public String getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public String getDescription() {
    return this.description;
  }

  public TestCategory getCategory() {
    return this.category;
  }

  public List<TestParameter> getParams() {
    return this.params;
  }

  public List<String> getDepends() {
    return this.depends;
  }

  public int getSortOrder() {
    return this.sortOrder;
  }

  public String toString() {
    return "TestPoint(id=" + this.getId() + ", name=" + this.getName() + ", description=" + this.getDescription() + ", category=" + this.getCategory() + ", params=" + this.getParams() + ", depends=" + this.getDepends() + ", sortOrder=" + this.getSortOrder() + ")";
  }
}
