using CommuteCompanionApi.Data;
using CommuteCompanionApi.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace CommuteCompanionApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class LocationsController : ControllerBase
{
    private readonly AppDbContext _context;

    public LocationsController(AppDbContext context)
    {
        _context = context;
    }

    // GET: /api/locations
    //
    // Returns only locations belonging to the
    // currently authenticated Firebase user.
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
            .OrderBy(x => x.Id)
            .ToListAsync();

        return Ok(locations);
    }

    // GET: /api/locations/{id}
    //
    // Returns the requested location only if
    // it belongs to the authenticated Firebase user.
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

    // POST: /api/locations
    //
    // Creates a location for the currently authenticated
    // Firebase user. The client does not provide the UserId.
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

        location.Id = 0;
        location.UserId = firebaseUid;
        location.CreatedAt = DateTime.UtcNow;

        _context.SavedLocations.Add(location);

        await _context.SaveChangesAsync();

        return CreatedAtAction(
            nameof(GetLocation),
            new { id = location.Id },
            location);
    }

    // DELETE: /api/locations/{id}
    //
    // Deletes the location only if it belongs to
    // the currently authenticated Firebase user.
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