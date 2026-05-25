
/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.services;

import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.repositoryservices.RestaurantRepositoryService;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RestaurantServiceImpl implements RestaurantService {

  private final Double peakHoursServingRadiusInKms = 3.0;
  private final Double normalHoursServingRadiusInKms = 5.0;

  @Autowired
  private RestaurantRepositoryService restaurantRepositoryService;

  // TODO: CRIO_TASK_MODULE_RESTAURANTSAPI - Implement findAllRestaurantsCloseby.
  // Check RestaurantService.java file for the interface contract.
  @Override
  public GetRestaurantsResponse findAllRestaurantsCloseBy(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {

        log.info("Finding restaurants close by for request {} at time {}", 
        getRestaurantsRequest, currentTime);

        Double latitude = getRestaurantsRequest.getLatitude();
        Double longtitude = getRestaurantsRequest.getLongtitude();

        Double servingRadius;

        if(isPeakHour(currentTime)){
          servingRadius = peakHoursServingRadiusInKms;
        } else{
          servingRadius = normalHoursServingRadiusInKms;
        }

        List<Restaurant> restaurants = restaurantRepositoryService.findAllRestaurantsCloseBy(
          latitude, longtitude, currentTime, servingRadius);

          if(restaurants == null){
            restaurants =  new ArrayList<>();
          }

          return new GetRestaurantsResponse(restaurants);
        }

        private boolean isPeakHour(LocalTime time){

          LocalTime morningStart = LocalTime.of(8, 0);
          LocalTime morningEnd = LocalTime.of(10, 0);

          LocalTime afternoonStart = LocalTime.of(13, 0);
          LocalTime afternoonEnd = LocalTime.of(14, 0);
          
          LocalTime eveningStart = LocalTime.of(19, 0);
          LocalTime eveningEnd = LocalTime.of(21, 0);

          return (!time.isBefore(morningStart) && !time.isAfter(morningEnd)) || 
          (!time.isBefore(afternoonStart) && !time.isAfter(afternoonEnd)) ||
          (!time.isBefore(eveningStart) && !time.isAfter(eveningEnd));
        }      
       
  }

