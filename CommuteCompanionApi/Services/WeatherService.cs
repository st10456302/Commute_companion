using System.Net.Http.Json;

namespace CommuteCompanionApi.Services;

public class WeatherService
{
    private readonly HttpClient _httpClient;

    public WeatherService(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task<WeatherResult?> GetCurrentWeatherAsync(
        double latitude,
        double longitude)
    {
        var apiKey =
            Environment.GetEnvironmentVariable(
                "OPENWEATHER_API_KEY");

        if (string.IsNullOrWhiteSpace(apiKey))
        {
            throw new InvalidOperationException(
                "OPENWEATHER_API_KEY is not configured.");
        }

        var url =
            "https://api.openweathermap.org/data/2.5/weather" +
            $"?lat={latitude.ToString(
                System.Globalization.CultureInfo.InvariantCulture)}" +
            $"&lon={longitude.ToString(
                System.Globalization.CultureInfo.InvariantCulture)}" +
            "&units=metric" +
            $"&appid={apiKey}";

        var response =
            await _httpClient.GetFromJsonAsync<OpenWeatherResponse>(
                url);

        if (response == null)
        {
            return null;
        }

        var weather =
            response.Weather?.FirstOrDefault();

        if (weather == null)
        {
            return null;
        }

        return new WeatherResult
        {
            TemperatureCelsius = response.Main?.Temp ?? 0,
            Condition = weather.Main,
            Description = weather.Description,
            WetRoads = IsWetWeather(weather.Main)
        };
    }

    private static bool IsWetWeather(string condition)
    {
        return condition.Equals(
                   "Rain",
                   StringComparison.OrdinalIgnoreCase)
               || condition.Equals(
                   "Drizzle",
                   StringComparison.OrdinalIgnoreCase)
               || condition.Equals(
                   "Thunderstorm",
                   StringComparison.OrdinalIgnoreCase);
    }
}

public class OpenWeatherResponse
{
    public OpenWeatherMain? Main { get; set; }

    public List<OpenWeatherCondition>? Weather { get; set; }
}

public class OpenWeatherMain
{
    public double Temp { get; set; }
}

public class OpenWeatherCondition
{
    public string Main { get; set; } = string.Empty;

    public string Description { get; set; } = string.Empty;
}

public class WeatherResult
{
    public double TemperatureCelsius { get; set; }

    public string Condition { get; set; } = string.Empty;

    public string Description { get; set; } = string.Empty;

    public bool WetRoads { get; set; }
}