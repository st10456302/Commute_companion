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