using System.Net.Http.Json;

namespace CommuteCompanionApi.Services;

public class OpenWeatherGeocodingService
{
    private readonly HttpClient _httpClient;

    public OpenWeatherGeocodingService(HttpClient httpClient)
    {
        _httpClient = httpClient;

        _httpClient.DefaultRequestHeaders.UserAgent.ParseAdd(
            "CommuteCompanion/1.0 (student-project)"
        );
    }

    public async Task<GeocodingResult?> GeocodeAddressAsync(
        string address)
    {
        if (string.IsNullOrWhiteSpace(address))
        {
            return null;
        }

        // First try the complete address as a free-form search.
        var result =
            await SearchFreeFormAsync(address);

        if (result != null)
        {
            return result;
        }

        // Add South Africa if the user did not provide it.
        result =
            await SearchFreeFormAsync(
                $"{address}, South Africa");

        if (result != null)
        {
            return result;
        }

        // Try to split a typical South African address into:
        // street + city + province + country.
        var parts = address
            .Split(
                ',',
                StringSplitOptions.RemoveEmptyEntries |
                StringSplitOptions.TrimEntries);

        if (parts.Length >= 2)
        {
            var street = parts[0];

            var city = parts[^1];

            if (parts.Length >= 3)
            {
                city = parts[^1];
            }

            result =
                await SearchStructuredAsync(
                    street,
                    city);

            if (result != null)
            {
                return result;
            }
        }

        return null;
    }

    private async Task<GeocodingResult?> SearchFreeFormAsync(
        string address)
    {
        var encodedAddress =
            Uri.EscapeDataString(address);

        var url =
            "https://nominatim.openstreetmap.org/search" +
            $"?q={encodedAddress}" +
            "&format=json" +
            "&limit=1" +
            "&countrycodes=za" +
            "&addressdetails=1";

        return await SearchAsync(url);
    }

    private async Task<GeocodingResult?> SearchStructuredAsync(
        string street,
        string city)
    {
        var encodedStreet =
            Uri.EscapeDataString(street);

        var encodedCity =
            Uri.EscapeDataString(city);

        var url =
            "https://nominatim.openstreetmap.org/search" +
            $"?street={encodedStreet}" +
            $"&city={encodedCity}" +
            "&state=Gauteng" +
            "&country=South Africa" +
            "&countrycodes=za" +
            "&format=json" +
            "&limit=1" +
            "&addressdetails=1";

        return await SearchAsync(url);
    }

    private async Task<GeocodingResult?> SearchAsync(
        string url)
    {
        var results =
            await _httpClient.GetFromJsonAsync<
                List<NominatimGeocodingResult>
            >(url);

        var result =
            results?.FirstOrDefault();

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