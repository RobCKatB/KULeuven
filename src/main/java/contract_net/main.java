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

import static com.google.common.collect.Maps.newHashMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.measure.unit.SI;

import org.apache.commons.math3.random.RandomGenerator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.comm.CommModel;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.Depot;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;
import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.event.Listener;
import com.github.rinde.rinsim.geom.Graph;
import com.github.rinde.rinsim.geom.MultiAttributeData;
import com.github.rinde.rinsim.geom.io.DotGraphIO;
import com.github.rinde.rinsim.geom.io.Filters;
import com.github.rinde.rinsim.pdptw.common.StatsTracker;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.GraphRoadModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;


/**
 * Example showing a fleet of taxis that have to pickup and transport customers
 * around the city of Leuven.
 * <p>
 * If this class is run on MacOS it might be necessary to use
 * -XstartOnFirstThread as a VM argument.
 * @author Rinde van Lon
 */
public final class main {

  private static final int NUM_DEPOTS = 1; // TODO: set op meer dan 1 omdat anders centraal systeem?? -_-"
  private static final int NUM_TRUCKS = 2;
  private static final int NUM_PARCELS = 3;
  private static final int NUM_CHARINGSTATIONS = 2;

  // time in ms
  private static final long SERVICE_DURATION = 60000;
  private static final int TRUCK_CAPACITY = 10;
  private static final int DEPOT_CAPACITY = 100;

  private static final int SPEED_UP = 4;
  private static final int MAX_CAPACITY = 3;
  private static final double NEW_PARCEL_PROB = .007; //TODO op .007 zetten 

  private static final String MAP_FILE = "/data/maps/leuven-simple.dot";
  private static final Map<String, Graph<MultiAttributeData>> GRAPH_CACHE =
    newHashMap();
  
  private static final long TEST_STOP_TIME = 20 * 60 * 10000;
  private static final int TEST_SPEED_UP = 64;
  
  private main() {}
 

  /**
   * Starts the {@link main}.
   * @param args The first option may optionally indicate the end time of the
   *          simulation.
   */
  public static void main(@Nullable String[] args) {
    final long endTime = args != null && args.length >= 1 ? Long
      .parseLong(args[0]) : Long.MAX_VALUE;

    final String graphFile = args != null && args.length >= 2 ? args[1]
      : MAP_FILE;
    run(true, endTime, graphFile, null /* new Display() */, null, null);
  }

  /**
   * Run the example.
   * @param testing If <code>true</code> enables the test mode.
   */
  public static void run(boolean testing) {
    run(testing, Long.MAX_VALUE, MAP_FILE, null, null, null);
  }

