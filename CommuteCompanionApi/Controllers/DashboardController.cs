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
    private readonly EskomSePushService _eskomSePushService;

    public DashboardController(
        AppDbContext context,
        WeatherService weatherService,
        TomTomTrafficService trafficService,
        EskomSePushService eskomSePushService)
    {
        _context = context;
        _weatherService = weatherService;
        _trafficService = trafficService;
        _eskomSePushService = eskomSePushService;
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

        // WEATHER
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
                new { error = exception.Message });
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

        // TRAFFIC
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
                new { error = exception.Message });
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

        // TRAFFIC INCIDENTS
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
                new { error = exception.Message });
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

        // LOAD SHEDDING STATUS
        EskomStatusResult? loadShedding;

        try
        {
            loadShedding =
                await _eskomSePushService.GetStatusAsync();
        }
        catch (InvalidOperationException exception)
        {
            return StatusCode(
                StatusCodes.Status503ServiceUnavailable,
                new { error = exception.Message });
        }
        catch (HttpRequestException exception)
        {
            return StatusCode(
                StatusCodes.Status503ServiceUnavailable,
                new
                {
                    error =
                        "The load-shedding service is currently unavailable.",
                    details = exception.Message
                });
        }

        if (loadShedding == null)
        {
            return StatusCode(
                StatusCodes.Status503ServiceUnavailable,
                new
                {
                    error =
                        "Load-shedding data could not be retrieved."
                });
        }

        Console.WriteLine(
            $"EskomSePush — national stage={loadShedding.Stage}");

        // LOAD SHEDDING AREA
        EskomAreaResult? nearbyArea;

        try
        {
            nearbyArea =
                await _eskomSePushService.GetNearbyAreaAsync(
                    location.Latitude.Value,
                    location.Longitude.Value);
        }
        catch (InvalidOperationException exception)
        {
            return StatusCode(
                StatusCodes.Status503ServiceUnavailable,
                new { error = exception.Message });
        }
        catch (HttpRequestException exception)
        {
            return StatusCode(
                StatusCodes.Status503ServiceUnavailable,
                new
                {
                    error =
                        "The load-shedding area service is currently unavailable.",
                    details = exception.Message
                });
        }

        if (nearbyArea == null)
        {
            return StatusCode(
                StatusCodes.Status503ServiceUnavailable,
                new
                {
                    error =
                        "A load-shedding area could not be found for the saved location."
                });
        }

        Console.WriteLine(
            $"EskomSePush area — " +
            $"id='{nearbyArea.Id}', " +
            $"name='{nearbyArea.Name}', " +
            $"municipality='{nearbyArea.Municipality}'");

        // LOAD SHEDDING AREA INFORMATION
        EskomAreaInfoResult? areaInfo;

        try
        {
            areaInfo =
                await _eskomSePushService.GetAreaInfoAsync(
                    nearbyArea.Id);
        }
        catch (InvalidOperationException exception)
        {
            return StatusCode(
                StatusCodes.Status503ServiceUnavailable,
                new { error = exception.Message });
        }
        catch (HttpRequestException exception)
        {
            return StatusCode(
                StatusCodes.Status503ServiceUnavailable,
                new
                {
                    error =
                        "The load-shedding area information service is currently unavailable.",
                    details = exception.Message
                });
        }

        if (areaInfo == null)
        {
            return StatusCode(
                StatusCodes.Status503ServiceUnavailable,
                new
                {
                    error =
                        "Load-shedding area information could not be retrieved."
                });
        }

        // SELECT THE LOADSHEDDING SCHEDULE
        var loadSheddingSchedule =
            areaInfo.Schedules
                .FirstOrDefault(schedule =>
                    schedule.Type.Equals(
                        "loadshedding",
                        StringComparison.OrdinalIgnoreCase));

        string nextLoadSheddingSlot =
            "No upcoming load-shedding event";

        string loadSheddingChange =
            $"Area: {areaInfo.Name}";

        if (loadSheddingSchedule != null)
        {
            Console.WriteLine(
                $"EskomSePush schedule — " +
                $"id='{loadSheddingSchedule.Id}', " +
                $"type='{loadSheddingSchedule.Type}'");

            // LOAD SHEDDING SCHEDULE
            EskomScheduleResult? schedule;

            try
            {
                schedule =
                    await _eskomSePushService.GetScheduleAsync(
                        loadSheddingSchedule.Id);
            }
            catch (InvalidOperationException exception)
            {
                return StatusCode(
                    StatusCodes.Status503ServiceUnavailable,
                    new { error = exception.Message });
            }
            catch (HttpRequestException exception)
            {
                return StatusCode(
                    StatusCodes.Status503ServiceUnavailable,
                    new
                    {
                        error =
                            "The load-shedding schedule service is currently unavailable.",
                        details = exception.Message
                    });
            }

if (schedule != null)
{
    var now = DateTimeOffset.Now;

    var parsedEvents =
        schedule.Events
            .Select(eventItem => new
            {
                Event = eventItem,
                Start = ParseDateTime(eventItem.Start),
                End = ParseDateTime(eventItem.End)
            })
            .Where(x =>
                x.Start.HasValue &&
                x.End.HasValue)
            .ToList();

    var currentEvent =
        parsedEvents
            .FirstOrDefault(x =>
                x.Start!.Value <= now &&
                x.End!.Value > now);

    var upcomingEvent =
        parsedEvents
            .Where(x =>
                x.Start!.Value > now)
            .OrderBy(x => x.Start!.Value)
            .FirstOrDefault();

    if (currentEvent != null)
    {
        var end =
            currentEvent.End!.Value.ToLocalTime();

        nextLoadSheddingSlot =
            $"Currently off until {end:HH:mm}";

        loadSheddingChange =
            string.IsNullOrWhiteSpace(currentEvent.Event.Note)
                ? "Load shedding is currently active"
                : currentEvent.Event.Note;

        Console.WriteLine(
            $"EskomSePush current event — " +
            $"start='{currentEvent.Start}', " +
            $"end='{currentEvent.End}', " +
            $"note='{currentEvent.Event.Note}'");
    }
    else if (upcomingEvent != null)
    {
        var start =
            upcomingEvent.Start!.Value.ToLocalTime();

        var end =
            upcomingEvent.End!.Value.ToLocalTime();

        nextLoadSheddingSlot =
            $"{start:dd MMM HH:mm} - {end:HH:mm}";

        loadSheddingChange =
            string.IsNullOrWhiteSpace(upcomingEvent.Event.Note)
                ? "Upcoming load shedding"
                : upcomingEvent.Event.Note;

        Console.WriteLine(
            $"EskomSePush upcoming event — " +
            $"start='{upcomingEvent.Start}', " +
            $"end='{upcomingEvent.End}', " +
            $"note='{upcomingEvent.Event.Note}'");
    }
    else
    {
        loadSheddingChange =
            $"Area: {areaInfo.Name}";

        nextLoadSheddingSlot =
            "No upcoming load-shedding event";

        Console.WriteLine(
            $"EskomSePush — no current or upcoming " +
            $"load-shedding event for area '{areaInfo.Name}'.");
    }
}
        }
        else
        {
            Console.WriteLine(
                $"EskomSePush — no loadshedding schedule " +
                $"was found for area '{areaInfo.Name}'.");
        }

        // DASHBOARD RESPONSE
        var dashboard = new DashboardResponse
        {
            LocationId = location.Id,
            LocationLabel = location.Label,
            LocationAddress = location.Address,

            Traffic = new TrafficDashboardData
            {
                Status = traffic.Status,
                CommuteMinutes = 0,
                DelayMinutes = traffic.DelayMinutes,
                IncidentCount = incidentCount
            },

            LoadShedding = new LoadSheddingDashboardData
            {
                Stage = loadShedding.Stage,
                PowerAvailable = loadShedding.Stage == 0,
                ChangeIn = loadSheddingChange,
                NextSlot = nextLoadSheddingSlot
            },

            Weather = new WeatherDashboardData
            {
                TemperatureCelsius = weather.TemperatureCelsius,
                Condition = weather.Condition,
                WetRoads = weather.WetRoads,
                AlertMessage =
                    weather.WetRoads
                        ? "Wet road conditions"
                        : "No weather alerts"
            },

            RetrievedAt = DateTime.UtcNow
        };

        Console.WriteLine(
            $"Dashboard assembled — " +
            $"location='{location.Label}', " +
            $"traffic='{traffic.Status}', " +
            $"incidents={incidentCount}, " +
            $"loadSheddingStage={loadShedding.Stage}, " +
            $"nextSlot='{nextLoadSheddingSlot}', " +
            $"temperature={weather.TemperatureCelsius}");

        return Ok(dashboard);
    }

    private static DateTimeOffset? ParseDateTime(
        string value)
    {
        if (DateTimeOffset.TryParse(
                value,
                out var parsed))
        {
            return parsed;
        }

        return null;
    }

    private string? GetFirebaseUid()
    {
        return HttpContext.Items["FirebaseUid"] as string;
    }
}