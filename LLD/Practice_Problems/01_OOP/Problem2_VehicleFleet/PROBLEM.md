# Problem 2: Vehicle Fleet Management

**Topic:** Abstract classes, Interfaces, Polymorphism, Upcasting
**Difficulty:** Medium

---

## Scenario

A logistics company manages a fleet of vehicles. Some vehicles are **refuelable** (petrol/diesel), some are **rechargeable** (electric). Some future vehicles might be both (hybrid). The system needs to calculate **trip cost** for any vehicle without knowing its type.

## Requirements

1. Create an abstract class `Vehicle` with:
   - `private` fields: `id` (String), `model` (String), `kmDriven` (double)
   - Constructor, getters
   - `void drive(double km)` -- adds to kmDriven
   - Abstract: `double tripCost(double distanceKm)`
   - `toString()` that returns `"model (id) - kmDriven km"`

2. Create interfaces:
   - `Refuelable` with `void refuel(double litres)` and `double getFuelLevel()`
   - `Rechargeable` with `void recharge(double kwh)` and `double getBatteryLevel()`

3. Create concrete vehicles:
   - `PetrolTruck` extends `Vehicle` implements `Refuelable`
     - Has `fuelLevel` (litres), `fuelEfficiency` (km per litre)
     - `tripCost` = distance / fuelEfficiency * pricePerLitre (pass as constructor arg)
   - `ElectricVan` extends `Vehicle` implements `Rechargeable`
     - Has `batteryLevel` (kwh), `energyEfficiency` (km per kwh)
     - `tripCost` = distance / energyEfficiency * pricePerKwh
   - `HybridCar` extends `Vehicle` implements `Refuelable, Rechargeable`
     - Has both fuel and battery. `tripCost` = uses the CHEAPER option for the given distance

4. Create `FleetManager` with:
   - `List<Vehicle> vehicles`
   - `double estimateTotalCost(double distanceKm)` -- sums `tripCost` across all vehicles (polymorphic, no instanceof)
   - `List<Refuelable> getRefuelableVehicles()` -- filters using `instanceof` (this is the ONE place it's OK)

5. `Main` class demonstrating the fleet with 1 of each vehicle type

## What to create

```
Vehicle.java
Refuelable.java
Rechargeable.java
PetrolTruck.java
ElectricVan.java
HybridCar.java
FleetManager.java
Main.java
```

## What I'll check

- Abstract class + interfaces used correctly
- `HybridCar` implements both interfaces (multiple interface inheritance)
- `tripCost()` is polymorphic -- `FleetManager` never checks vehicle type
- `getRefuelableVehicles()` uses `instanceof` correctly (safe downcasting)
- Fields are private, constructors validate
