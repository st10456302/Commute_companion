using System.Net.Http.Headers;
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
                "https://developer.sepush.co.za/business/2.0/status");

        request.Headers.TryAddWithoutValidation(
            "Token",
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

        if (result?.Status == null)
        {
            return null;
        }

        return new EskomStatusResult
        {
            Stage = result.Status.Eskom.Stage
        };
    }
}

public class EskomStatusResponse
{
    [JsonPropertyName("status")]
    public EskomStatus? Status { get; set; }
}

public class EskomStatus
{
    [JsonPropertyName("eskom")]
    public EskomStatusDetails Eskom { get; set; } = new();
}

public class EskomStatusDetails
{
    [JsonPropertyName("stage")]
    public int Stage { get; set; }
}

public class EskomStatusResult
{
    public int Stage { get; set; }
}