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

    // GET: /api/notificationpreferences
    //
    // Returns notification preferences for the
    // currently authenticated Firebase user.
    //
    // If the user does not have preferences yet,
    // default preferences are created automatically.
    [HttpGet]
    public async Task<ActionResult<NotificationPreferences>>
        GetPreferences()
    {
        var firebaseUid = GetFirebaseUid();

        if (firebaseUid == null)
        {
            return Unauthorized(new
            {
                error = "Authenticated Firebase user was not found."
            });
        }

        var preferences = await _context.NotificationPreferences
            .FirstOrDefaultAsync(x => x.UserId == firebaseUid);

        // First-time users do not have a notification
        // preference record yet, so create the defaults.
        if (preferences == null)
        {
            preferences = new NotificationPreferences
            {
                UserId = firebaseUid,
                NotificationsEnabled = true,
                TrafficAlerts = true,
                WeatherAlerts = true,
                LoadSheddingAlerts = true,
                UpdatedAt = DateTime.UtcNow
            };

            _context.NotificationPreferences.Add(preferences);

            await _context.SaveChangesAsync();
        }

        return Ok(preferences);
    }

    // POST: /api/notificationpreferences
    //
    // Creates notification preferences for the
    // currently authenticated Firebase user.
    [HttpPost]
    public async Task<ActionResult<NotificationPreferences>>
        CreatePreferences(
            [FromBody] NotificationPreferences preferences)
    {
        var firebaseUid = GetFirebaseUid();

        if (firebaseUid == null)
        {
            return Unauthorized(new
            {
                error = "Authenticated Firebase user was not found."
            });
        }

        var existing = await _context.NotificationPreferences
            .FirstOrDefaultAsync(x => x.UserId == firebaseUid);

        if (existing != null)
        {
            return Conflict(
                "Notification preferences already exist for this user.");
        }

        preferences.Id = 0;

        // Never trust UserId supplied by the client.
        preferences.UserId = firebaseUid;

        preferences.UpdatedAt = DateTime.UtcNow;

        _context.NotificationPreferences.Add(preferences);

        await _context.SaveChangesAsync();

        return CreatedAtAction(
            nameof(GetPreferences),
            null,
            preferences);
    }

    // PUT: /api/notificationpreferences
    //
    // Updates notification preferences for the
    // currently authenticated Firebase user.
    [HttpPut]
    public async Task<ActionResult<NotificationPreferences>>
        UpdatePreferences(
            [FromBody] NotificationPreferences updatedPreferences)
    {
        var firebaseUid = GetFirebaseUid();

        if (firebaseUid == null)
        {
            return Unauthorized(new
            {
                error = "Authenticated Firebase user was not found."
            });
        }

        var existing = await _context.NotificationPreferences
            .FirstOrDefaultAsync(x => x.UserId == firebaseUid);

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

    private string? GetFirebaseUid()
    {
        return HttpContext.Items["FirebaseUid"] as string;
    }
}