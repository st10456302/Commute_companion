using System.Net.Http.Json;
using System.Text.Json.Serialization;

namespace CommuteCompanionApi.Services;

public class EskomSePushService
{
    private readonly HttpClient _httpClient;

    public EskomSePushService(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task<EskomStatusResult?> GetStatusAsync()
    {
        var apiKey =
            Environment.GetEnvironmentVariable(
                "ESKOMSEPUSH_API_KEY");

        if (string.IsNullOrWhiteSpace(apiKey))
        {
            throw new InvalidOperationException(
                "ESKOMSEPUSH_API_KEY is not configured.");
        }

        using var request =
            new HttpRequestMessage(
                HttpMethod.Get,
                "https://developer.sepush.co.za/business/3.1/status");

        request.Headers.TryAddWithoutValidation(
            "token",
            apiKey);

        var response =
            await _httpClient.SendAsync(request);

        if (!response.IsSuccessStatusCode)
        {
            var errorBody =
                await response.Content.ReadAsStringAsync();

            throw new HttpRequestException(
                $"EskomSePush API returned " +
                $"HTTP {(int)response.StatusCode}: " +
                errorBody);
        }

        var result =
            await response.Content.ReadFromJsonAsync<
                EskomStatusResponse>();

        if (result?.Status?.Eskom == null)
        {
            return null;
        }

        if (!int.TryParse(
                result.Status.Eskom.Stage,
                out var stage))
        {
            throw new InvalidOperationException(
                $"EskomSePush returned an invalid load-shedding stage: " +
                $"'{result.Status.Eskom.Stage}'.");
        }

        return new EskomStatusResult
        {
            Stage = stage
        };
    }

    public async Task<EskomAreaResult?> GetNearbyAreaAsync(
        double latitude,
        double longitude)
    {
        var apiKey =
            Environment.GetEnvironmentVariable(
                "ESKOMSEPUSH_API_KEY");

        if (string.IsNullOrWhiteSpace(apiKey))
        {
            throw new InvalidOperationException(
                "ESKOMSEPUSH_API_KEY is not configured.");
        }

        var url =
            "https://developer.sepush.co.za/business/3.1/areas_nearby" +
            $"?lat={latitude.ToString(System.Globalization.CultureInfo.InvariantCulture)}" +
            $"&lon={longitude.ToString(System.Globalization.CultureInfo.InvariantCulture)}" +
            "&radius=1";

        using var request =
            new HttpRequestMessage(
                HttpMethod.Get,
                url);

        request.Headers.TryAddWithoutValidation(
            "token",
            apiKey);

        var response =
            await _httpClient.SendAsync(request);

        if (!response.IsSuccessStatusCode)
        {
            var errorBody =
                await response.Content.ReadAsStringAsync();

            throw new HttpRequestException(
                $"EskomSePush areas_nearby API returned " +
                $"HTTP {(int)response.StatusCode}: " +
                errorBody);
        }

        var result =
            await response.Content.ReadFromJsonAsync<
                EskomAreasNearbyResponse>();

        var area =
            result?.Areas?.FirstOrDefault();

        if (area == null)
        {
            return null;
        }

        return new EskomAreaResult
        {
            Id = area.Id,
            Name = area.Name,
            Municipality = area.Municipality,
            Province = area.Province
        };
    }

    public async Task<EskomAreaInfoResult?> GetAreaInfoAsync(
        string areaId)
    {
        var apiKey =
            Environment.GetEnvironmentVariable(
                "ESKOMSEPUSH_API_KEY");

        if (string.IsNullOrWhiteSpace(apiKey))
        {
            throw new InvalidOperationException(
                "ESKOMSEPUSH_API_KEY is not configured.");
        }

        if (string.IsNullOrWhiteSpace(areaId))
        {
            throw new ArgumentException(
                "An EskomSePush area ID is required.",
                nameof(areaId));
        }

        var url =
            "https://developer.sepush.co.za/business/3.1/area" +
            $"?id={Uri.EscapeDataString(areaId)}";

        using var request =
            new HttpRequestMessage(
                HttpMethod.Get,
                url);

        request.Headers.TryAddWithoutValidation(
            "token",
            apiKey);

        var response =
            await _httpClient.SendAsync(request);

        if (!response.IsSuccessStatusCode)
        {
            var errorBody =
                await response.Content.ReadAsStringAsync();

            throw new HttpRequestException(
                $"EskomSePush area API returned " +
                $"HTTP {(int)response.StatusCode}: " +
                errorBody);
        }

        var result =
            await response.Content.ReadFromJsonAsync<
                EskomAreaInfoResponse>();

        if (result == null)
        {
            return null;
        }

        return new EskomAreaInfoResult
        {
            Id = result.Id,
            Name = result.Name,
            Municipality = result.Municipality,
            Province = result.Province,
            Schedules = result.Schedules
                .Select(schedule => new EskomScheduleReference
                {
                    Id = schedule.Id,
                    Type = schedule.Type,
                    AutoEnabled = schedule.AutoEnabled
                })
                .ToList()
        };
    }

    public async Task<EskomScheduleResult?> GetScheduleAsync(
        string scheduleId)
    {
        var apiKey =
            Environment.GetEnvironmentVariable(
                "ESKOMSEPUSH_API_KEY");

        if (string.IsNullOrWhiteSpace(apiKey))
        {
            throw new InvalidOperationException(
                "ESKOMSEPUSH_API_KEY is not configured.");
        }

        if (string.IsNullOrWhiteSpace(scheduleId))
        {
            throw new ArgumentException(
                "An EskomSePush schedule ID is required.",
                nameof(scheduleId));
        }

        var url =
            "https://developer.sepush.co.za/business/3.1/schedule" +
            $"?id={Uri.EscapeDataString(scheduleId)}";

        using var request =
            new HttpRequestMessage(
                HttpMethod.Get,
                url);

        request.Headers.TryAddWithoutValidation(
            "token",
            apiKey);

        var response =
            await _httpClient.SendAsync(request);

        if (!response.IsSuccessStatusCode)
        {
            var errorBody =
                await response.Content.ReadAsStringAsync();

            throw new HttpRequestException(
                $"EskomSePush schedule API returned " +
                $"HTTP {(int)response.StatusCode}: " +
                errorBody);
        }

        var result =
            await response.Content.ReadFromJsonAsync<
                EskomScheduleResponse>();

        if (result == null)
        {
            return null;
        }

        return new EskomScheduleResult
        {
            Name = result.Name,
            Events = result.Events
                .Select(eventItem => new EskomScheduleEventResult
                {
                    Start = eventItem.Start,
                    End = eventItem.End,
                    Note = eventItem.Note
                })
                .ToList()
        };
    }
}

public class EskomStatusResponse
{
    [JsonPropertyName("status")]
    public EskomStatusCollection? Status { get; set; }
}

public class EskomStatusCollection
{
    [JsonPropertyName("eskom")]
    public EskomStatusDetails? Eskom { get; set; }
}

public class EskomStatusDetails
{
    [JsonPropertyName("name")]
    public string Name { get; set; } = string.Empty;

