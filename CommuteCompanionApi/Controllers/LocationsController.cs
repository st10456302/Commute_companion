using CommuteCompanionApi.Data;
using CommuteCompanionApi.Models;
using CommuteCompanionApi.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace CommuteCompanionApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class LocationsController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly OpenWeatherGeocodingService _geocodingService;

    public LocationsController(
        AppDbContext context,
        OpenWeatherGeocodingService geocodingService)
    {
        _context = context;
        _geocodingService = geocodingService;
    }

    [HttpGet]
    public async Task<ActionResult<IEnumerable<SavedLocation>>> GetLocations()
    {
        var firebaseUid = GetFirebaseUid();

        if (firebaseUid == null)
        {
            return Unauthorized(new
            {
                error = "Authenticated Firebase user was not found."
            });
        }

        var locations = await _context.SavedLocations
            .Where(x => x.UserId == firebaseUid)
            .OrderByDescending(x => x.CreatedAt)
            .ToListAsync();

        return Ok(locations);
    }

    // -------------------------------------------------------------
    // Geocoding preview
    // -------------------------------------------------------------
    // Converts an address into coordinates without saving it.
    // This is used by the Android Set Location screen so that
    // the mini-map can update before the user presses Save.
    [HttpGet("geocode")]
    public async Task<IActionResult> GeocodeLocation(
        [FromQuery] string address)
    {
        var firebaseUid = GetFirebaseUid();

        if (firebaseUid == null)
        {
            return Unauthorized(new
            {
                error = "Authenticated Firebase user was not found."
            });
        }

        if (string.IsNullOrWhiteSpace(address))
        {
            return BadRequest(new
            {
                error = "An address is required."
            });
        }

        try
        {
            var coordinates =
                await _geocodingService.GeocodeAddressAsync(
                    address);

            if (coordinates == null)
            {
                return NotFound(new
                {
                    error =
                        "The location could not be found. " +
                        "Please enter a more specific address."
                });
            }

            return Ok(new
            {
                latitude = coordinates.Latitude,
                longitude = coordinates.Longitude
            });
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
                        "The location geocoding service " +
                        "is currently unavailable."
                });
        }
    }

    [HttpGet("{id:int}")]
    public async Task<ActionResult<SavedLocation>> GetLocation(int id)
    {
        var firebaseUid = GetFirebaseUid();

        if (firebaseUid == null)
        {
            return Unauthorized(new
            {
                error = "Authenticated Firebase user was not found."
            });
        }

        var location = await _context.SavedLocations
            .FirstOrDefaultAsync(x =>
                x.Id == id &&
                x.UserId == firebaseUid);

        if (location == null)
        {
            return NotFound();
        }

        return Ok(location);
    }

    [HttpPost]
    public async Task<ActionResult<SavedLocation>> CreateLocation(
        [FromBody] SavedLocation location)
    {
        var firebaseUid = GetFirebaseUid();

        if (firebaseUid == null)
        {
            return Unauthorized(new
            {
                error = "Authenticated Firebase user was not found."
            });
        }

        if (string.IsNullOrWhiteSpace(location.Address))
        {
            return BadRequest(new
            {
                error = "A location address is required."
            });
        }

        try
        {
            // Convert the user's address into coordinates.
            var coordinates =
                await _geocodingService.GeocodeAddressAsync(
                    location.Address);

            if (coordinates == null)
            {
                return BadRequest(new
                {
                    error =
                        "The location could not be found. " +
                        "Please enter a more specific address."
                });
            }

            location.Id = 0;
            location.UserId = firebaseUid;
            location.Latitude = coordinates.Latitude;
            location.Longitude = coordinates.Longitude;
            location.CreatedAt = DateTime.UtcNow;

            _context.SavedLocations.Add(location);

            await _context.SaveChangesAsync();

            return CreatedAtAction(
                nameof(GetLocation),
                new { id = location.Id },
                location);
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
                        "The location geocoding service " +
                        "is currently unavailable."
                });
        }
    }

    [HttpDelete("{id:int}")]
    public async Task<IActionResult> DeleteLocation(int id)
    {
        var firebaseUid = GetFirebaseUid();

        if (firebaseUid == null)
        {
            return Unauthorized(new
            {
                error = "Authenticated Firebase user was not found."
            });
        }

        var location = await _context.SavedLocations
            .FirstOrDefaultAsync(x =>
                x.Id == id &&
                x.UserId == firebaseUid);

        if (location == null)
        {
            return NotFound();
        }

        _context.SavedLocations.Remove(location);

        await _context.SaveChangesAsync();

        return NoContent();
    }

    private string? GetFirebaseUid()
    {
        return HttpContext.Items["FirebaseUid"] as string;
    }
}