using CommuteCompanionApi.Data;
using CommuteCompanionApi.Models;
using CommuteCompanionApi.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace CommuteCompanionApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class DashboardController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly WeatherService _weatherService;

    public DashboardController(
        AppDbContext context,
        WeatherService weatherService)
    {
        _context = context;
        _weatherService = weatherService;
    }

    [HttpGet]
    public async Task<ActionResult<DashboardResponse>> GetDashboard(
        [FromQuery] int? locationId)
    {
        var firebaseUid = GetFirebaseUid();

        if (firebaseUid == null)
        {
            return Unauthorized(new
            {
                error = "Authenticated Firebase user was not found."
            });
        }

        if (locationId == null)
        {
            return BadRequest(new
            {
                error = "A locationId is required."
            });
        }

        var location = await _context.SavedLocations
            .FirstOrDefaultAsync(x =>
                x.Id == locationId &&
                x.UserId == firebaseUid);

        if (location == null)
        {
            return NotFound(new
            {
                error = "The requested saved location was not found."
            });
        }

        if (location.Latitude == null ||
            location.Longitude == null)
        {
            return BadRequest(new
            {
                error =
                    "The saved location does not have valid coordinates."
            });
        }

        WeatherResult? weather;

        try
        {
            weather =
                await _weatherService.GetCurrentWeatherAsync(
                    location.Latitude.Value,
                    location.Longitude.Value);
        }
        catch (InvalidOperationException exception)
        {
            return StatusCode(
                StatusCodes.Status503ServiceUnavailable,
                new
                {
                    error = exception.Message
                });
        }
        catch (HttpRequestException)
        {
            return StatusCode(
                StatusCodes.Status503ServiceUnavailable,
                new
                {
                    error =
                        "The weather service is currently unavailable."
                });
        }

        if (weather == null)
        {
            return StatusCode(
                StatusCodes.Status503ServiceUnavailable,
                new
                {
                    error =
                        "Weather data could not be retrieved."
                });
        }

        // Traffic and load-shedding values remain temporary
        // until their respective live API integrations are added.
        var dashboard = new DashboardResponse
        {
            LocationId = location.Id,
            LocationLabel = location.Label,
            LocationAddress = location.Address,

            Traffic = new TrafficDashboardData
            {
                Status = "Moderate",
                CommuteMinutes = 32,
                DelayMinutes = 5,
                IncidentCount = 2
            },

            LoadShedding = new LoadSheddingDashboardData
            {
                Stage = 2,
                PowerAvailable = true,
                ChangeIn = "2 hours",
                NextSlot = "18:00 - 20:30"
            },

            Weather = new WeatherDashboardData
            {
                TemperatureCelsius =
                    weather.TemperatureCelsius,

                Condition =
                    weather.Condition,

                WetRoads =
                    weather.WetRoads,

                AlertMessage =
                    weather.WetRoads
                        ? "Wet road conditions"
                        : "No weather alerts"
            },

            RetrievedAt = DateTime.UtcNow
        };

        return Ok(dashboard);
    }

    private string? GetFirebaseUid()
    {
        return HttpContext.Items["FirebaseUid"] as string;
    }
}