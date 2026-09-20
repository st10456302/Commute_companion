using CommuteCompanionApi.Data;
using CommuteCompanionApi.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace CommuteCompanionApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class NotificationPreferencesController : ControllerBase
{
    private readonly AppDbContext _context;

    public NotificationPreferencesController(AppDbContext context)
    {
        _context = context;
    }

    // GET: /api/notificationpreferences/{userId}
    [HttpGet("{userId}")]
    public async Task<ActionResult<NotificationPreferences>>
        GetPreferences(string userId)
    {
        var preferences = await _context.NotificationPreferences
            .FirstOrDefaultAsync(x => x.UserId == userId);

        if (preferences == null)
        {
            return NotFound();
        }

        return Ok(preferences);
    }

    // POST: /api/notificationpreferences
    [HttpPost]
    public async Task<ActionResult<NotificationPreferences>>
        CreatePreferences(NotificationPreferences preferences)
    {
        var existing = await _context.NotificationPreferences
            .FirstOrDefaultAsync(x => x.UserId == preferences.UserId);

        if (existing != null)
        {
            return Conflict(
                "Notification preferences already exist for this user.");
        }

        preferences.Id = 0;
        preferences.UpdatedAt = DateTime.UtcNow;

        _context.NotificationPreferences.Add(preferences);

        await _context.SaveChangesAsync();

        return CreatedAtAction(
            nameof(GetPreferences),
            new { userId = preferences.UserId },
            preferences);
    }

    // PUT: /api/notificationpreferences/{userId}
    [HttpPut("{userId}")]
    public async Task<ActionResult<NotificationPreferences>>
        UpdatePreferences(
            string userId,
            NotificationPreferences updatedPreferences)
    {
        var existing = await _context.NotificationPreferences
            .FirstOrDefaultAsync(x => x.UserId == userId);

        if (existing == null)
        {
            return NotFound();
        }

        existing.NotificationsEnabled =
            updatedPreferences.NotificationsEnabled;

        existing.TrafficAlerts =
            updatedPreferences.TrafficAlerts;

        existing.WeatherAlerts =
            updatedPreferences.WeatherAlerts;

        existing.LoadSheddingAlerts =
            updatedPreferences.LoadSheddingAlerts;

        existing.UpdatedAt = DateTime.UtcNow;

        await _context.SaveChangesAsync();

        return Ok(existing);
    }
}