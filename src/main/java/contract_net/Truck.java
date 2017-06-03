package contract_net;
/*
 * Copyright (C) 2011-2016 Rinde van Lon, iMinds-DistriNet, KU Leuven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import javax.annotation.Nullable;

import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;


/**
 * This truck implementation only picks parcels up, it does not deliver them.
 * This class is derived from Taxi.java, written by Rinde van Lon
 *
 */
class Truck extends Vehicle {
	private static final double SPEED = 1000d;
	private static final double ENERGYCONSUMPTION = 1d; // Per unit mileage
	private static final double ENERGYCAPACITY = 1000d;
	
	private Optional<Parcel> curr;
	private int capacity;
	private Point startPosition;
  
	private double energy;
  
	Truck(Point startPosition, int capacity) {
		super(VehicleDTO.builder()
			      .capacity(capacity)
			      .startPosition(startPosition)
			      .speed(SPEED)
			      .build());
		curr = Optional.absent();
	}
	
	// parcel pickup and delivery
	protected void tickImpl(TimeLapse time) {
	    final RoadModel rm = getRoadModel();
	    final PDPModel pm = getPDPModel();

	    if (!time.hasTimeLeft()) {
	      return;
	    }
	    if (!curr.isPresent()) {
	      curr = Optional.fromNullable(RoadModels.findClosestObject(
	        rm.getPosition(this), rm, Parcel.class));
	    }

	    if (curr.isPresent()) {
	      final boolean inCargo = pm.containerContains(this, curr.get());
	      // sanity check: if it is not in our cargo AND it is also not on the
	      // RoadModel, we cannot go to curr anymore.
	      if (!inCargo && !rm.containsObject(curr.get())) {
	        curr = Optional.absent();
	      } else if (inCargo) {
	        // if it is in cargo, go to its destination
	        rm.moveTo(this, curr.get().getDeliveryLocation(), time);
	        if (rm.getPosition(this).equals(curr.get().getDeliveryLocation())) {
	          // deliver when we arrive
	          pm.deliver(this, curr.get(), time);
	        }
	      } else {
	        // it is still available, go there as fast as possible
	        rm.moveTo(this, curr.get(), time);
	        if (rm.equalPosition(this, curr.get())) {
	          // pickup parcel
	          pm.pickup(this, curr.get(), time);
	        }
	      }
	    }
	}
	
	public Optional<Point> getPosition(){
		final RoadModel rm = getRoadModel();
		return Optional.of(rm.getPosition(this));
	}
	
	public void charge(double amount){
		this.energy = Math.max(this.energy+amount, this.ENERGYCAPACITY);
	}

  }