    [JsonPropertyName("stage")]
    public string Stage { get; set; } = "0";

    [JsonPropertyName("stage_updated")]
    public string StageUpdated { get; set; } = string.Empty;

    [JsonPropertyName("next_stages")]
    public List<EskomNextStage> NextStages { get; set; } = new();
}

public class EskomNextStage
{
    [JsonPropertyName("stage")]
    public string Stage { get; set; } = string.Empty;

    [JsonPropertyName("start")]
    public string Start { get; set; } = string.Empty;

    [JsonPropertyName("end")]
    public string End { get; set; } = string.Empty;
}

public class EskomStatusResult
{
    public int Stage { get; set; }
}

public class EskomAreasNearbyResponse
{
    [JsonPropertyName("areas")]
    public List<EskomArea>? Areas { get; set; }
}

public class EskomArea
{
    [JsonPropertyName("id")]
    public string Id { get; set; } = string.Empty;

    [JsonPropertyName("municipality")]
    public string Municipality { get; set; } = string.Empty;

    [JsonPropertyName("name")]
    public string Name { get; set; } = string.Empty;

    [JsonPropertyName("province")]
    public string Province { get; set; } = string.Empty;
}

public class EskomAreaResult
{
    public string Id { get; set; } = string.Empty;
    public string Name { get; set; } = string.Empty;
    public string Municipality { get; set; } = string.Empty;
    public string Province { get; set; } = string.Empty;
}

public class EskomAreaInfoResponse
{
    [JsonPropertyName("id")]
    public string Id { get; set; } = string.Empty;

    [JsonPropertyName("municipality")]
    public string Municipality { get; set; } = string.Empty;

    [JsonPropertyName("name")]
    public string Name { get; set; } = string.Empty;

    [JsonPropertyName("province")]
    public string Province { get; set; } = string.Empty;

    [JsonPropertyName("schedules")]
    public List<EskomScheduleReferenceResponse> Schedules { get; set; } = new();
}

public class EskomScheduleReferenceResponse
{
    [JsonPropertyName("auto_enabled")]
    public bool AutoEnabled { get; set; }

    [JsonPropertyName("id")]
    public string Id { get; set; } = string.Empty;

    [JsonPropertyName("type")]
    public string Type { get; set; } = string.Empty;
}

public class EskomAreaInfoResult
{
    public string Id { get; set; } = string.Empty;
    public string Name { get; set; } = string.Empty;
    public string Municipality { get; set; } = string.Empty;
    public string Province { get; set; } = string.Empty;
    public List<EskomScheduleReference> Schedules { get; set; } = new();
}

public class EskomScheduleReference
{
    public string Id { get; set; } = string.Empty;
    public string Type { get; set; } = string.Empty;
    public bool AutoEnabled { get; set; }
}

public class EskomScheduleResponse
{
    [JsonPropertyName("events")]
    public List<EskomScheduleEvent> Events { get; set; } = new();

    [JsonPropertyName("name")]
    public string Name { get; set; } = string.Empty;
}

public class EskomScheduleEvent
{
    [JsonPropertyName("start")]
    public string Start { get; set; } = string.Empty;

    [JsonPropertyName("end")]
    public string End { get; set; } = string.Empty;

    [JsonPropertyName("note")]
    public string Note { get; set; } = string.Empty;
}

public class EskomScheduleResult
{
    public string Name { get; set; } = string.Empty;
    public List<EskomScheduleEventResult> Events { get; set; } = new();
}

public class EskomScheduleEventResult
{
    public string Start { get; set; } = string.Empty;
    public string End { get; set; } = string.Empty;
    public string Note { get; set; } = string.Empty;
}