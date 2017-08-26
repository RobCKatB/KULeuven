Multi-agent System for a pickup and delivery problem solved via three different contract net protocols
-------------------------------------------------------------------------------------------------------

date: 26/8/2017
developed by: Robbert Camps and Katrien Bernaerts

SHORT DESCRIPTION OF THE PROGRAM
This program allows to solve pickup and delivery problems. Trucks are cooperating to pickup parcels located at several pickup positions in a city and deliver them at the desired place. In order to assign trucks to parcels, the dispatchagent (there can be multiple of them) organizes auctions. The trucks are constrained by their fuel level, and if the amount of fuel is not sufficient anymore to accept an auction, they have to go to a charging station. Three different versions of the contract net protocol (CNP) are implemented. The three versions differ in the way how truckagents deal with auctions:
1) the BASIC CNP, as originally described by Smith, R.G., The Contract Net Protocol: High-level Communication and Control in a Distributed Problem Solver. IEEE Trans. On Computers C-29 (12): 1104-1113 (1980). In this version, the truckagent does not participate in other auctions while the auction for which he made a bid is still running. This means that the truckagent can only deal with sequential auctions.
2) the PARALLEL_AUCION: truck agent is not limited to sequential auctions, but can participate in several auctions in parallel 
2) the DRIVING_AUCTION: truck agent can send a proposal for an auction while he is still driving to execute a PDP task. In the other versions of the CNP, the truck agent is not idle while driving, so he cannot send proposals/bids for an incoming auction.

HOW TO RUN THE PROGRAM?
- execute main.java
- change the mode to Mode.BASIC, Mode.DRIVING_AUCTIONS or Mode.PARALLEL_AUCTIONS in line 83 of main.java
- the following parameters can be modified in different classes. 

parameter		class		indicative value
--------		-----		----------------
SPEED			TruckAgent	1000d
ENERGYCONSUMPTION	TruckAgent	1d
ENERGYCAPACITY		TruckAgent	1000000d
reliability		TruckAgent	1.0D
LONELINESS_THRESHOLD	TruckAgent	10 * 1000
range			TruckAgent	Double.MAX_VALUE
AUCTION_DURATION	DispatchAgent	10000
reliability		DispatchAgent	1.0D
LONELINESS_THRESHOLD	DispatchAgent	10 * 1000
range			DispatchAgent	Double.MAX_VALUE
mode			main		Mode.BASIC, Mode.PARALLEL_AUCTIONS, Mode.DRIVING_AUCTIONS
NUM_DEPOTS		main		2
NUM_TRUCKS		main		10
NUM_PARCELS		main		30
NUM_CHARGINGSTATIONS	main		2
SERVICE_DURATION	main		60000
SPEED_UP		main		4
MAX_CAPACITY		main		3
NEW_PARCEL_PROB		main		0
MAP_FILE		main		/data/maps/leuven-simple.dot
TEST_STOP_TIME		main		50000*10000
TEST_SPEED_UP		main		64


