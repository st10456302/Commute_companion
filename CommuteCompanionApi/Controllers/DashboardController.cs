using CommuteCompanionApi.Data;
using CommuteCompanionApi.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace CommuteCompanionApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class DashboardController : ControllerBase
{
    private readonly AppDbContext _context;

    public DashboardController(AppDbContext context)
    {
        _context = context;
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

        // Temporary dashboard data.
        // These values will later be replaced by live
        // TomTom, EskomSePush and OpenWeather data.
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
                TemperatureCelsius = 22,
                Condition = "Sunny",
                WetRoads = false,
                AlertMessage = "No weather alerts"
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