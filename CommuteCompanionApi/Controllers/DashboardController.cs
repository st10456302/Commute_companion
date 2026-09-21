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
    private readonly TomTomTrafficService _trafficService;

    public DashboardController(
        AppDbContext context,
        WeatherService weatherService,
        TomTomTrafficService trafficService)
    {
        _context = context;
        _weatherService = weatherService;
        _trafficService = trafficService;
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

        // ---------------------------------------------------------
        // WEATHER
        // ---------------------------------------------------------

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

        // ---------------------------------------------------------
        // TRAFFIC
        // ---------------------------------------------------------

        TomTomTrafficResult? traffic;

        try
        {
            traffic =
                await _trafficService.GetTrafficAsync(
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
        catch (HttpRequestException exception)
        {
            return StatusCode(
                StatusCodes.Status503ServiceUnavailable,
                new
                {
                    error =
                        "The traffic service is currently unavailable.",
                    details = exception.Message
                });
        }

        if (traffic == null)
        {
            return StatusCode(
                StatusCodes.Status503ServiceUnavailable,
                new
                {
                    error =
                        "Traffic data could not be retrieved."
                });
        }

        int incidentCount;

        try
        {
            incidentCount =
                await _trafficService.GetIncidentCountAsync(
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
        catch (HttpRequestException exception)
        {
            return StatusCode(
                StatusCodes.Status503ServiceUnavailable,
                new
                {
                    error =
                        "The traffic incident service is currently unavailable.",
                    details = exception.Message
                });
        }

        // ---------------------------------------------------------
        // DASHBOARD RESPONSE
        // ---------------------------------------------------------

        var dashboard = new DashboardResponse
        {
            LocationId = location.Id,

            LocationLabel =
                location.Label,

            LocationAddress =
                location.Address,


Traffic = new TrafficDashboardData
{
    Status = traffic.Status,
    CommuteMinutes = 0,
    DelayMinutes = traffic.DelayMinutes,
    IncidentCount = incidentCount
},

            LoadShedding = new LoadSheddingDashboardData
            {
                // Temporary values until the EskomSePush
                // integration is implemented.
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

            RetrievedAt =
                DateTime.UtcNow
        };

        return Ok(dashboard);
    }



    private string? GetFirebaseUid()
    {
        return HttpContext.Items["FirebaseUid"] as string;
    }
}