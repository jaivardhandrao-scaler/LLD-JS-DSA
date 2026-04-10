# Problem 10: Weather Service Adapter

**Topic:** Adapter Pattern with unit conversions
**Difficulty:** Medium

---

## Scenario

Your weather app uses a `WeatherService` interface. You have two third-party weather providers that you CANNOT modify:

**Provider A -- OpenWeatherAPI** (already exists):
```java
class OpenWeatherAPI {
    public double getTempFahrenheit(String city) { return 72.5; }
    public double getWindMph(String city) { return 15.3; }
    public int getHumidity(String city) { return 65; }
}
```

**Provider B -- MeteoAPI** (already exists):
```java
class MeteoAPI {
    public Map<String, Object> getConditions(String location, String units) {
        // returns {"temp_c": 22.5, "wind_kmh": 24.6, "humidity_pct": 65}
        return Map.of("temp_c", 22.5, "wind_kmh", 24.6, "humidity_pct", 65);
    }
}
```

Your app expects everything in **metric** (Celsius, km/h, percentage).

## Requirements

1. **DO NOT modify** `OpenWeatherAPI` or `MeteoAPI` -- they are third-party (copy them as-is)

2. Create `WeatherData` (immutable): `double tempCelsius`, `double windKmh`, `int humidityPct`

3. Create `WeatherService` interface:
   - `WeatherData getWeather(String city)`

4. Create `OpenWeatherAdapter` implements `WeatherService`:
   - Wraps `OpenWeatherAPI` via composition
   - Converts: Fahrenheit -> Celsius: `(f - 32) * 5.0 / 9.0`
   - Converts: mph -> km/h: `mph * 1.60934`

5. Create `MeteoAdapter` implements `WeatherService`:
   - Wraps `MeteoAPI` via composition
   - Extracts values from the Map, casts appropriately

6. Create `WeatherApp` that takes `WeatherService` (constructor injection) and has `displayWeather(String city)`

7. `Main`: wire with both adapters, show same `WeatherApp` works with either provider

## What to create

```
OpenWeatherAPI.java      (copy as-is, DO NOT modify)
MeteoAPI.java            (copy as-is, DO NOT modify)
WeatherData.java
WeatherService.java
OpenWeatherAdapter.java
MeteoAdapter.java
WeatherApp.java
Main.java
```

## What I'll check

- Legacy classes are NOT modified
- Adapters use composition (hold a reference to the adaptee)
- Unit conversions are in the adapter (not in WeatherApp)
- `WeatherApp` depends only on `WeatherService` interface (DIP)
- Swapping providers = only change the wiring in `Main`