  /**
   * Starts the example.
   * @param testing Indicates whether the method should run in testing mode.
   * @param endTime The time at which simulation should stop.
   * @param graphFile The graph that should be loaded.
   * @param display The display that should be used to show the ui on.
   * @param m The monitor that should be used to show the ui on.
   * @param list A listener that will receive callbacks from the ui.
   * @return The simulator instance.
   */
  public static Simulator run(boolean testing, final long endTime,
      String graphFile,
      @Nullable Display display, @Nullable Monitor m, @Nullable Listener list) {

    final View.Builder view = createGui(testing, display, m, list);
    
    // TODO: we must use withCollisionAvoidance() example. This can be obtained with a dynamicGraph instead of a staticGraph
    // however, with a dynamic graph we cannot give the map as parameter
    /*
    roadModel = CollisionGraphRoadModel.builder(GraphUtils.createGraph())
            .setVehicleLength(GraphUtils.VEHICLE_LENGTH)
            .build();
    */
    // use map of leuven
	  //TODO: include collisionavoidance and deadlock
	  /*
	  .withCollisionAvoidance()
    .withDistanceUnit(SI.METER)
    .withVehicleLength(VEHICLE_LENGTH)
	  .withMinDistance(1d))*/
    final Simulator simulator = Simulator.builder()
    	      .addModel(RoadModelBuilders.staticGraph(loadGraph(graphFile)))
    	      .addModel(DefaultPDPModel.builder())
    	      .addModel(CommModel.builder())
    	      .addModel(view)
    	      .build();
    final RandomGenerator rng = simulator.getRandomGenerator();
    final PDPModel pdpModel = simulator.getModelProvider().getModel(PDPModel.class);
    final DefaultPDPModel defaultpdpmodel = simulator.getModelProvider().getModel(DefaultPDPModel.class);
    final RoadModel roadModel = simulator.getModelProvider().getModel(
      RoadModel.class);
    final CommModel commModel = simulator.getModelProvider().getModel(CommModel.class);
    final List<AuctionResult> auctionResultsList;
    ArrayList<DispatchAgent> dispatchAgents = new ArrayList<DispatchAgent>();

    // generate an empty list to store the results of each auction
    AuctionResults auctionResults = new AuctionResults();
    auctionResultsList = auctionResults.getAuctionResults();
    
    // add depots, trucks and parcels to simulator
    //TODO take into account depot capacity
    for (int i = 0; i < NUM_DEPOTS; i++) {
    	DispatchAgent dispatchAgent = new DispatchAgent(defaultpdpmodel, roadModel, rng, roadModel.getRandomPosition(rng), auctionResultsList);
    	simulator.register(dispatchAgent);
    	dispatchAgents.add(dispatchAgent);
    }
    

    for (int i = 0; i < NUM_TRUCKS; i++) {
    	TruckAgent truckAgent = new TruckAgent(defaultpdpmodel, roadModel, roadModel.getRandomPosition(rng),TRUCK_CAPACITY, rng);
      simulator.register(truckAgent);
    }

    for (int i = 0; i < NUM_PARCELS; i++) {
    	Parcel parcel = Parcel.builder(roadModel.getRandomPosition(rng),
                roadModel.getRandomPosition(rng))
                .serviceDuration(SERVICE_DURATION) /// this might cause problems since we calculate the PDP distance (which is SERVICE_DURATION) and we do not use a constant
                .neededCapacity(1 + rng.nextInt(MAX_CAPACITY)) // we did not yet do anything with capacity
                .build();
		simulator.register(new Customer(parcel.getDto()));
		
		// Assign parcel to random DispatchAgent.
		dispatchAgents.get(rng.nextInt(dispatchAgents.size())).assignParcel(parcel);
    }
  
    /*
    for (int i = 0; i < NUM_CHARINGSTATIONS; i++) {
    	ChargingStation chargingStation = new ChargingStation(roadModel.getRandomPosition(rng), roadModel, rng);
    	simulator.register(chargingStation);
    }
    */
  
    simulator.addTickListener(new TickListener() {
      @Override
      public void tick(TimeLapse time) {
        if (time.getStartTime() > endTime) {
          simulator.stop();
        } /*else if (rng.nextDouble() < NEW_PARCEL_PROB) {
        	Parcel parcel =Parcel.builder(roadModel.getRandomPosition(rng),
                    roadModel.getRandomPosition(rng))
                    .serviceDuration(SERVICE_DURATION) /// this might cause problems since we calculate the PDP distance (which is SERVICE_DURATION) and we do not use a constant
                    .neededCapacity(1 + rng.nextInt(MAX_CAPACITY)) // we did not yet do anything with capacity
                    .build();
    		simulator.register(new Customer(parcel.getDto()));
    		

    		// Assign parcel to random DispatchAgent.
    		Set<DispatchAgent> dispatchAgents = (roadModel.getObjectsOfType(DispatchAgent.class));
    		int num = rng.nextInt(dispatchAgents.size());
    		int i = 0;
    		for (DispatchAgent dispatchAgent : dispatchAgents){
    		    if(i == num){
    		    	dispatchAgent.assignParcel(parcel);
    		    	break;
    		    }
    			i++;
    		}
        }*/
      }

      @Override
      public void afterTick(TimeLapse timeLapse) {}
    });
    simulator.start();
    /*
    System.out.println(simulator.getModelProvider().getModel(StatsTracker.class)
    	      .getStatistics());
    	      */
    return simulator;
  }

  
  static View.Builder createGui(
      boolean testing,
      @Nullable Display display,
      @Nullable Monitor m,
      @Nullable Listener list) {

    View.Builder view = View.builder()
      .with(GraphRoadModelRenderer.builder())
      .with(RoadUserRenderer.builder()
        .withImageAssociation(
        DispatchAgent.class, "/graphics/perspective/tall-building-64.png")
        .withImageAssociation(
          TruckAgent.class, "/graphics/flat/small-truck-64.png")
        .withImageAssociation(
          Customer.class, "/graphics/perspective/deliverypackage.png")
      .withImageAssociation(
    		  ChargingStation.class, "/graphics/perspective/gas-truck-64.png"))
      //.with(TaxiRenderer.builder(Language.ENGLISH))
      .withTitleAppendix("PDP Demo");

    if (testing) {
      view = view.withAutoClose()
        .withAutoPlay()
        .withSimulatorEndTime(TEST_STOP_TIME)
        .withSpeedUp(TEST_SPEED_UP);
    } else if (m != null && list != null && display != null) {
      view = view.withMonitor(m)
        .withSpeedUp(SPEED_UP)
        .withResolution(m.getClientArea().width, m.getClientArea().height)
        .withDisplay(display)
        .withCallback(list)
        .withAsync()
        .withAutoPlay()
        .withAutoClose();
    }
    return view;
  }

  // load the graph file
  static Graph<MultiAttributeData> loadGraph(String name) {
    try {
      if (GRAPH_CACHE.containsKey(name)) {
        return GRAPH_CACHE.get(name);
      }
      final Graph<MultiAttributeData> g = DotGraphIO
        .getMultiAttributeGraphIO(
          Filters.selfCycleFilter())
        .read(
          main.class.getResourceAsStream(name));

      GRAPH_CACHE.put(name, g);
      return g;
    } catch (final FileNotFoundException e) {
      throw new IllegalStateException(e);
    } catch (final IOException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * A customer with very permissive time windows.
   */
  static class Customer extends Parcel {
    Customer(ParcelDTO dto) {
      super(dto);
    }

    @Override
    public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {}
  }
}
