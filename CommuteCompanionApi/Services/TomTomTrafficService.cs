using System.Net.Http.Json;
using System.Text.Json.Serialization;

namespace CommuteCompanionApi.Services;

public class TomTomTrafficService
{
    private readonly HttpClient _httpClient;

    public TomTomTrafficService(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task<TomTomTrafficResult?> GetTrafficAsync(
        double latitude,
        double longitude)
    {
        var apiKey =
            Environment.GetEnvironmentVariable("TOMTOM_API_KEY");

        if (string.IsNullOrWhiteSpace(apiKey))
        {
            throw new InvalidOperationException(
                "TOMTOM_API_KEY is not configured.");
        }

        var point =
            $"{latitude.ToString(System.Globalization.CultureInfo.InvariantCulture)}," +
            $"{longitude.ToString(System.Globalization.CultureInfo.InvariantCulture)}";

        var url =
            "https://api.tomtom.com/traffic/services/4/flowSegmentData/absolute/10/json" +
            $"?point={point}" +
            "&unit=KMPH" +
            $"&key={apiKey}";

        var response =
            await _httpClient.GetAsync(url);

        if (!response.IsSuccessStatusCode)
        {
            var errorBody =
                await response.Content.ReadAsStringAsync();

            throw new HttpRequestException(
                $"TomTom traffic API returned " +
                $"HTTP {(int)response.StatusCode}: " +
                errorBody);
        }

        var trafficResponse =
            await response.Content.ReadFromJsonAsync<
                TomTomFlowResponse>();

        if (trafficResponse?.FlowSegmentData == null)
        {
            return null;
        }

        var flow = trafficResponse.FlowSegmentData;

        var currentSpeed = flow.CurrentSpeed;
        var freeFlowSpeed = flow.FreeFlow;
        var confidence = flow.Confidence;

        // The current TomTom response may not provide a usable
        // free-flow speed or segment length. Therefore, traffic
        // status is based on the observed current speed.
        var status =
            DetermineTrafficStatus(currentSpeed);

        Console.WriteLine(
            $"TomTom traffic — " +
            $"currentSpeed={currentSpeed} km/h, " +
            $"freeFlowSpeed={freeFlowSpeed} km/h, " +
            $"confidence={confidence}, " +
            $"length={flow.LengthInMeters} m, " +
            $"status={status}");

        return new TomTomTrafficResult
        {
            Status = status,
            CurrentSpeedKph = currentSpeed,
            FreeFlowSpeedKph = freeFlowSpeed,
            DelayMinutes = 0,
            Confidence = confidence
        };
    }

    public async Task<int> GetIncidentCountAsync(
        double latitude,
        double longitude)
    {
        var apiKey =
            Environment.GetEnvironmentVariable("TOMTOM_API_KEY");

        if (string.IsNullOrWhiteSpace(apiKey))
        {
            throw new InvalidOperationException(
                "TOMTOM_API_KEY is not configured.");
        }

        // Create a small geographic bounding box around
        // the user's saved location.
        const double radius = 0.02;

        var minLatitude = latitude - radius;
        var maxLatitude = latitude + radius;
        var minLongitude = longitude - radius;
        var maxLongitude = longitude + radius;

        var bbox =
            $"{minLongitude.ToString(System.Globalization.CultureInfo.InvariantCulture)}," +
            $"{minLatitude.ToString(System.Globalization.CultureInfo.InvariantCulture)}," +
            $"{maxLongitude.ToString(System.Globalization.CultureInfo.InvariantCulture)}," +
            $"{maxLatitude.ToString(System.Globalization.CultureInfo.InvariantCulture)}";

        var url =
            "https://api.tomtom.com/traffic/services/5/incidentDetails" +
            $"?key={apiKey}" +
            $"&bbox={bbox}" +
            "&language=en-GB" +
            "&timeValidityFilter=present";

        var response =
            await _httpClient.GetAsync(url);

        if (!response.IsSuccessStatusCode)
        {
            var errorBody =
                await response.Content.ReadAsStringAsync();

            throw new HttpRequestException(
                $"TomTom incident API returned " +
                $"HTTP {(int)response.StatusCode}: " +
                errorBody);
        }

        var incidentResponse =
            await response.Content.ReadFromJsonAsync<
                TomTomIncidentResponse>();

        var incidents =
            incidentResponse?.Incidents;

        var count = incidents?.Count ?? 0;

        Console.WriteLine(
            $"TomTom incidents — " +
            $"location={latitude},{longitude}, " +
            $"count={count}");

        return count;
    }

    public async Task<TomTomRouteResult?> GetRouteAsync(
        double originLatitude,
        double originLongitude,
        double destinationLatitude,
        double destinationLongitude)
    {
        var apiKey =
            Environment.GetEnvironmentVariable("TOMTOM_API_KEY");

        if (string.IsNullOrWhiteSpace(apiKey))
        {
            throw new InvalidOperationException(
                "TOMTOM_API_KEY is not configured.");
        }

        var origin =
            $"{originLatitude.ToString(System.Globalization.CultureInfo.InvariantCulture)}," +
            $"{originLongitude.ToString(System.Globalization.CultureInfo.InvariantCulture)}";

        var destination =
            $"{destinationLatitude.ToString(System.Globalization.CultureInfo.InvariantCulture)}," +
            $"{destinationLongitude.ToString(System.Globalization.CultureInfo.InvariantCulture)}";

        var locations =
            $"{origin}:{destination}";

        var url =
            "https://api.tomtom.com/routing/1/calculateRoute/" +
            $"{locations}/json" +
            $"?key={apiKey}" +
            "&traffic=true" +
            "&travelMode=car";

        var response =
            await _httpClient.GetAsync(url);

        if (!response.IsSuccessStatusCode)
        {
            var errorBody =
                await response.Content.ReadAsStringAsync();

            throw new HttpRequestException(
                $"TomTom routing API returned " +
                $"HTTP {(int)response.StatusCode}: " +
                errorBody);
        }

        var routeResponse =
            await response.Content.ReadFromJsonAsync<
                TomTomRouteResponse>();

        var route =
            routeResponse?.Routes?.FirstOrDefault();

        if (route?.Summary == null)
        {
            return null;
        }

        var summary = route.Summary;

        var travelTimeMinutes =
            (int)Math.Ceiling(
                summary.TravelTimeInSeconds / 60.0);

        var trafficDelayMinutes =
            (int)Math.Round(
                summary.TrafficDelayInSeconds / 60.0);

        Console.WriteLine(
            $"TomTom route — " +
            $"travelTime={travelTimeMinutes} min, " +
            $"trafficDelay={trafficDelayMinutes} min, " +
            $"length={summary.LengthInMeters} m");

        return new TomTomRouteResult
        {
            TravelTimeMinutes = travelTimeMinutes,
            TrafficDelayMinutes = trafficDelayMinutes,
            LengthInMeters = summary.LengthInMeters
        };
    }

    private static string DetermineTrafficStatus(
        double currentSpeed)
    {
        if (currentSpeed <= 0)
        {
            return "Unknown";
        }

        if (currentSpeed >= 45)
        {
            return "Light";
        }

        if (currentSpeed >= 30)
        {
            return "Moderate";
        }

        if (currentSpeed >= 15)
        {
            return "Heavy";
        }

        return "Severe";
    }
}

public class TomTomFlowResponse
{
    [JsonPropertyName("flowSegmentData")]
    public TomTomFlowSegmentData? FlowSegmentData { get; set; }
}

public class TomTomFlowSegmentData
{
    [JsonPropertyName("frc")]
    public string? FunctionalRoadClass { get; set; }

