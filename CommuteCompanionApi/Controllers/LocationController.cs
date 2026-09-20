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
    [HttpGet]
    public async Task<ActionResult<IEnumerable<SavedLocation>>> GetLocations()
    {
        var locations = await _context.SavedLocations
            .OrderBy(x => x.Id)
            .ToListAsync();

        return Ok(locations);
    }

    // GET: /api/locations/{id}
    [HttpGet("{id:int}")]
    public async Task<ActionResult<SavedLocation>> GetLocation(int id)
    {
        var location = await _context.SavedLocations
            .FirstOrDefaultAsync(x => x.Id == id);

        if (location == null)
        {
            return NotFound();
        }

        return Ok(location);
    }

    // POST: /api/locations
    [HttpPost]
    public async Task<ActionResult<SavedLocation>> CreateLocation(
        SavedLocation location)
    {
        location.Id = 0;
        location.CreatedAt = DateTime.UtcNow;

        _context.SavedLocations.Add(location);
        await _context.SaveChangesAsync();

        return CreatedAtAction(
            nameof(GetLocation),
            new { id = location.Id },
            location);
    }

    // DELETE: /api/locations/{id}
    [HttpDelete("{id:int}")]
    public async Task<IActionResult> DeleteLocation(int id)
    {
        var location = await _context.SavedLocations
            .FirstOrDefaultAsync(x => x.Id == id);

        if (location == null)
        {
            return NotFound();
        }

        _context.SavedLocations.Remove(location);
        await _context.SaveChangesAsync();

        return NoContent();
    }
}