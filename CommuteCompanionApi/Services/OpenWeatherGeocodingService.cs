using System.Net.Http.Json;

namespace CommuteCompanionApi.Services;

public class OpenWeatherGeocodingService
{
    private readonly HttpClient _httpClient;

    public OpenWeatherGeocodingService(HttpClient httpClient)
    {
        _httpClient = httpClient;

        // Nominatim requires an identifying User-Agent.
        _httpClient.DefaultRequestHeaders.UserAgent.ParseAdd(
            "CommuteCompanion/1.0 (student-project)"
        );
    }

    public async Task<GeocodingResult?> GeocodeAddressAsync(
        string address)
    {
        var encodedAddress =
            Uri.EscapeDataString(address);

        var url =
            "https://nominatim.openstreetmap.org/search" +
            $"?q={encodedAddress}" +
            "&format=json" +
            "&limit=1" +
            "&countrycodes=za";

        var results =
            await _httpClient.GetFromJsonAsync<
                List<NominatimGeocodingResult>
            >(url);

        var result = results?.FirstOrDefault();

        if (result == null)
        {
            return null;
        }

        if (!double.TryParse(
                result.Lat,
                System.Globalization.NumberStyles.Float,
                System.Globalization.CultureInfo.InvariantCulture,
                out var latitude))
        {
            return null;
        }

        if (!double.TryParse(
                result.Lon,
                System.Globalization.NumberStyles.Float,
                System.Globalization.CultureInfo.InvariantCulture,
                out var longitude))
        {
            return null;
        }

        return new GeocodingResult
        {
            Latitude = latitude,
            Longitude = longitude
        };
    }
}

public class NominatimGeocodingResult
{
    public string Lat { get; set; } = string.Empty;

    public string Lon { get; set; } = string.Empty;

    public string DisplayName { get; set; } = string.Empty;
}

public class GeocodingResult
{
    public double Latitude { get; set; }

    public double Longitude { get; set; }
}