    [JsonPropertyName("currentSpeed")]
    public double CurrentSpeed { get; set; }

    [JsonPropertyName("freeFlow")]
    public double FreeFlow { get; set; }

    [JsonPropertyName("confidence")]
    public double Confidence { get; set; }

    [JsonPropertyName("roadClosure")]
    public bool RoadClosure { get; set; }

    [JsonPropertyName("lengthInMeters")]
    public double LengthInMeters { get; set; }
}

public class TomTomTrafficResult
{
    public string Status { get; set; } = "Unknown";
    public double CurrentSpeedKph { get; set; }
    public double FreeFlowSpeedKph { get; set; }
    public int DelayMinutes { get; set; }
    public double Confidence { get; set; }
}

public class TomTomRouteResult
{
    public int TravelTimeMinutes { get; set; }
    public int TrafficDelayMinutes { get; set; }
    public double LengthInMeters { get; set; }
}

public class TomTomRouteResponse
{
    [JsonPropertyName("routes")]
    public List<TomTomRoute>? Routes { get; set; }
}

public class TomTomRoute
{
    [JsonPropertyName("summary")]
    public TomTomRouteSummary? Summary { get; set; }
}

public class TomTomRouteSummary
{
    [JsonPropertyName("lengthInMeters")]
    public double LengthInMeters { get; set; }

    [JsonPropertyName("travelTimeInSeconds")]
    public int TravelTimeInSeconds { get; set; }

    [JsonPropertyName("trafficDelayInSeconds")]
    public int TrafficDelayInSeconds { get; set; }
}

public class TomTomIncidentResponse
{
    [JsonPropertyName("incidents")]
    public List<TomTomIncident>? Incidents { get; set; }
}

public class TomTomIncident
{
    [JsonPropertyName("type")]
    public string? Type { get; set; }

    [JsonPropertyName("properties")]
    public TomTomIncidentProperties? Properties { get; set; }
}

public class TomTomIncidentProperties
{
    [JsonPropertyName("iconCategory")]
    public int IconCategory { get; set; }

    [JsonPropertyName("magnitudeOfDelay")]
    public int MagnitudeOfDelay { get; set; }

    [JsonPropertyName("events")]
    public List<TomTomIncidentEvent>? Events { get; set; }
}

public class TomTomIncidentEvent
{
    [JsonPropertyName("description")]
    public string? Description { get; set; }
}