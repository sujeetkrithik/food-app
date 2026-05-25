/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositoryservices;

import ch.hsr.geohash.GeoHash;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.globals.GlobalConstants;
import com.crio.qeats.models.RestaurantEntity;
import com.crio.qeats.repositories.RestaurantRepository;
import com.crio.qeats.utils.GeoLocation;
import com.crio.qeats.utils.GeoUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Provider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;


@Service
@Primary
public class RestaurantRepositoryServiceImpl implements RestaurantRepositoryService {

  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private RestaurantRepository restaurantRepository;

  @Autowired
  private Provider<ModelMapper> modelMapperProvider;

  private boolean isOpenNow(LocalTime time, RestaurantEntity res) {
    LocalTime openingTime = LocalTime.parse(res.getOpensAt());
    LocalTime closingTime = LocalTime.parse(res.getClosesAt());

    // return time.isAfter(openingTime) && time.isBefore(closingTime);
    return !time.isBefore(openingTime) && !time.isAfter(closingTime);
  }

  // TODO: CRIO_TASK_MODULE_NOSQL
  // Objectives:
  // 1. Implement findAllRestaurantsCloseby.
  // 2. Remember to keep the precision of GeoHash in mind while using it as a key.
  // Check RestaurantRepositoryService.java file for the interface contract.
  public List<Restaurant> findAllRestaurantsCloseBy(Double latitude,
      Double longitude, LocalTime currentTime, Double servingRadiusInKms) {

    List<Restaurant> restaurants = new ArrayList<>();

    restaurantRepository.findAll();

    // final int GEOHASH_PRECISION = 7;

    // GeoHash geoHash = GeoHash.withCharacterPrecision(latitude, longitude, GEOHASH_PRECISION);

    // Set<String> geoHashList = new HashSet<>();
    //  geoHashList.add(geoHash.toBase32());

    //  for(GeoHash neighbor : geoHash.getAdjacent()){
    //   geoHashList.add(neighbor.toBase32());
    //  }

    //  Query query = new Query();
    //  query.addCriteria(Criteria.where("geoHash").in(geoHashList));

    //  List<RestaurantEntity> restaurantEntities = mongoTemplate.find(query, 
    //  RestaurantEntity.class);

    List<RestaurantEntity> restaurantEntities =
    mongoTemplate.findAll(RestaurantEntity.class);

     if(restaurantEntities == null || restaurantEntities.isEmpty()){
      return restaurants;
     }

     ModelMapper modelMapper = modelMapperProvider.get();

      //CHECKSTYLE:OFF
      //CHECKSTYLE:ON
      for(RestaurantEntity entity : restaurantEntities){
        if(isRestaurantCloseByAndOpen(entity, currentTime, latitude, longitude, servingRadiusInKms)){
          restaurants.add(modelMapper.map(entity, Restaurant.class));
        }

      }

    return restaurants;
}

  // TODO: CRIO_TASK_MODULE_NOSQL
  // Objective:
  // 1. Check if a restaurant is nearby and open. If so, it is a candidate to be returned.
  // NOTE: How far exactly is "nearby"?

  /**
   * Utility method to check if a restaurant is within the serving radius at a given time.
   * @return boolean True if restaurant falls within serving radius and is open, false otherwise
   */
  private boolean isRestaurantCloseByAndOpen(RestaurantEntity restaurantEntity,
      LocalTime currentTime, Double latitude, Double longitude, Double servingRadiusInKms) {

    // if (isOpenNow(currentTime, restaurantEntity)) {
    //   return GeoUtils.findDistanceInKm(latitude, longitude,
    //       restaurantEntity.getLatitude(), restaurantEntity.getLongitude())
    //       < servingRadiusInKms;
    // }

    // return false;

    if (!isOpenNow(currentTime, restaurantEntity)) {
      return false;
    }

    double distance = GeoUtils.findDistanceInKm(
      latitude,
      longitude,
      restaurantEntity.getLatitude(),
      restaurantEntity.getLongitude());

  return distance <= servingRadiusInKms;
  }

}

