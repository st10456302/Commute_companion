using CommuteCompanionApi.Data;
using CommuteCompanionApi.Models;
using CommuteCompanionApi.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace CommuteCompanionApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class RoutesController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly OpenWeatherGeocodingService _geocodingService;

    public RoutesController(
        AppDbContext context,
        OpenWeatherGeocodingService geocodingService)
    {
        _context = context;
        _geocodingService = geocodingService;
    }

    [HttpGet]
    public async Task<ActionResult<IEnumerable<SavedRoute>>> GetRoutes()
    {
        var firebaseUid = GetFirebaseUid();

        if (firebaseUid == null)
        {
            return Unauthorized(new
            {
                error = "Authenticated Firebase user was not found."
            });
        }

        var routes = await _context.SavedRoutes
            .Where(x => x.UserId == firebaseUid)
            .OrderByDescending(x => x.CreatedAt)
            .ToListAsync();

        return Ok(routes);
    }

    [HttpPost]
    public async Task<ActionResult<SavedRoute>> CreateRoute(
        [FromBody] SavedRoute route)
    {
        var firebaseUid = GetFirebaseUid();

        if (firebaseUid == null)
        {
            return Unauthorized(new
            {
                error = "Authenticated Firebase user was not found."
            });
        }

        if (string.IsNullOrWhiteSpace(route.Destination))
        {
            return BadRequest(new
            {
                error = "A route destination is required."
            });
        }

        try
        {
            var coordinates =
                await _geocodingService.GeocodeAddressAsync(
                    route.Destination);

            if (coordinates == null)
            {
                return BadRequest(new
                {
                    error =
                        "The route destination could not be found. " +
                        "Please enter a more specific destination."
                });
            }

            route.Id = 0;
            route.UserId = firebaseUid;
            route.DestinationLatitude =
                coordinates.Latitude;
            route.DestinationLongitude =
                coordinates.Longitude;
            route.CreatedAt = DateTime.UtcNow;

            _context.SavedRoutes.Add(route);

            await _context.SaveChangesAsync();

            return CreatedAtAction(
                nameof(GetRoutes),
                new { id = route.Id },
                route);
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
                        "The route geocoding service " +
                        "is currently unavailable."
                });
        }
    }

    private string? GetFirebaseUid()
    {
        return HttpContext.Items["FirebaseUid"] as string;
    }
}