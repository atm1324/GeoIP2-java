package com.maxmind.geoip2;


import java.lang.*;
import java.util.*;
import java.io.*;
import org.json.*;

public class City extends Country
{
  private CityRecord city;
  private LocationRecord location;
  private RepresentedCountry representedCountry;
  private Vector<SubDivision> subDivisionsList;
  City(JSONObject json) {
    super(json);
    try {
      JSONObject jcity = json.getJSONObject("city");
      city = new CityRecord(jcity);      
      JSONObject jlocation = json.getJSONObject("location");
      location = new LocationRecord(jlocation);
      if (json.has("represented_country")) {
        JSONObject rcountry = json.getJSONObject("represented_country");
        representedCountry = new RepresentedCountry(rcountry);
      }
      subDivisionsList = new Vector<SubDivision>();
      JSONArray subDivisionsArray = json.getJSONArray("subdivisions");
      int length = subDivisionsArray.length();
      for (int i = 0;i < length;i++) {
        JSONObject jsubDivision = subDivisionsArray.getJSONObject(i);
        subDivisionsList.add(new SubDivision(jsubDivision));
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }  
  }
  public CityRecord getCity() {
    return city;
  }
  public LocationRecord getLocation() {
    return location;
  }
  public RepresentedCountry getRepresentedCountry() {
    return representedCountry;
  }
  public Vector<SubDivision> getSubDivisionsList() {
    return subDivisionsList;
  }
}

